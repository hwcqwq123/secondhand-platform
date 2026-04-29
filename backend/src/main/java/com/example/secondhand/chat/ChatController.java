package com.example.secondhand.chat;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.secondhand.audit.AuditDecision;
import com.example.secondhand.audit.ContentAuditService;
import com.example.secondhand.auth.CurrentUser;
import com.example.secondhand.config.ApiResponse;
import com.example.secondhand.item.Item;
import com.example.secondhand.item.ItemRepository;
import com.example.secondhand.item.ItemStatus;
import com.example.secondhand.user.User;
import com.example.secondhand.user.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

// 新增：用户与用户之间的聊天接口
@RestController
@RequestMapping("/api/chats")
public class ChatController {

    private final ChatConversationRepository conversations;
    private final ChatMessageRepository messages;
    private final ItemRepository items;
    private final UserRepository users;
    private final CurrentUser currentUser;

    // 新增：聊天消息发送前进行内容审核
    private final ContentAuditService contentAuditService;

    public ChatController(
            ChatConversationRepository conversations,
            ChatMessageRepository messages,
            ItemRepository items,
            UserRepository users,
            CurrentUser currentUser,
            ContentAuditService contentAuditService
    ) {
        this.conversations = conversations;
        this.messages = messages;
        this.items = items;
        this.users = users;
        this.currentUser = currentUser;
        this.contentAuditService = contentAuditService;
    }

    // 新增：根据商品创建或打开聊天会话
    @PostMapping("/items/{itemId}")
    @Transactional
    public ApiResponse<ChatConversationResponse> openByItem(@PathVariable Long itemId) {
        User me = getMe();

        Item item = items.findByIdWithSeller(itemId)
                .orElseThrow(() -> new EntityNotFoundException("item not found"));

        if (Boolean.TRUE.equals(item.getDeleted())) {
            return ApiResponse.fail("商品已删除，不能发起聊天");
        }

        if (item.getStatus() == ItemStatus.OFF_SHELF) {
            return ApiResponse.fail("商品已下架，不能发起聊天");
        }

        User seller = item.getSeller();

        if (seller.getId().equals(me.getId())) {
            return ApiResponse.fail("不能和自己聊天");
        }

        ChatConversation conversation = conversations
                .findByItem_IdAndBuyer_IdAndSeller_Id(item.getId(), me.getId(), seller.getId())
                .orElseGet(() -> {
                    ChatConversation c = new ChatConversation();
                    c.setItem(item);
                    c.setBuyer(me);
                    c.setSeller(seller);
                    c.setUpdatedAt(Instant.now());
                    return conversations.save(c);
                });

        ChatMessage last = messages.findTopByConversation_IdOrderByCreatedAtDesc(conversation.getId())
                .orElse(null);

        long unread = messages.countByConversation_IdAndSender_IdNotAndReadFlagFalse(
                conversation.getId(),
                me.getId()
        );

        ChatConversationResponse response = ChatConversationResponse.from(
                conversation,
                me.getId(),
                last,
                unread
        );

        return ApiResponse.ok(response, "会话已打开");
    }

    // 新增：查看我的聊天会话列表
    @GetMapping
    @Transactional(readOnly = true)
    public ApiResponse<List<ChatConversationResponse>> listMyConversations() {
        User me = getMe();

        List<ChatConversation> myConversations = conversations.findMyConversations(me.getId());

        // 修改：显式使用 Collectors.toList，避免类型推断异常
        List<ChatConversationResponse> responseList = myConversations.stream()
                .map(conversation -> {
                    ChatMessage last = messages
                            .findTopByConversation_IdOrderByCreatedAtDesc(conversation.getId())
                            .orElse(null);

                    long unread = messages.countByConversation_IdAndSender_IdNotAndReadFlagFalse(
                            conversation.getId(),
                            me.getId()
                    );

                    return ChatConversationResponse.from(
                            conversation,
                            me.getId(),
                            last,
                            unread
                    );
                })
                .collect(Collectors.toList());

        return ApiResponse.ok(responseList);
    }

    // 新增：查看某个会话下的消息列表
    @GetMapping("/{conversationId}/messages")
    @Transactional
    public ApiResponse<List<ChatMessageResponse>> listMessages(@PathVariable Long conversationId) {
        User me = getMe();

        ChatConversation conversation = getConversationAndCheckParticipant(conversationId, me);

        // 新增：打开会话时，把对方发来的未读消息标记为已读
        List<ChatMessage> unreadMessages = messages.findByConversation_IdAndSender_IdNotAndReadFlagFalse(
                conversation.getId(),
                me.getId()
        );

        unreadMessages.forEach(message -> message.setReadFlag(true));
        messages.saveAll(unreadMessages);

        List<ChatMessageResponse> responseList = messages
                .findByConversation_IdOrderByCreatedAtAsc(conversation.getId())
                .stream()
                .map(message -> ChatMessageResponse.from(message, me.getId()))
                .collect(Collectors.toList());

        return ApiResponse.ok(responseList);
    }

    // 新增：发送聊天消息，发送前调用本地 AI / 规则审核服务
    @PostMapping("/{conversationId}/messages")
    @Transactional
    public ApiResponse<ChatMessageResponse> sendMessage(
            @PathVariable Long conversationId,
            @Valid @RequestBody ChatSendMessageRequest req
    ) {
        User me = getMe();

        ChatConversation conversation = getConversationAndCheckParticipant(conversationId, me);

        String content = req.safeContent();

        if (content.isBlank()) {
            return ApiResponse.fail("消息内容不能为空");
        }

        // 新增：聊天消息内容审核
        AuditDecision audit = contentAuditService.auditChatMessage(content);

        if (!audit.allowed()) {
            return ApiResponse.fail("消息审核未通过：" + audit.reason());
        }

        ChatMessage message = new ChatMessage();
        message.setConversation(conversation);
        message.setSender(me);
        message.setContent(content);

        // 新增：保存审核结果，方便后续扩展管理员审核记录
        message.setAuditStatus(audit.status());
        message.setAuditReason(audit.reason());
        message.setAuditScore(audit.score());
        message.setReadFlag(false);

        ChatMessage saved = messages.save(message);

        // 新增：发送消息后更新会话时间
        conversation.setUpdatedAt(Instant.now());
        conversations.save(conversation);

        ChatMessageResponse response = ChatMessageResponse.from(saved, me.getId());

        return ApiResponse.ok(response, "消息发送成功");
    }

    // 新增：手动标记已读
    @PutMapping("/{conversationId}/read")
    @Transactional
    public ApiResponse<Void> markRead(@PathVariable Long conversationId) {
        User me = getMe();

        ChatConversation conversation = getConversationAndCheckParticipant(conversationId, me);

        List<ChatMessage> unreadMessages = messages.findByConversation_IdAndSender_IdNotAndReadFlagFalse(
                conversation.getId(),
                me.getId()
        );

        unreadMessages.forEach(message -> message.setReadFlag(true));
        messages.saveAll(unreadMessages);

        return ApiResponse.ok(null, "已标记为已读");
    }

    private User getMe() {
        String username = currentUser.username();

        if (username == null) {
            throw new EntityNotFoundException("current user not found");
        }

        return users.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("user not found"));
    }

    private ChatConversation getConversationAndCheckParticipant(Long conversationId, User me) {
        ChatConversation conversation = conversations.findByIdWithAll(conversationId)
                .orElseThrow(() -> new EntityNotFoundException("conversation not found"));

        boolean isBuyer = conversation.getBuyer().getId().equals(me.getId());
        boolean isSeller = conversation.getSeller().getId().equals(me.getId());

        if (!isBuyer && !isSeller) {
            throw new EntityNotFoundException("conversation not found");
        }

        return conversation;
    }
}
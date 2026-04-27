package com.example.secondhand.order;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<TradeOrder, Long> {

    @Query("""
            select o from TradeOrder o
            join fetch o.item i
            join fetch i.seller
            join fetch o.buyer
            where i.seller.id = :sellerId
            order by o.id desc
            """)
    List<TradeOrder> findBySellerIdWithItemAndBuyer(@Param("sellerId") Long sellerId);

    @Query("""
            select o from TradeOrder o
            join fetch o.item i
            join fetch i.seller
            join fetch o.buyer
            where o.buyer.id = :buyerId
            order by o.id desc
            """)
    List<TradeOrder> findByBuyerIdWithItemSellerAndBuyer(@Param("buyerId") Long buyerId);

    List<TradeOrder> findByStatusAndCreatedAtBefore(OrderStatus status, Instant deadline);

    boolean existsByItem_Id(Long itemId);

    boolean existsByItem_IdAndStatus(Long itemId, OrderStatus status);

    boolean existsByItem_IdAndStatusIn(Long itemId, Collection<OrderStatus> statuses);

    /**
     * 判断某个商品是否存在未完成或已支付订单。
     * CREATED：待支付
     * PAID：已支付
     */
    @Query("""
            select count(o) > 0 from TradeOrder o
            where o.item.id = :itemId
            and o.status in :statuses
            """)
    boolean existsActiveOrderByItemId(
            @Param("itemId") Long itemId,
            @Param("statuses") Collection<OrderStatus> statuses
    );

    /**
     * 查询某个商品的超时未支付订单。
     * 下单前释放超时订单，避免商品长期卡在 RESERVED。
     */
    @Query("""
            select o from TradeOrder o
            join fetch o.item i
            join fetch i.seller
            join fetch o.buyer
            where i.id = :itemId
            and o.status = :status
            and o.createdAt < :deadline
            """)
    List<TradeOrder> findExpiredCreatedOrdersByItemId(
            @Param("itemId") Long itemId,
            @Param("status") OrderStatus status,
            @Param("deadline") Instant deadline
    );
}
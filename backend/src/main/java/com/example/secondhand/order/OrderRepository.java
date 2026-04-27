package com.example.secondhand.order;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

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

    long countByStatus(OrderStatus status);

    long countByItem_Id(Long itemId);

    long countByBuyer_Id(Long buyerId);

    long countByItem_Seller_Id(Long sellerId);

    @Query("""
            select count(o) > 0 from TradeOrder o
            where o.item.id = :itemId
            and o.status in :statuses
            """)
    boolean existsActiveOrderByItemId(
            @Param("itemId") Long itemId,
            @Param("statuses") Collection<OrderStatus> statuses
    );

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

    @Query("""
            select o from TradeOrder o
            join fetch o.item i
            join fetch i.seller s
            join fetch o.buyer b
            where (:keyword is null
                   or lower(i.title) like lower(concat('%', :keyword, '%'))
                   or lower(b.username) like lower(concat('%', :keyword, '%'))
                   or lower(s.username) like lower(concat('%', :keyword, '%')))
            and (:status is null or o.status = :status)
            order by o.id desc
            """)
    List<TradeOrder> findAdminOrders(
            @Param("keyword") String keyword,
            @Param("status") OrderStatus status
    );

    @Query("""
            select o from TradeOrder o
            join fetch o.item i
            join fetch i.seller
            join fetch o.buyer
            where o.id = :id
            """)
    Optional<TradeOrder> findByIdWithItemBuyerSeller(@Param("id") Long id);
}
package com.example.secondhand.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface OrderRepository extends JpaRepository<TradeOrder, Long> {

  @Query("""
    select o
    from TradeOrder o
    join fetch o.item i
    join fetch i.seller
    join fetch o.buyer
    where i.seller.id = :sellerId
    order by o.id desc
    """)
  List<TradeOrder> findBySellerIdWithItemAndBuyer(Long sellerId);

  @Query("""
    select o
    from TradeOrder o
    join fetch o.item i
    join fetch i.seller
    join fetch o.buyer
    where o.buyer.id = :buyerId
    order by o.id desc
    """)
  List<TradeOrder> findByBuyerIdWithItemSellerAndBuyer(Long buyerId);

  List<TradeOrder> findByStatusAndCreatedAtBefore(OrderStatus status, Instant deadline);

  boolean existsByItem_Id(Long itemId);

  boolean existsByItem_IdAndStatus(Long itemId, com.example.secondhand.order.OrderStatus status);

  boolean existsByItem_IdAndStatusIn(Long itemId, java.util.Collection<com.example.secondhand.order.OrderStatus> statuses);

}
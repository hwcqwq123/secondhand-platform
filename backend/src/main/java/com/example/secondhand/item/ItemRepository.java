package com.example.secondhand.item;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("select i from Item i join fetch i.seller order by i.id desc")
    List<Item> findAllWithSeller();

    @Query("select i from Item i join fetch i.seller where lower(i.title) like lower(concat('%', :keyword, '%')) order by i.id desc")
    List<Item> searchWithSeller(@Param("keyword") String keyword);

    @Query("select i from Item i join fetch i.seller where i.status = :status order by i.id desc")
    List<Item> findByStatusWithSeller(@Param("status") ItemStatus status);

    @Query("select i from Item i join fetch i.seller where i.id = :id")
    Optional<Item> findByIdWithSeller(@Param("id") Long id);

    @Query("select i from Item i join fetch i.seller order by i.createdAt desc")
    List<Item> findLatestWithSeller(Pageable pageable);

    @Query("""
            select i from Item i
            join fetch i.seller
            where i.seller.id = :sellerId
            order by i.id desc
            """)
    List<Item> findBySellerIdWithSeller(@Param("sellerId") Long sellerId);

    /**
     * MySQL 行锁：
     * 下单、支付、取消订单时使用。
     * Redis 锁只是前置拦截，MySQL 行锁才是最终一致性的兜底。
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Item i join fetch i.seller where i.id = :id")
    Optional<Item> findWithLockById(@Param("id") Long id);
}
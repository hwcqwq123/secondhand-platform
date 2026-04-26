package com.example.secondhand.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.LockModeType;
import java.util.Optional;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

  @Query("select i from Item i join fetch i.seller order by i.id desc")
  List<Item> findAllWithSeller();

  @Query("select i from Item i join fetch i.seller where lower(i.title) like lower(concat('%', :keyword, '%')) order by i.id desc")
  List<Item> searchWithSeller(String keyword);

  @Query("select i from Item i join fetch i.seller where i.status = :status order by i.id desc")
  List<Item> findByStatusWithSeller(ItemStatus status);

  @Query("select i from Item i join fetch i.seller where i.id = :id")
  Optional<Item> findByIdWithSeller(Long id);

  @Query("select i from Item i join fetch i.seller order by i.createdAt desc")
  List<Item> findLatestWithSeller(Pageable pageable);

  @Query("""
    select i
    from Item i
    join fetch i.seller
    where i.seller.id = :sellerId
    order by i.id desc
    """)
  List<Item> findBySellerIdWithSeller(Long sellerId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<Item> findWithLockById(Long id);


}
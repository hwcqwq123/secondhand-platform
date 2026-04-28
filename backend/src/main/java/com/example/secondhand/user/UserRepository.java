package com.example.secondhand.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByUsername(String username);

  boolean existsByUsername(String username);

  long countByStatus(UserStatus status);

  /**
   * 用于限制 SUPER_ADMIN 唯一。
   */
  long countByRole(String role);

  Optional<User> findFirstByRole(String role);

  @Query("""
          select u from User u
          where (:keyword is null
                 or lower(u.username) like lower(concat('%', :keyword, '%'))
                 or lower(coalesce(u.nickname, '')) like lower(concat('%', :keyword, '%')))
          and (:role is null or u.role = :role)
          and (:status is null or u.status = :status)
          order by u.id desc
          """)
  List<User> findAdminUsers(
          @Param("keyword") String keyword,
          @Param("role") String role,
          @Param("status") UserStatus status
  );
}
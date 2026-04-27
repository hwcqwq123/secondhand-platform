package com.example.secondhand.admin;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AdminOperationLogRepository extends JpaRepository<AdminOperationLog, Long> {

    @Query("""
            select l from AdminOperationLog l
            where (:keyword is null
                   or lower(l.adminUsername) like lower(concat('%', :keyword, '%'))
                   or lower(l.action) like lower(concat('%', :keyword, '%'))
                   or lower(l.targetType) like lower(concat('%', :keyword, '%'))
                   or lower(coalesce(l.detail, '')) like lower(concat('%', :keyword, '%'))
                   or lower(coalesce(l.reason, '')) like lower(concat('%', :keyword, '%')))
            order by l.id desc
            """)
    List<AdminOperationLog> search(
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
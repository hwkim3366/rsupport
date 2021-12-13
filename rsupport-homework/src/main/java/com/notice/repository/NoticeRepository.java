package com.notice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.notice.domain.Notice;
import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    @Query("SELECT rdb FROM Notice rdb JOIN FETCH rdb.rdbAttachments WHERE rdb.id = :id")
    Optional<Notice> findByIdUsingJoin(@Param("id") Long id);
}

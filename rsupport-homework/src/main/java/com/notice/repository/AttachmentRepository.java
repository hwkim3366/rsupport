package com.notice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.notice.domain.Attachment;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
}

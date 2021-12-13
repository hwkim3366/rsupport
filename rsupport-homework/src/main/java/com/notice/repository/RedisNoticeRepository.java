package com.notice.repository;

import org.springframework.data.repository.CrudRepository;

import com.notice.domain.RedisNotice;

public interface RedisNoticeRepository extends CrudRepository<RedisNotice, Long> {
}

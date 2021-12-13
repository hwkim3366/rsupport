package com.notice.domain;

import javax.persistence.Id;

import org.springframework.data.redis.core.RedisHash;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
@RedisHash(("Attachment"))
public class RedisAttachment {
	@Id
    private Long id;
    private String name;

    public RedisAttachment(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public void update(Attachment rdbAttachment) {
        this.name = rdbAttachment.getName();
    }
}

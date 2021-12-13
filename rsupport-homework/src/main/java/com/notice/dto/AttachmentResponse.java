package com.notice.dto;


import java.util.List;

import com.notice.domain.RedisAttachment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AttachmentResponse {
    private Long id;
    private String name;

    public static AttachmentResponse from(RedisAttachment redisAttachment) {
        return new AttachmentResponse(redisAttachment.getId(), redisAttachment.getName());
    }
}

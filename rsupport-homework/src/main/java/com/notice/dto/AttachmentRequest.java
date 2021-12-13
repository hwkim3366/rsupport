package com.notice.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.notice.domain.Attachment;

import lombok.*;


@Getter
@Setter
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AttachmentRequest {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    public Attachment toEntity() {
        return Attachment.builder()
                .name(name)
                .build();
    }
}

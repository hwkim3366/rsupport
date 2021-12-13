package com.notice.dto;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.notice.domain.RedisAttachment;
import com.notice.domain.RedisNotice;

@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NoticeResponse {
	
    private Long id;
    
    private String title;
    
    private String content;
    
    private String author;
    
    private int viewCount;
    
    private String startTime;
    
    private String endTime;
    
    private List<AttachmentResponse> attachmentResponses;

    public static NoticeResponse fromEntity(RedisNotice notice) {
    	
        return new NoticeResponse(
					                notice.getId(),
					                notice.getTitle(),
					                notice.getContent(),
					                notice.getAuthor(),
					                notice.getViewCount(),
					                notice.getStartTime(),
					                notice.getEndTime(),
					                fromAttachments(notice.getAttachments())
					        		);
    }

    public static List<AttachmentResponse> fromAttachments(List<RedisAttachment> attachments) {
    	
        return attachments.stream().map(AttachmentResponse::from).collect(Collectors.toList());
    }
}

package com.notice.dto;


import com.notice.domain.Notice;
import com.notice.domain.Attachment;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.stream.Collectors;


@Getter
@Setter
@ToString
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class NoticeRequest {
	
    private String title;
    
    private String content;
    
    private String author;
    
    private String startTime;
    
    private String endTime;
    
    private List<AttachmentRequest> attachments;

    public Notice toRdbEntity() {
    	
        List<Attachment> rdbAttachments = toAttachments();
        
        return new Notice(title, content, author, startTime, endTime);
    }

    public List<Attachment> toAttachments() {
    	
        return attachments.stream().map(AttachmentRequest::toEntity).collect(Collectors.toList());
    }
}

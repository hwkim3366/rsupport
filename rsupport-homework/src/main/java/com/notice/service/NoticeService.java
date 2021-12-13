package com.notice.service;

import java.nio.file.Path;
import org.springframework.web.multipart.MultipartFile;
import com.notice.dto.AttachmentRequest;
import com.notice.dto.AttachmentResponse;
import com.notice.dto.NoticeRequest;
import com.notice.dto.NoticeResponse;
import org.springframework.core.io.Resource;

public interface NoticeService {
	
	public void init();

	public void store(String prefix, MultipartFile file);

	public Path load(String filename);

	public Resource loadAsResource(String filename);
    
	public NoticeResponse createNotice(NoticeRequest noticeRequest);
    
	public NoticeResponse getNotice(Long id);
    
	public NoticeResponse updateNotice(Long id, NoticeRequest noticeRequest);
    
	public void deleteNotice(Long id);
    
	public void deleteAttachment(Long noticeId, Long attachmentId);
    
	public AttachmentResponse updateAttachment(Long noticeId, Long attachmentId, AttachmentRequest request);
    
}

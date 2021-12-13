package com.notice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;
import com.notice.domain.*;
import com.notice.dto.AttachmentRequest;
import com.notice.dto.AttachmentResponse;
import com.notice.dto.NoticeRequest;
import com.notice.dto.NoticeResponse;
import com.notice.exception.NotFoundAttachmentException;
import com.notice.exception.NotFoundNoticeException;
import com.notice.repository.AttachmentRepository;
import com.notice.repository.NoticeRepository;
import com.notice.repository.RedisNoticeRepository;
import com.notice.service.NoticeService;

@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {
	
	@Value("${spring.servlet.multipart.location}")
    private String uploadPath;
	
    private final NoticeRepository rdbNoticeRepository;
    
    private final RedisNoticeRepository redisNoticeRepository;
    
    private final AttachmentRepository rdbAttachmentRepository;
    
    @Override
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadPath));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload folder!");
        }
    }
    
    @Override
    public void store(String prefix, MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new Exception("ERROR : File is empty.");
            }
            Path root = Paths.get(uploadPath);
            if (!Files.exists(root)) {
                init();
            }

            try (InputStream inputStream = file.getInputStream()) {
            	Files.copy(inputStream, root.resolve(prefix + file.getOriginalFilename()),
                    StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
        }
    }

    @Override
    public Path load(String filename) {
    	return Paths.get(uploadPath).resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename) {
    	try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            }
            else {
                throw new RuntimeException("Could not read file: " + filename);
            }
        }
        catch (MalformedURLException e) {
            throw new RuntimeException("Could not read file: " + filename, e);
        }
    }
    
    @Transactional
    public NoticeResponse createNotice(NoticeRequest noticeRequest) {
    	
        Notice notice = noticeRequest.toRdbEntity();
        addRdbAttachments(noticeRequest, notice);
        Notice savedRdbNotice = rdbNoticeRepository.save(notice);

        RedisNotice redisNotice = savedRdbNotice.toRedisEntity();
        addRedisAttachments(notice, redisNotice);
        RedisNotice savedRedisNotice = redisNoticeRepository.save(redisNotice);

        return NoticeResponse.fromEntity(savedRedisNotice);
    }

    @Transactional
    public NoticeResponse getNotice(Long id) {
    	
        RedisNotice redisNotice;
        Optional<RedisNotice> redisNoticeOptional = redisNoticeRepository.findById(id);

        if (!redisNoticeOptional.isPresent()) {
            Notice notice = findByIdFromRdb(rdbNoticeRepository.findByIdUsingJoin(id));
            redisNoticeRepository.save(notice.toRedisEntity());
            redisNoticeOptional = redisNoticeRepository.findById(id);
        }

        redisNotice = addNoticeViewCountAtRedis(redisNoticeOptional);

        if (redisNotice.isSynViewCount()) {
            synchronizeViewCountBetweenRedisAndRdb(id, redisNotice);
        }

        return NoticeResponse.fromEntity(redisNotice);
    }

    @Transactional
    public NoticeResponse updateNotice(Long id, NoticeRequest noticeRequest) {
    	
        Notice notice = findByIdFromRdb(rdbNoticeRepository.findByIdUsingJoin(id));
        notice.update(noticeRequest);
        rdbNoticeRepository.save(notice);

        RedisNotice redisNotice = findByIdFromRedis(id);
        redisNotice.update(notice);
        redisNoticeRepository.save(redisNotice);

        return NoticeResponse.fromEntity(redisNotice);
    }

    @Transactional
    public AttachmentResponse updateAttachment(Long noticeId, Long attachmentId, AttachmentRequest attachmentRequest) {
    	
        Notice notice = findByIdFromRdb(rdbNoticeRepository.findByIdUsingJoin(noticeId));
        Attachment rdbAttachment = getRdbAttachment(attachmentId, notice);
        rdbAttachment.update(attachmentRequest);
        rdbNoticeRepository.save(notice);

        RedisNotice redisNotice = findByIdFromRedis(noticeId);
        RedisAttachment redisAttachment = getRedisAttachment(attachmentId, redisNotice);
        redisAttachment.update(rdbAttachment);
        redisNoticeRepository.save(redisNotice);

        return AttachmentResponse.from(redisAttachment);
    }

    @Transactional
    public void deleteNotice(Long id) {
    	
        RedisNotice redisNotice = findByIdFromRedis(id);
        redisNoticeRepository.delete(redisNotice);

        Notice notice = findByIdFromRdb(rdbNoticeRepository.findById(id));
        rdbNoticeRepository.delete(notice);
    }
    
    @Transactional
    public void deleteAttachment(Long noticeId, Long attachmentId) {
    	
    	RedisNotice redisNotice = findByIdFromRedis(noticeId);
    	RedisAttachment redisAttachment = getRedisAttachment(attachmentId, redisNotice);
    	redisNotice.getAttachments().remove(redisAttachment);
    	redisNoticeRepository.save(redisNotice);
    	
    	rdbAttachmentRepository.deleteById(attachmentId);
    }

    private Notice findByIdFromRdb(Optional<Notice> byIdUsingJoin) {
    	
        return byIdUsingJoin.orElseThrow(NotFoundNoticeException::new);
    }

    private RedisNotice findByIdFromRedis(Long noticeId) {
    	
        return redisNoticeRepository.findById(noticeId).orElseThrow(NotFoundNoticeException::new);
    }

    private RedisAttachment getRedisAttachment(Long attachmentId, RedisNotice redisNotice) {
    	
        return redisNotice.getAttachments().stream()
                .filter(attachment -> attachment.getId().equals(attachmentId))
                .findFirst()
                .orElseThrow(NotFoundAttachmentException::new);
    }

    private Attachment getRdbAttachment(Long attachmentId, Notice notice) {
    	
        return notice.getRdbAttachments().stream()
                .filter(attachment -> attachment.getId().equals(attachmentId))
                .findFirst()
                .orElseThrow(NotFoundAttachmentException::new);
    }

    private void addRdbAttachments(NoticeRequest noticeRequest, Notice notice) {
    	
        noticeRequest.getAttachments().stream().map(AttachmentRequest::toEntity).forEach(notice::addAttachment);
    }

    private void addRedisAttachments(Notice notice, RedisNotice redisNotice) {
    	
    	notice.getRdbAttachments().stream().map(Attachment::toRedisEntity).forEach(redisNotice::addAttachments);
    }
    
    private RedisNotice addNoticeViewCountAtRedis(Optional<RedisNotice> redisNoticeOptional) {
    	
        RedisNotice redisNotice = redisNoticeOptional.get();
        redisNotice.addViewCount();
        redisNoticeRepository.save(redisNotice);
        return redisNotice;
    }

    private void synchronizeViewCountBetweenRedisAndRdb(Long id, RedisNotice redisNotice) {
    	
        Notice notice = findByIdFromRdb(rdbNoticeRepository.findByIdUsingJoin(id));
        notice.updateViewCount(redisNotice.getViewCount());
        rdbNoticeRepository.save(notice);
    }
    
}

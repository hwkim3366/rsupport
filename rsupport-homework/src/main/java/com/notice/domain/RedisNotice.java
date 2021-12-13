package com.notice.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.format.annotation.DateTimeFormat;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@ToString
@RedisHash(("Notice"))
public class RedisNotice {

    @Id
    private Long id;

    private String title;

    private String content;

    private String author;

    private int viewCount = 0;

    private String startTime;

    private String endTime;

    @CreatedDate
    @DateTimeFormat(pattern = "yyyy-MM-dd-HH:mm")
    private LocalDateTime createdAt = LocalDateTime.now();

    @LastModifiedDate
    @DateTimeFormat(pattern = "yyyy-MM-dd-HH:mm")
    private LocalDateTime lastModifiedAt;

    private List<RedisAttachment> attachments = new ArrayList<>();

    @Builder
    public RedisNotice(Long id,
                       String title,
                       String content,
                       String author,
                       String startTime,
                       String endTime) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.author = author;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public void update(Notice notice) {
        updateTitle(notice.getTitle());
        updateContent(notice.getContent());
        updateAuthor(notice.getAuthor());
        updateStartTime(notice.getStartTime());
        updateEndTime(notice.getEndTime());
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void updateAuthor(String author) {
        this.author = author;
    }

    public void updateStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void updateEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void addAttachments(RedisAttachment redisAttachment) {
        attachments.add(redisAttachment);
    }
    
    public void addViewCount() {
        this.viewCount++;
    }

    public boolean isSynViewCount() {
        return viewCount % 10 == 0;
    }
}

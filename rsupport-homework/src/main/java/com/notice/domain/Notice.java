package com.notice.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.common.CommonEntity;
import com.notice.dto.NoticeRequest;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notice extends CommonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content")
    private String content;

    @Column(name = "author", nullable = false)
    private String author;

    @Column(name = "view_count")
    private int viewCount = 0;

    @Column(name = "start_time", nullable = false)
    private String startTime;

    @Column(name = "end_time", nullable = false)
    private String endTime;

    @OneToMany(mappedBy = "notice", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attachment> rdbAttachments = new ArrayList<>();

    @Builder
    public Notice(String title,
                     String content,
                     String author,
                     String startTime,
                     String endTime) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public void update(NoticeRequest noticeRequest) {
        updateTitle(noticeRequest.getTitle());
        updateContent(noticeRequest.getContent());
        updateAuthor(noticeRequest.getAuthor());
        updateStartTime(noticeRequest.getStartTime());
        updateEndTime(noticeRequest.getEndTime());
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

    public void updateViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public RedisNotice toRedisEntity() {
        return RedisNotice.builder()
                .id(id)
                .title(title)
                .content(content)
                .author(author)
                .startTime(startTime)
                .endTime(endTime)
                .build();
    }

    public List<RedisAttachment> toAttachmentsRedisEntity() {
        return rdbAttachments.stream().map(Attachment::toRedisEntity)
                .collect(Collectors.toList());
    }

    public void addAttachment(Attachment rdbAttachment) {
        rdbAttachments.add(rdbAttachment);
        rdbAttachment.setNotice(this);
    }
}


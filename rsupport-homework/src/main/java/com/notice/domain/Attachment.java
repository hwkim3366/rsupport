package com.notice.domain;

import lombok.*;

import javax.persistence.*;

import com.notice.dto.AttachmentRequest;

import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Attachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "file_name")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id")
    private Notice notice;

    @Builder
    public Attachment(String name) {
        this.name = name;
    }

    public RedisAttachment toRedisEntity() {
        return new RedisAttachment(id, name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Attachment that = (Attachment) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public void update(AttachmentRequest attachmentRequest) {
        this.name = attachmentRequest.getName();
    }
}

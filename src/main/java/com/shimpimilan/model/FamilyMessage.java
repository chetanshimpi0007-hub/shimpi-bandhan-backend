package com.shimpimilan.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "family_messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FamilyMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private FamilyDiscussionRoom room;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String messageType; // "TEXT", "IMAGE", "PDF", "VOICE", "LOCATION", "CARD"
    private String fileUrl; // URL from Cloudinary if media

    @ManyToOne
    @JoinColumn(name = "reply_to_message_id")
    private FamilyMessage replyTo;

    @Builder.Default
    private Boolean isDeletedForEveryone = false;

    @CreationTimestamp
    private LocalDateTime sentAt;
}

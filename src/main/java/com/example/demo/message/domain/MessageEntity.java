package com.example.demo.message.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "message")
public class MessageEntity {

    @EmbeddedId
    private MessageId id;

    @Column(name = "message_text")
    private String messageText;

    @Column(name = "message_type")
    private String messageType;

    @Column(name = "use_yn")
    private String useYn;

    @Column(name = "important_message_use_yn")
    private String importantMessageUseYn;
}
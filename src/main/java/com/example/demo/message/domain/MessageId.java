package com.example.demo.message.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class MessageId implements Serializable {

    @Column(name = "message_id")
    private String messageId;

    @Column(name = "language_code")
    private String languageCode;

    @Column(name = "important_message_type_code")
    private String actionTypeCode;
}
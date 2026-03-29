package com.example.demo.message.repository;

import com.example.demo.message.domain.MessageEntity;
import com.example.demo.message.domain.MessageId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<MessageEntity, MessageId> {

    List<MessageEntity> findByUseYn(String useYn);

}
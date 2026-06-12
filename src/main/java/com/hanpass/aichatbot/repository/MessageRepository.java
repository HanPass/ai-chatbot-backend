package com.hanpass.aichatbot.repository;

import com.hanpass.aichatbot.model.ConversationEntity;
import com.hanpass.aichatbot.model.MessageEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<MessageEntity, UUID> {

    List<MessageEntity> findByConversationOrderByCreatedAtAsc(ConversationEntity conversation);

    List<MessageEntity> findByConversationOrderByCreatedAtDesc(ConversationEntity conversation, Pageable pageable);
}

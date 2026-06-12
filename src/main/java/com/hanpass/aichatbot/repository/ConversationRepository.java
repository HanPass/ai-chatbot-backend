package com.hanpass.aichatbot.repository;

import com.hanpass.aichatbot.model.ConversationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ConversationRepository extends JpaRepository<ConversationEntity, UUID> {
}

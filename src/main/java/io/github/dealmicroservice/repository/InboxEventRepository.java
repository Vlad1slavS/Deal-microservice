package io.github.dealmicroservice.repository;

import io.github.dealmicroservice.model.entity.InboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Репозиторий для работы с Inbox данными
 */
@Repository
public interface InboxEventRepository extends JpaRepository<InboxEvent, Long> {

    Optional<InboxEvent> findInboxEventByMessageId(String messageId);

    @Modifying
    @Query("UPDATE InboxEvent SET processed = true, processedAt = :processedAt WHERE id = :id")
    void markAsProcessed(Long id, LocalDateTime processedAt);

    @Modifying
    @Query("UPDATE InboxEvent SET retryCount = :retryCount, errorMessage = :errorMessage, modifyDate = :modifyDate WHERE id = :id")
    void updateRetryInfo(Long id, Integer retryCount, String errorMessage, LocalDateTime modifyDate);

    @Query("SELECT MAX(ie.version) FROM InboxEvent ie WHERE ie.aggregateId = :aggregateId AND ie.processed = true AND ie.version IS NOT NULL")
    Optional<Long> findMaxProcessedVersionByAggregateId(String aggregateId);

}

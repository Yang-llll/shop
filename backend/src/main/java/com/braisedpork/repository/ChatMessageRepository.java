package com.braisedpork.repository;

import com.braisedpork.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findBySenderIdAndReceiverIdOrderByCreatedAtAsc(Long senderId, Long receiverId);
    List<ChatMessage> findByReceiverIdAndSenderIdOrderByCreatedAtAsc(Long receiverId, Long senderId);

    @Query("SELECT m FROM ChatMessage m WHERE (m.senderId = ?1 AND m.receiverId = ?2) OR (m.senderId = ?2 AND m.receiverId = ?1) ORDER BY m.createdAt ASC")
    List<ChatMessage> findConversation(Long userId1, Long userId2);

    @Query("SELECT m FROM ChatMessage m WHERE m.receiverId = ?1 AND m.isRead = false")
    List<ChatMessage> findUnreadByReceiverId(Long receiverId);

    @Modifying
    @Transactional
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE m.senderId = ?1 AND m.receiverId = ?2 AND m.isRead = false")
    void markAsRead(Long senderId, Long receiverId);

    @Query(value = "SELECT CASE WHEN sender_id = ?1 THEN receiver_id ELSE sender_id END AS partner_id, " +
            "MAX(created_at) AS latest FROM chat_message " +
            "WHERE sender_id = ?1 OR receiver_id = ?1 " +
            "GROUP BY CASE WHEN sender_id = ?1 THEN receiver_id ELSE sender_id END " +
            "ORDER BY latest DESC", nativeQuery = true)
    List<Object[]> findChatPartners(Long userId);
}

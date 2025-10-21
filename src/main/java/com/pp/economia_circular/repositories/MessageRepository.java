package com.pp.economia_circular.repositories;

import com.pp.economia_circular.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    List<Message> findBySender_Id(Long senderId);
    
    List<Message> findByReceiver_Id(Long receiverId);
    
    List<Message> findBySender_IdAndReceiver_Id(Long senderId, Long receiverId);
    
    List<Message> findByArticle_Id(Long articleId);
    
    List<Message> findByStatus(Message.MessageStatus status);
    
    @Query("SELECT m FROM Message m WHERE " +
           "(m.sender.id = :userId OR m.receiver.id = :userId) AND " +
           "m.status != 'DELETED' " +
           "ORDER BY m.createdAt DESC")
    List<Message> findConversationsByUser(@Param("userId") Long userId);
    
    @Query("SELECT m FROM Message m WHERE " +
           "((m.sender.id = :userId1 AND m.receiver.id = :userId2) OR " +
           "(m.sender.id = :userId2 AND m.receiver.id = :userId1)) AND " +
           "m.status != 'DELETED' " +
           "ORDER BY m.createdAt ASC")
    List<Message> findConversationBetweenUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiver.id = :userId AND m.status = 'UNREAD'")
    Long countUnreadMessagesByUser(@Param("userId") Long userId);
    
    @Query("SELECT m FROM Message m WHERE m.receiver.id = :userId AND m.status = 'UNREAD'")
    List<Message> findUnreadMessagesByUser(@Param("userId") Long userId);
}

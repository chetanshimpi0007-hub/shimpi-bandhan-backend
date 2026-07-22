package com.shimpimilan.repository;

import com.shimpimilan.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;

import java.util.List;
import java.util.Map;


@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatRoomIdOrderBySentAtAsc(Long chatRoomId);
    void deleteByChatRoomId(Long chatRoomId);
    long countByChatRoomId(Long chatRoomId);
    
    long countByIsDeleted(boolean isDeleted);

    @Query("SELECT m FROM ChatMessage m WHERE (:chatRoomId IS NULL OR m.chatRoom.id = :chatRoomId) " +
           "AND (:content IS NULL OR m.content LIKE %:content%) " +
           "AND (cast(:startDate as date) IS NULL OR m.sentAt >= :startDate) " +
           "AND (cast(:endDate as date) IS NULL OR m.sentAt <= :endDate) " +
           "AND (:isDeleted IS NULL OR m.isDeleted = :isDeleted)")
    Page<ChatMessage> findWithFilters(@Param("chatRoomId") Long chatRoomId, 
                                      @Param("content") String content, 
                                      @Param("startDate") LocalDateTime startDate, 
                                      @Param("endDate") LocalDateTime endDate, 
                                      @Param("isDeleted") Boolean isDeleted, 
                                      Pageable pageable);

    @Query(value = "SELECT CAST(m.sent_at AS DATE) AS date, COUNT(*) AS messages " +
                   "FROM chat_messages m WHERE m.sent_at BETWEEN :start AND :end " +
                   "GROUP BY CAST(m.sent_at AS DATE) ORDER BY date", nativeQuery = true)
    List<Map<String, Object>> countByDayBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}

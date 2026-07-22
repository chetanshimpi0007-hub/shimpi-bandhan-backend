package com.shimpimilan.repository;

import com.shimpimilan.model.ChatRoom;
import com.shimpimilan.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByGroomAndBride(User groom, User bride);
    boolean existsByGroomAndBride(User groom, User bride);

    @Query("SELECT r FROM ChatRoom r WHERE (:search IS NULL OR r.groom.phone LIKE %:search% OR r.bride.phone LIKE %:search%) " +
           "AND (:roomId IS NULL OR r.id = :roomId)")
    Page<ChatRoom> findWithFilters(@Param("search") String search, @Param("roomId") Long roomId, Pageable pageable);
}

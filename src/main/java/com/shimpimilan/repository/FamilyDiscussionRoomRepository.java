package com.shimpimilan.repository;

import com.shimpimilan.model.FamilyDiscussionRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FamilyDiscussionRoomRepository extends JpaRepository<FamilyDiscussionRoom, Long> {
    Optional<FamilyDiscussionRoom> findByChatRoomId(Long chatRoomId);
}

package com.shimpimilan.repository;

import com.shimpimilan.model.FamilyDiscussionMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FamilyDiscussionMemberRepository extends JpaRepository<FamilyDiscussionMember, Long> {
    List<FamilyDiscussionMember> findByRoomId(Long roomId);
    void deleteByRoomIdAndUserId(Long roomId, Long userId);
    boolean existsByRoomIdAndUserId(Long roomId, Long userId);
}

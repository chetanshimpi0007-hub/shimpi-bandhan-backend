package com.shimpimilan.repository;

import com.shimpimilan.model.FamilyMeeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FamilyMeetingRepository extends JpaRepository<FamilyMeeting, Long> {
    List<FamilyMeeting> findByRoomId(Long roomId);
}

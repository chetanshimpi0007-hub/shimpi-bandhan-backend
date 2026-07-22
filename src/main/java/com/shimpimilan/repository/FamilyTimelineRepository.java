package com.shimpimilan.repository;

import com.shimpimilan.model.FamilyTimeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FamilyTimelineRepository extends JpaRepository<FamilyTimeline, Long> {
    List<FamilyTimeline> findByRoomIdOrderByEventDateAsc(Long roomId);
}

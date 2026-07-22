package com.shimpimilan.repository;

import com.shimpimilan.model.FamilyMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FamilyMessageRepository extends JpaRepository<FamilyMessage, Long> {
    List<FamilyMessage> findByRoomIdOrderBySentAtAsc(Long roomId);
}

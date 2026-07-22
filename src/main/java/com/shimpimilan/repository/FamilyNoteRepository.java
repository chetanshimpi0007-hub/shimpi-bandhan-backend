package com.shimpimilan.repository;

import com.shimpimilan.model.FamilyNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FamilyNoteRepository extends JpaRepository<FamilyNote, Long> {
    List<FamilyNote> findByRoomIdAndAuthorId(Long roomId, Long authorId);
}

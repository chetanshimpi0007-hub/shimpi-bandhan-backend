package com.shimpimilan.repository.melava;

import com.shimpimilan.model.melava.MelavaVolunteer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MelavaVolunteerRepository extends JpaRepository<MelavaVolunteer, Long> {
    List<MelavaVolunteer> findByMelavaId(Long melavaId);
}

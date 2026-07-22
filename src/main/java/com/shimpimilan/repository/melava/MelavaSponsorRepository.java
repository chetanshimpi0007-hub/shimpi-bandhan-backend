package com.shimpimilan.repository.melava;

import com.shimpimilan.model.melava.MelavaSponsor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MelavaSponsorRepository extends JpaRepository<MelavaSponsor, Long> {
    List<MelavaSponsor> findByMelavaId(Long melavaId);
}

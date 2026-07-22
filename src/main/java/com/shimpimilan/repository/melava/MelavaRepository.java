package com.shimpimilan.repository.melava;

import com.shimpimilan.model.melava.Melava;
import com.shimpimilan.model.melava.MelavaStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MelavaRepository extends JpaRepository<Melava, Long> {
    List<Melava> findByStatus(MelavaStatus status);
}

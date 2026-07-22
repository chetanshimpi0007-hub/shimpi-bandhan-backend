package com.shimpimilan.repository.melava;

import com.shimpimilan.model.melava.MelavaGallery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MelavaGalleryRepository extends JpaRepository<MelavaGallery, Long> {
    List<MelavaGallery> findByMelavaId(Long melavaId);
}

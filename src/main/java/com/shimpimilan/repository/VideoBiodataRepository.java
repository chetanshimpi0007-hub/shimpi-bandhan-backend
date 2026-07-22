package com.shimpimilan.repository;

import com.shimpimilan.model.VideoBiodata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VideoBiodataRepository extends JpaRepository<VideoBiodata, Long> {
    Optional<VideoBiodata> findByUserId(Long userId);
}

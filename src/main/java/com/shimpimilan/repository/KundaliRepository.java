package com.shimpimilan.repository;

import com.shimpimilan.model.Kundali;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KundaliRepository extends JpaRepository<Kundali, Long> {
    Optional<Kundali> findByUserId(Long userId);
}

package com.shimpimilan.repository;

import com.shimpimilan.model.Interest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterestRepository extends JpaRepository<Interest, Long> {
    Optional<Interest> findBySenderIdAndReceiverId(Long senderId, Long receiverId);
    List<Interest> findByReceiverId(Long receiverId);
    List<Interest> findBySenderId(Long senderId);

    @org.springframework.data.jpa.repository.Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END FROM Interest i " +
           "WHERE ((i.sender.id = :userA AND i.receiver.id = :userB) OR (i.sender.id = :userB AND i.receiver.id = :userA)) " +
           "AND i.status = :status")
    boolean existsMutualInterest(@org.springframework.data.repository.query.Param("userA") Long userA, 
                                 @org.springframework.data.repository.query.Param("userB") Long userB, 
                                 @org.springframework.data.repository.query.Param("status") com.shimpimilan.model.InterestStatus status);

    @org.springframework.data.jpa.repository.Query(value =
        "SELECT YEAR(i.created_at) AS \"year\", MONTH(i.created_at) AS \"month\", " +
        "COUNT(*) AS sent, SUM(CASE WHEN i.status = 'ACCEPTED' THEN 1 ELSE 0 END) AS accepted " +
        "FROM interests i WHERE i.created_at BETWEEN :start AND :end " +
        "GROUP BY YEAR(i.created_at), MONTH(i.created_at) ORDER BY YEAR(i.created_at), MONTH(i.created_at)", nativeQuery = true)
    java.util.List<java.util.Map<String, Object>> countByMonthBetween(
        @org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start,
        @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end);
}

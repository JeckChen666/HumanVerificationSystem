package com.jeckchen.humanverificationsystem.repository;

import com.jeckchen.humanverificationsystem.pojo.LogRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LogRecordRepository extends JpaRepository<LogRecord, Long> {

    @Query("SELECT l FROM LogRecord l " +
            "WHERE (:sessionId IS NULL OR l.sessionId = :sessionId) " +
            "AND (:ip IS NULL OR l.ip = :ip) " +
            "AND (:url IS NULL OR l.url = :url) " +
            "AND (:country IS NULL OR l.country = :country) " +
            "AND (:city IS NULL OR l.city = :city) " +
            "AND (:deviceInfo IS NULL OR l.deviceInfo LIKE CONCAT('%', :deviceInfo, '%')) " +
            "AND (:operation IS NULL OR l.operation LIKE CONCAT('%', :operation, '%')) " +
            "AND (:firstVisit IS NULL OR l.firstVisit = :firstVisit) " +
            "AND (:startDate IS NULL OR l.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR l.createdAt <= :endDate)")
    Page<LogRecord> findAllWithFilters(
            @Param("sessionId") String sessionId,
            @Param("ip") String ip,
            @Param("url") String url,
            @Param("country") String country,
            @Param("city") String city,
            @Param("deviceInfo") String deviceInfo,
            @Param("operation") String operation,
            @Param("firstVisit") String firstVisit,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);


    @Query("SELECT l FROM LogRecord l " +
            "WHERE (:sessionId IS NULL OR l.sessionId = :sessionId) " +
            "AND (:ip IS NULL OR l.ip = :ip) " +
            "AND (:url IS NULL OR l.url = :url) " +
            "AND (:country IS NULL OR l.country = :country) " +
            "AND (:city IS NULL OR l.city = :city) " +
            "AND (:deviceInfo IS NULL OR l.deviceInfo LIKE CONCAT('%', :deviceInfo, '%')) " +
            "AND (:operation IS NULL OR l.operation LIKE CONCAT('%', :operation, '%')) " +
            "AND (:firstVisit IS NULL OR l.firstVisit = :firstVisit) " +
            "AND (:startDate IS NULL OR l.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR l.createdAt <= :endDate)")
    List<LogRecord> findAllWithFilters(
            @Param("sessionId") String sessionId,
            @Param("ip") String ip,
            @Param("url") String url,
            @Param("country") String country,
            @Param("city") String city,
            @Param("deviceInfo") String deviceInfo,
            @Param("operation") String operation,
            @Param("firstVisit") String firstVisit,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT l FROM LogRecord l WHERE l.createdAt >= :time")
    List<LogRecord> findAllAfterTime(LocalDateTime time);
}


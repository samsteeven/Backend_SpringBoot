package com.app.easypharma_backend.domain.search.repository;

import com.app.easypharma_backend.domain.search.entity.SearchLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

@Repository
public interface SearchLogRepository extends JpaRepository<SearchLog, UUID> {

    @Query("SELECT s.query, COUNT(s) as count FROM SearchLog s GROUP BY s.query ORDER BY count DESC")
    List<Object[]> findTopSearchedQueries(Pageable pageable);

    void deleteByUser(com.app.easypharma_backend.domain.auth.entity.User user);
}

package com.techjournal.repository;

import com.techjournal.entity.SentNews;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SentNewsRepository extends JpaRepository<SentNews, Long> {

    Optional<SentNews> findByLink(String link);

    boolean existsByLink(String link);
}

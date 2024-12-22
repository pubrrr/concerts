package com.bierchitekt.concerts.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ConcertRepository extends JpaRepository<ConcertEntity, String> {
    List<ConcertEntity> findAllByDateBefore(LocalDate now);

    List<ConcertEntity> findByNotifiedOrderByDate(boolean notified);

    List<ConcertEntity> findByDateAfterOrderByDate(LocalDate date);

    List<ConcertEntity> findByDateAfterAndDateBeforeOrderByDate(LocalDate now, LocalDate localDate);

    List<ConcertEntity> findByTitle(String title);

    List<ConcertEntity> findByTitleAndDate(String title, LocalDate date);

    List<ConcertEntity> findByGenreIn(Set<String> s);

    Optional<ConcertEntity> findByLink(String url);
}

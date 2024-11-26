package com.bierchitekt.concerts.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ConcertRepository extends JpaRepository<ConcertEntity, Long> {
    List<ConcertEntity> findAllByDateBefore(LocalDate now);

    List<ConcertEntity> findAllByPriceIsNull();

    List<ConcertEntity> findByNotified(boolean notified);

    List<ConcertEntity> findAllByOrderByDate();

    List<ConcertEntity> findByDateAfterAndDateBeforeOrderByDate(LocalDate now, LocalDate localDate);

    List<ConcertEntity> findByTitle(String title);

    List<ConcertEntity> findByTitleAndDate(String title, LocalDate date);
}

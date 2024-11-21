package com.bierchitekt.concerts.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ConcertRepository extends JpaRepository<ConcertEntity, String> {
    List<ConcertEntity> findAllByDateBefore(LocalDate now);

    List<ConcertEntity> findAllByPriceIsNull();
}

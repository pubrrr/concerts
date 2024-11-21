package com.bierchitekt.concerts;

import com.bierchitekt.concerts.persistence.ConcertEntity;
import com.bierchitekt.concerts.persistence.ConcertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ConcertService {
    private final ConcertRepository concertRepository;

    public void deleteOldConcerts(){
        List<ConcertEntity> allByDateBefore = concertRepository.findAllByDateBefore(LocalDate.now());
        log.info("deleting {} old concerts", allByDateBefore.size());
        if (!allByDateBefore.isEmpty()) {
            concertRepository.deleteAll(allByDateBefore);
        }
    }

}

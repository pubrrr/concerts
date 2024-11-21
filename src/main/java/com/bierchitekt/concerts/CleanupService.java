package com.bierchitekt.concerts;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class CleanupService {
    private final ConcertService concertService;

    @Scheduled(cron = "15 5 * * *")
    public void deleteOldConcerts(){
        concertService.deleteOldConcerts();
    }
}

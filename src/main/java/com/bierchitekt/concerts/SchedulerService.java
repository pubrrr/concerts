package com.bierchitekt.concerts;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class SchedulerService {
    private final ConcertService concertService;

    @Scheduled(cron = "* 15 5 * * * ")
    public void deleteOldConcerts() {
        concertService.deleteOldConcerts();
    }

    @Scheduled(cron = "${notify.cron}")
    public void notifyNewConcerts() {
        concertService.notifyNewMetalConcerts();
    }

  //  @Scheduled(cron = "${getConcerts.cron}")
    @PostConstruct
    public void getNewConcerts() {
        concertService.getNewConcerts();
    }

}

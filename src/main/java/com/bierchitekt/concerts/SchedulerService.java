package com.bierchitekt.concerts;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class SchedulerService {
    private final ConcertService concertService;

    @Scheduled(cron = "1 15 5 3 * * ")
    public void deleteOldConcerts() {
        concertService.deleteOldConcerts();
    }

    @Scheduled(cron = "${notify.cron}")
    public void notifyNewMetalConcerts() {
        concertService.notifyNewConcerts();
    }

    @Scheduled(cron = "${getConcerts.cron}")
    public void getNewConcerts() {
        concertService.getNewConcerts();
    }

    @Scheduled(cron = "${notify-metal-concerts.cron}")
    public void notifyNextWeekMetalConcerts() {
        concertService.notifyNextWeekMetalConcerts();
    }

}

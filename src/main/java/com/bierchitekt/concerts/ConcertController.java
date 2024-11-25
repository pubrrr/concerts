package com.bierchitekt.concerts;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ConcertController {

    private final ConcertService concertService;

    @PostMapping("/update-concerts")
    public void getConcerts() {
        concertService.getNewConcerts();
    }

    @PostMapping("/notify-new-concerts")
    public void notifyNewConcerts() {
        concertService.notifyNewMetalConcerts();
    }

    @PostMapping("/notify-nextweek-concerts")
    public void notifyNextWeekConcerts() {
        concertService.notifyNextWeekMetalConcerts();
    }

}

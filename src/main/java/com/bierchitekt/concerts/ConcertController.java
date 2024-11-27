package com.bierchitekt.concerts;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @GetMapping("/get-next-week-metal-concerts")
    public List<ConcertDTO> getNextWeekConcerts(){
        return concertService.getNextWeekConcerts();
    }

}

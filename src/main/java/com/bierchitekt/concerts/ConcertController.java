package com.bierchitekt.concerts;

import com.bierchitekt.concerts.persistence.ConcertEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
        concertService.notifyNewConcerts();
    }

    @PostMapping("/notify-nextweek-metal-concerts")
    public void notifyNextWeekMetalConcerts() {
        concertService.notifyNextWeekMetalConcerts();
    }


    @PostMapping("/notify-nextweek-punk-concerts")
    public void notifyNextWeekPunkConcerts() {
        concertService.notifyNextWeekPunkConcerts();
    }

    @PostMapping("/notify-nextweek-rock-concerts")
    public void notifyNextWeekRockConcerts() {
        concertService.notifyNextWeekRockConcerts();
    }

    @GetMapping("/next-week-metal-concerts")
    public List<ConcertDTO> getNextWeekConcerts() {
        return concertService.getNextWeekConcerts();
    }

    @GetMapping("/concerts-without-genre")
    public List<ConcertEntity> getConcertsWithoutGenre() {
        return concertService.getConcertsWithoutGenre();
    }

    @PutMapping("/update-genre")
    public void updateGenre(@RequestParam String id, @RequestParam String genre) {
        concertService.updateConcertGenre(id, genre);
    }

}

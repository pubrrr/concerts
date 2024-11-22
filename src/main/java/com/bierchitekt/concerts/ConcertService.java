package com.bierchitekt.concerts;

import com.bierchitekt.concerts.persistence.ConcertEntity;
import com.bierchitekt.concerts.persistence.ConcertRepository;
import com.bierchitekt.concerts.spotify.SpotifyClient;
import com.bierchitekt.concerts.venues.BackstageService;
import com.bierchitekt.concerts.venues.StromService;
import com.bierchitekt.concerts.venues.ZenithService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ConcertService {
    private final ConcertRepository concertRepository;
    private final TelegramService telegramService;

    private final BackstageService backstageService;
    private final ZenithService zenithService;
    private final StromService stromService;
    private final SpotifyClient spotifyClient;

    public void deleteOldConcerts() {
        List<ConcertEntity> allByDateBefore = concertRepository.findAllByDateBefore(LocalDate.now());
        log.info("deleting {} old concerts", allByDateBefore.size());
        if (!allByDateBefore.isEmpty()) {
            concertRepository.deleteAll(allByDateBefore);
        }
    }

    public void notifyNewMetalConcerts() {
        log.info("notifying for new concerts");
        for (ConcertEntity concertEntity : concertRepository.findByNotified(false)) {
            List<String> genres = concertEntity.getGenre();
            for (String genre : genres) {
                if (genre.toLowerCase().contains("rock") || genre.toLowerCase().contains("metal")) {
                    String message = concertEntity.getTitle() + " \n" +
                            "playing at " + concertEntity.getLocation() + " \n" +
                            "on " + concertEntity.getDate() + " \n" +
                            "price is " + concertEntity.getPrice() + " \n" +
                            "genre is " + concertEntity.getGenre() + " \n" +
                            concertEntity.getLink();

                    telegramService.sendMessage(message);
                    concertEntity.setNotified(true);
                    concertRepository.save(concertEntity);
                    break;
                }
            }
        }
    }

    public void getNewConcerts() {
        log.info("starting");

        List<ConcertDTO> allConcerts = new ArrayList<>();

        allConcerts.addAll(getStromKonzerts());
        allConcerts.addAll(getBackstageConcerts());

        allConcerts.addAll(getZenithConcerts());

        List<ConcertEntity> concertEntities = new ArrayList<>();
        log.info("found {} concerts, saving now", allConcerts.size());
        for (ConcertDTO concertDTO : allConcerts) {
            if (concertRepository.findById(concertDTO.title()).isEmpty()) {
                log.info("new concert found. Title: {}", concertDTO.title());
                ConcertEntity concertEntity = ConcertEntity.builder()
                        .date(concertDTO.date())
                        .genre(concertDTO.genre())
                        .title(concertDTO.title())
                        .location(concertDTO.location())
                        .link(concertDTO.link())
                        .price(concertDTO.price())
                        .build();
                concertEntities.add(concertEntity);
            }
        }
        concertRepository.saveAll(concertEntities);
    }

    List<ConcertDTO> getBackstageConcerts() {
        List<ConcertDTO> backstageConcerts = new ArrayList<>();
        List<ConcertDTO> concerts = backstageService.getConcerts();

        concerts.forEach(concert -> {
            if (concertRepository.findById(concert.title()).isEmpty()) { // new concert, query for price
                String price = backstageService.getPrice(concert.link());
                backstageConcerts.add(new ConcertDTO(concert.title(), concert.date(), concert.link(), concert.genre(), concert.location(), price));
            }
        });
        return backstageConcerts;
    }

    List<ConcertDTO> getZenithConcerts() {
        List<ConcertDTO> zenithConcerts = new ArrayList<>();
        zenithService.getConcerts().forEach(concert -> {
            if (concertRepository.findById(concert.title()).isEmpty()) { // new concert, query for price
                List<String> genres = spotifyClient.getGenres(concert.title());
                zenithConcerts.add(new ConcertDTO(concert.title(), concert.date(), concert.link(), genres, concert.location(), null));

            }
        });
        return zenithConcerts;
    }

    List<ConcertDTO> getStromKonzerts() {
        List<ConcertDTO> stromConcerts = new ArrayList<>();
        try {
            for (ConcertDTO stromConcert : stromService.getConcerts()) {
                if (concertRepository.findById(stromConcert.title()).isEmpty()) { // new Concert found, need to get date and genre
                    LocalDate date = stromService.getDate(stromConcert.link());
                    List<String> genres = spotifyClient.getGenres(stromConcert.title());
                    ConcertDTO concertDTO = new ConcertDTO(stromConcert.title(), date, stromConcert.link(), genres, "Strom", null);
                    stromConcerts.add(concertDTO);
                }
            }
            return stromConcerts;
        } catch (Exception ex) {
            log.warn("error getting Strom concerts", ex);
        }
        return List.of();
    }
}


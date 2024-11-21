package com.bierchitekt.concerts;

import com.bierchitekt.concerts.persistence.ConcertEntity;
import com.bierchitekt.concerts.persistence.ConcertRepository;
import com.bierchitekt.concerts.venues.BackstageService;
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

    public void deleteOldConcerts() {
        List<ConcertEntity> allByDateBefore = concertRepository.findAllByDateBefore(LocalDate.now());
        log.info("deleting {} old concerts", allByDateBefore.size());
        if (!allByDateBefore.isEmpty()) {
            concertRepository.deleteAll(allByDateBefore);
        }
    }

    public void notifyNewConcerts() {
        log.info("notifying for new concerts");
        for (ConcertEntity concertEntity : concertRepository.findByNotified(false)) {
            String message = concertEntity.getTitle() + " \n" +
                    "playing at " + concertEntity.getLocation() + " \n" +
                    "on " + concertEntity.getDate() + " \n" +
                    "price is " + concertEntity.getPrice() + " \n" +
                    "genre is " + concertEntity.getGenre() + " \n" +
                    concertEntity.getLink();

            telegramService.sendMessage(message);
            concertEntity.setNotified(true);
            concertRepository.save(concertEntity);
        }
    }

    public void getNewConcerts() {
        log.info("starting");

        List<ConcertDTO> allConcerts = backstageService.getConcerts();
        allConcerts.addAll(zenithService.getConcerts());

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
                        .build();
                concertEntities.add(concertEntity);
            }
        }
        concertRepository.saveAll(concertEntities);

        // check if all backstage concerts have prices.
        concertRepository.findAllByPriceIsNull().forEach(concertEntity -> {
            if (concertEntity.getLocation().contains("BACKSTAGE")) {
                String price = backstageService.getPrice(concertEntity.getLink());
                concertEntity.setPrice(price);
                concertRepository.save(concertEntity);
            }
        });
    }
}


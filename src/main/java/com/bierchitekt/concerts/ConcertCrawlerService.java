package com.bierchitekt.concerts;


import com.bierchitekt.concerts.persistence.ConcertEntity;
import com.bierchitekt.concerts.persistence.ConcertRepository;
import com.bierchitekt.concerts.venues.BackstageService;
import com.bierchitekt.concerts.venues.ZenithService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConcertCrawlerService {


    private final ConcertRepository concertRepository;
    private final BackstageService backstageService;
    private final ZenithService zenithService;
    private final TelegramService telegramService;

    @PostConstruct
    public void saveConcerts() throws XPathExpressionException, IOException, ParserConfigurationException {
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

        List<ConcertEntity> byNotified = concertRepository.findByNotified(false);
        for (int i = 0; i < 5; i++) {
            ConcertEntity concertEntity = byNotified.get(i);
            String message = concertEntity.getTitle() + " \n" +
                    "playing at " + concertEntity.getLocation() + " \n" +
                    "on " + concertEntity.getDate() + " \n" +
                    "price is " + concertEntity.getPrice() + " \n" +
                    "link is " + concertEntity.getLink();

            telegramService.sendMessage(message);
            concertEntity.setNotified(true);
            concertRepository.save(concertEntity);
        }
    }
}
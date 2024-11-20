package com.bierchitekt.concerts;


import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConcertCrawlerService {


    private final ConcertRepository concertRepository;
    private final Backstage backstage;

    @PostConstruct
    public void saveConcerts() throws XPathExpressionException, IOException, ParserConfigurationException {
        log.info("cleaning up old concerts");
        deleteOldConcerts();
        log.info("starting");

        List<ConcertDTO> backstageConcerts = backstage.getConcerts();

        List<ConcertEntity> concertEntities = new ArrayList<>();
        log.info("found {} concerts, saving now", backstageConcerts.size());
        for (ConcertDTO concertDTO : backstageConcerts) {
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

        // check if all concerts have prices.
        concertRepository.findAllByPriceIsNull().forEach(concertEntity -> {
            String price = backstage.getPrice(concertEntity.getLink());
            concertEntity.setPrice(price);
            concertRepository.save(concertEntity);
        });

        log.info("all");
    }

    void deleteOldConcerts() {
        List<ConcertEntity> allByDateBefore = concertRepository.findAllByDateBefore(LocalDate.now());
        log.info("deleting {} old concerts", allByDateBefore.size());
        if (!allByDateBefore.isEmpty()) {
            concertRepository.deleteAll(allByDateBefore);
        }
    }

}
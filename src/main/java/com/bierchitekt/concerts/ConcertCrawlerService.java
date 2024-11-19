package com.bierchitekt.concerts;


import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

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
    private final Crawler crawler;

    @PostConstruct
    public void saveConcerts() throws XPathExpressionException, IOException, ParserConfigurationException, SAXException {
        log.info("starting");
        List<ConcertDTO> backstageConcerts = crawler.getBackstageConcerts();
        List<ConcertEntity> concertEntities = new ArrayList<>();
        log.info("found {} concerts, saving now", backstageConcerts.size());
        for (ConcertDTO concertDTO : backstageConcerts) {
            ConcertEntity concertEntity = ConcertEntity.builder().date(concertDTO.date()).genre(concertDTO.genre()).title(concertDTO.title()).location(concertDTO.location()).build();
            concertEntities.add(concertEntity);
        }
        concertRepository.saveAll(concertEntities);
    }

}
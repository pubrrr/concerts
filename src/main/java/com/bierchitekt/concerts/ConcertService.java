package com.bierchitekt.concerts;

import com.bierchitekt.concerts.genre.GenreService;
import com.bierchitekt.concerts.persistence.ConcertEntity;
import com.bierchitekt.concerts.persistence.ConcertRepository;
import com.bierchitekt.concerts.venues.BackstageService;
import com.bierchitekt.concerts.venues.FeierwerkService;
import com.bierchitekt.concerts.venues.MuffathalleService;
import com.bierchitekt.concerts.venues.OlympiaparkService;
import com.bierchitekt.concerts.venues.StromService;
import com.bierchitekt.concerts.venues.ZenithService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Service
public class ConcertService {
    private final ConcertRepository concertRepository;
    private final TelegramService telegramService;

    private final BackstageService backstageService;
    private final ZenithService zenithService;
    private final StromService stromService;
    private final MuffathalleService muffathalleService;
    private final FeierwerkService feierwerkService;
    private final OlympiaparkService olympiaparkService;


    private final GenreService genreService;

    public void deleteOldConcerts() {
        List<ConcertEntity> allByDateBefore = concertRepository.findAllByDateBefore(LocalDate.now());
        if (!allByDateBefore.isEmpty()) {
            log.info("deleting {} old concerts", allByDateBefore.size());
            concertRepository.deleteAll(allByDateBefore);
        }
    }

    public void notifyNewMetalConcerts() {
        log.info("notifying for new concerts");
        for (ConcertEntity concertEntity : concertRepository.findByNotified(false)) {
            Set<String> genres = concertEntity.getGenre();
            for (String genre : genres) {
                if (genre.toLowerCase().contains("rock") || genre.toLowerCase().contains("metal") || genre.toLowerCase().contains("punk")) {
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

    public void notifyNextWeekMetalConcerts() {
        for (ConcertEntity concertEntity : concertRepository.findByDateAfterAndDateBeforeOrderByDate(LocalDate.now(), LocalDate.now().plusDays(8))) {

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd LLLL yyyy");

            Set<String> genres = concertEntity.getGenre();
            for (String genre : genres) {
                if (genre.toLowerCase().contains("rock") || genre.toLowerCase().contains("metal") || genre.toLowerCase().contains("punk")) {
                    String message = "<b>" + concertEntity.getTitle() + "</b> \n" +
                            "playing at <b>" + concertEntity.getLocation() + "</b> \n" +
                            "on " + concertEntity.getDate().format(formatter) + " \n" +
                            "genre is " + concertEntity.getGenre() + " \n" +

                            "playing at <a href=\"" + concertEntity.getLink() + "\">" + concertEntity.getLocation() + "</a>";


                    telegramService.sendMessage(message);
                    break;
                }
            }
        }
    }

    public void getNewConcerts() {
        log.info("starting");

        List<ConcertDTO> allConcerts = new ArrayList<>();
        allConcerts.addAll(getZenithConcerts());

        allConcerts.addAll(getStromConzerts());
        allConcerts.addAll(getOlympiaparkConcerts());
        allConcerts.addAll(getBackstageConcerts());
        allConcerts.addAll(getMuffathalleConcerts());
        allConcerts.addAll(getFeierwerkConcerts());

        log.info("found {} concerts, saving now", allConcerts.size());
        for (ConcertDTO concertDTO : allConcerts) {
            if (concertRepository.findByTitleAndDate(concertDTO.title(), concertDTO.date()).isEmpty()) {
                log.info("new concert found. Title: {}", concertDTO.title());
                ConcertEntity concertEntity = ConcertEntity.builder()
                        .date(concertDTO.date())
                        .genre(concertDTO.genre())
                        .title(concertDTO.title())
                        .location(concertDTO.location())
                        .link(concertDTO.link())
                        .price(concertDTO.price())
                        .build();
                concertRepository.save(concertEntity);
            }
        }

    }

    private Collection<ConcertDTO> getOlympiaparkConcerts() {
        List<ConcertDTO> olypiaparkConcerts = new ArrayList<>();
        olympiaparkService.getConcerts().forEach(concert -> {
            if (concertRepository.findByTitleAndDate(concert.title(), concert.date()).isEmpty()) { // new concert, query for price
                Set<String> genres = genreService.getGenres(concert.title());
                olypiaparkConcerts.add(new ConcertDTO(concert.title(), concert.date(), concert.link(), genres, concert.location(), null));
            }
        });
        return olypiaparkConcerts;
    }

    public Collection<ConcertDTO> getFeierwerkConcerts() {
        List<ConcertDTO> feierwerkConcerts = new ArrayList<>();
        feierwerkService.getConcerts().forEach(concert -> {
            if (concertRepository.findByTitleAndDate(concert.title(), concert.date()).isEmpty()) { // new concert, query for price
                String price = feierwerkService.getPrice(concert.link());
                feierwerkConcerts.add(new ConcertDTO(concert.title(), concert.date(), concert.link(), concert.genre(), concert.location(), price));
            }
        });
        return feierwerkConcerts;
    }

    List<ConcertDTO> getBackstageConcerts() {
        List<ConcertDTO> backstageConcerts = new ArrayList<>();
        List<ConcertDTO> concerts = backstageService.getConcerts();

        concerts.forEach(concert -> {
            if (concertRepository.findByTitleAndDate(concert.title(), concert.date()).isEmpty()) { // new concert, query for price
                String price = backstageService.getPrice(concert.link());
                backstageConcerts.add(new ConcertDTO(concert.title(), concert.date(), concert.link(), concert.genre(), concert.location(), price));
            }
        });
        return backstageConcerts;
    }

    List<ConcertDTO> getZenithConcerts() {
        List<ConcertDTO> zenithConcerts = new ArrayList<>();
        zenithService.getConcerts().forEach(concert -> {
            if (concertRepository.findByTitle(concert.title()).isEmpty()) { // new concert, query for price
                Set<String> genres = genreService.getGenres(concert.title());
                zenithConcerts.add(new ConcertDTO(concert.title(), concert.date(), concert.link(), genres, concert.location(), null));

            }
        });
        return zenithConcerts;
    }

    List<ConcertDTO> getStromConzerts() {
        List<ConcertDTO> stromConcerts = new ArrayList<>();
        for (ConcertDTO concert : stromService.getConcerts()) {
            if (concertRepository.findByTitleAndDate(concert.title(), concert.date()).isEmpty()) {
                Set<String> genres = genreService.getGenres(concert.title());
                ConcertDTO concertDTO = new ConcertDTO(concert.title(), concert.date(), concert.link(), genres, "Strom", null);
                stromConcerts.add(concertDTO);
            }
        }
        return stromConcerts;

    }

    List<ConcertDTO> getMuffathalleConcerts() {
        List<ConcertDTO> muffatHalleConcerts = new ArrayList<>();

        for (ConcertDTO muffathalleConcert : muffathalleService.getConcerts()) {
            if (concertRepository.findByTitle(muffathalleConcert.title()).isEmpty()) { // new Concert found, need to get date and genre
                LocalDate date = muffathalleService.getDate(muffathalleConcert.link());
                Set<String> genres = genreService.getGenres(muffathalleConcert.title());
                ConcertDTO concertDTO = new ConcertDTO(muffathalleConcert.title(), date, muffathalleConcert.link(), genres, muffathalleConcert.location(), null);
                muffatHalleConcerts.add(concertDTO);
            }
        }
        return muffatHalleConcerts;

    }

    public void generateHtml() throws FileNotFoundException {
        String result = "<table>" +
                "  <tr>" +
                "    <th>Datum</th>" +
                "    <th>Band</th>" +
                "    <th>Genre</th>\n" +
                "    <th>Location</th>\n" +
                "  </tr>\" ";
        for (ConcertEntity concertEntity : concertRepository.findAllByOrderByDate()) {
            result += "<tr>";
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd LLLL yyyy");

            result += "<td>" + concertEntity.getDate().format(formatter) + "</td>";
            result += "<td><a href=" + concertEntity.getLink() + ">" + concertEntity.getTitle() + "</a></td>";

            result += "<td>" + concertEntity.getGenre() + "</td>";
            result += "<td>" + concertEntity.getLocation() + "</td>";
            result += "</tr>";
        }
        result += "</table>";
        try (PrintWriter out = new PrintWriter("result.html")) {
            out.println(result);
        }
    }
}


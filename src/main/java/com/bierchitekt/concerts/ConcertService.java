package com.bierchitekt.concerts;

import com.bierchitekt.concerts.genre.GenreService;
import com.bierchitekt.concerts.persistence.ConcertEntity;
import com.bierchitekt.concerts.persistence.ConcertRepository;
import com.bierchitekt.concerts.venues.BackstageService;
import com.bierchitekt.concerts.venues.FeierwerkService;
import com.bierchitekt.concerts.venues.Kult9Service;
import com.bierchitekt.concerts.venues.MuffathalleService;
import com.bierchitekt.concerts.venues.OlympiaparkService;
import com.bierchitekt.concerts.venues.StromService;
import com.bierchitekt.concerts.venues.Theaterfabrik;
import com.bierchitekt.concerts.venues.ZenithService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

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
    private final Theaterfabrik theaterfabrikService;
    private final Kult9Service kult9Service;

    private final ConcertMapper concertMapper;

    private final GenreService genreService;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd LLLL yyyy");


    public void notifyNewConcerts() {
        log.info("notifying for new concerts");
        TreeSet<ConcertEntity> newMetalConcerts = new TreeSet<>();
        TreeSet<ConcertEntity> newRockConcerts = new TreeSet<>();
        TreeSet<ConcertEntity> newPunkConcerts = new TreeSet<>();
        for (ConcertEntity concertEntity : concertRepository.findByNotified(false)) {
            Set<String> genres = concertEntity.getGenre();
            for (String genre : genres) {
                if (genre.toLowerCase().contains("rock")) {
                    newRockConcerts.add(concertEntity);
                }
                if (genre.toLowerCase().contains("metal")) {
                    newMetalConcerts.add(concertEntity);
                }
                if (genre.toLowerCase().contains("punk")) {
                    newPunkConcerts.add(concertEntity);
                }
            }
            concertEntity.setNotified(true);
            concertRepository.save(concertEntity);
        }


        notifyNewConcerts("Good news everyone! I found some new metal concerts for you\n\n", newMetalConcerts, "@MunichMetalConcerts");
        notifyNewConcerts("Good news everyone! I found some new rock concerts for you\n\n", newRockConcerts, "@MunichRockConcerts");
        notifyNewConcerts("Good news everyone! I found some new punk concerts for you\n\n", newPunkConcerts, "@MunichPunkConcerts");
    }

    private void notifyNewConcerts(String message, TreeSet<ConcertEntity> newConcerts, String channelName) {
        if (!newConcerts.isEmpty()) {
            for (ConcertEntity concert : newConcerts) {
                message += "<b>" + concert.getTitle() + "</b> \n" +
                        "on " + concert.getDate().format(formatter) + " \n" +
                        "genre is " + concert.getGenre() + " \n" +
                        "playing at <a href=\"" + concert.getLink() + "\">" + concert.getLocation() + "</a>\n\n";
            }
            telegramService.sendMessage(channelName, message);
        }
    }


    public void notifyNextWeekConcerts() {
        notifyNextWeekMetalConcerts();
        notifyNextWeekRockConcerts();
        notifyNextWeekPunkConcerts();
    }


    public void notifyNextWeekMetalConcerts() {
        notifyConcerts("metal", "@MunichMetalConcerts");
    }


    public void notifyNextWeekRockConcerts() {
        notifyConcerts("rock", "@MunichRockConcerts");
    }

    public void notifyNextWeekPunkConcerts() {
        notifyConcerts("punk", "@MunichPunkConcerts");
    }

    private void notifyConcerts(String genreName, String channelName) {
        TreeSet<ConcertEntity> concerts = new TreeSet<>();
        for (ConcertEntity concertEntity : concertRepository.findByDateAfterAndDateBeforeOrderByDate(LocalDate.now().minusDays(1), LocalDate.now().plusDays(8))) {

            Set<String> genres = concertEntity.getGenre();
            for (String genre : genres) {
                if (genre.toLowerCase().contains(genreName)) {
                    concerts.add(concertEntity);
                    break;
                }
            }
        }
        notifyNewConcerts("Upcoming " + genreName + " concerts for next week: \n\n", concerts, channelName);
    }

    public void getNewConcerts() {
        log.info("starting");

        List<ConcertDTO> allConcerts = new ArrayList<>();
        allConcerts.addAll(getOlympiaparkConcerts());
        allConcerts.addAll(getKult9Concerts());
        allConcerts.addAll(getZenithConcerts());
        allConcerts.addAll(getTheaterfabrikConcerts());
        allConcerts.addAll(getStromConcerts());
        allConcerts.addAll(getBackstageConcerts());
        allConcerts.addAll(getMuffathalleConcerts());
        allConcerts.addAll(getFeierwerkConcerts());

        log.info("found {} concerts, saving now", allConcerts.size());
        for (ConcertDTO concertDTO : allConcerts) {
            if (concertRepository.findByTitleAndDate(concertDTO.title(), concertDTO.date()).isEmpty()) {
                log.info("new concert found. Title: {}", concertDTO.title());
                ConcertEntity concertEntity = concertMapper.toConcertEntity(concertDTO);
                concertRepository.save(concertEntity);
            }
        }

    }

    private Collection<ConcertDTO> getKult9Concerts() {
        List<ConcertDTO> kult9Concerts = new ArrayList<>();
        kult9Service.getConcerts().forEach(concert -> {
            if (concertRepository.findByTitleAndDate(concert.title(), concert.date()).isEmpty()) {
                kult9Concerts.add(new ConcertDTO(concert.title(), concert.date(), concert.link(), concert.genre(), concert.location(), concert.price()));
            }
        });
        return kult9Concerts;
    }

    private Collection<ConcertDTO> getTheaterfabrikConcerts() {
        List<ConcertDTO> theaterfabrikConcerts = new ArrayList<>();
        theaterfabrikService.getConcerts().forEach(concert -> {
            if (concertRepository.findByTitleAndDate(concert.title(), concert.date()).isEmpty()) {
                Set<String> genres = genreService.getGenres(concert.title());
                theaterfabrikConcerts.add(new ConcertDTO(concert.title(), concert.date(), concert.link(), genres, concert.location(), concert.price()));
            }
        });
        return theaterfabrikConcerts;
    }

    private Collection<ConcertDTO> getOlympiaparkConcerts() {
        List<ConcertDTO> olypiaparkConcerts = new ArrayList<>();
        olympiaparkService.getConcerts().forEach(concert -> {
            if (concertRepository.findByTitleAndDate(concert.title(), concert.date()).isEmpty()) {
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

    List<ConcertDTO> getStromConcerts() {
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

        String tdOpenTag = "<td>";
        String tdCloseTag = "</td>";
        StringBuilder result = new StringBuilder("""
                  <!DOCTYPE html>
                  <html lang="de">
                  <head>
                  <meta http-equiv="Content-Type"
                        content="text/html; charset=utf-8"/>
                      <title>All Concerts in munich</title>
                  </head>
                
                  <table>
                     <tr>
                        <th>Datum</th>
                        <th>Band</th>
                        <th>Genre</th>
                        <th>Location</th>
                     </tr>
                """);
        for (ConcertEntity concertEntity : concertRepository.findByDateAfterOrderByDate(LocalDate.now().minusDays(1))) {
            result.append("<tr>\n");
            String title = concertEntity.getTitle();

            result.append(tdOpenTag).append(concertEntity.getDate().format(formatter)).append(tdCloseTag);
            result.append(tdOpenTag).append("<a href=\"").append(concertEntity.getLink()).append("\">").append(title).append("</a>").append(tdCloseTag);
            String genre = String.join(", ", concertEntity.getGenre());

            result.append(tdOpenTag).append(genre).append(tdCloseTag);
            result.append(tdOpenTag).append(concertEntity.getLocation()).append(tdCloseTag);
            result.append("</tr>");
        }
        result.append("</table></html>");
        try (PrintWriter out = new PrintWriter("result.html")) {
            out.println(result);
        }
    }

    public List<ConcertDTO> getNextWeekConcerts() {

        List<ConcertEntity> byDateAfterAndDateBeforeOrderByDate = concertRepository.findByDateAfterAndDateBeforeOrderByDate(LocalDate.now(), LocalDate.now().plusDays(8));

        return concertMapper.toConcertDto(byDateAfterAndDateBeforeOrderByDate);
    }

    public List<ConcertEntity> getConcertsWithoutGenre() {
        return concertRepository.findByGenreIn(Set.of("[]]"));
    }

    public void updateConcertGenre(String id, String genre) {
        Optional<ConcertEntity> byId = concertRepository.findById(id);
        if (byId.isEmpty()) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(404), "Unable to find resource");
        } else {
            ConcertEntity concertEntity = byId.get();
            concertEntity.setGenre(Set.of(genre));
            concertRepository.save(concertEntity);
        }
    }

    public void deleteOldConcerts() {
        List<ConcertEntity> allByDateBefore = concertRepository.findAllByDateBefore(LocalDate.now());
        if (!allByDateBefore.isEmpty()) {
            log.info("deleting {} old concerts", allByDateBefore.size());
            concertRepository.deleteAll(allByDateBefore);
        }
    }
}


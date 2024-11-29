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
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
                        "genre is " + concert.getGenre() + " \n";
                if (!concert.getSupportBands().isEmpty()) {
                    message += "support bands are " + concert.getSupportBands() + "\n";

                }
                message += "playing at <a href=\"" + concert.getLink() + "\">" + concert.getLocation() + "</a>\n\n";
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
        allConcerts.addAll(getFeierwerkConcerts());
        allConcerts.addAll(getBackstageConcerts());
        allConcerts.addAll(getOlympiaparkConcerts());
        allConcerts.addAll(getKult9Concerts());
        allConcerts.addAll(getZenithConcerts());
        allConcerts.addAll(getTheaterfabrikConcerts());
        allConcerts.addAll(getStromConcerts());
        allConcerts.addAll(getMuffathalleConcerts());

        log.info("found {} concerts, saving now", allConcerts.size());
        for (ConcertDTO concertDTO : allConcerts) {
            if (concertRepository.findByTitleAndDate(concertDTO.title(), concertDTO.date()).isEmpty()) {
                log.info("new concert found. Title: {}", concertDTO.title());
                ConcertEntity concertEntity = concertMapper.toConcertEntity(concertDTO);
                concertRepository.save(concertEntity);
            }
        }
        generateHtml();
    }

    private Collection<ConcertDTO> getKult9Concerts() {
        List<ConcertDTO> kult9Concerts = new ArrayList<>();
        kult9Service.getConcerts().forEach(concert -> {
            if (concertRepository.findByTitleAndDate(concert.title(), concert.date()).isEmpty()) {
                kult9Concerts.add(new ConcertDTO(concert.title(), concert.date(), concert.link(), concert.genre(), concert.location(), ""));
            }
        });
        return kult9Concerts;
    }

    private Collection<ConcertDTO> getTheaterfabrikConcerts() {
        List<ConcertDTO> theaterfabrikConcerts = new ArrayList<>();
        theaterfabrikService.getConcerts().forEach(concert -> {
            if (concertRepository.findByTitleAndDate(concert.title(), concert.date()).isEmpty()) {
                Set<String> genres = genreService.getGenres(concert.title());
                theaterfabrikConcerts.add(new ConcertDTO(concert.title(), concert.date(), concert.link(), genres, concert.location(), ""));
            }
        });
        return theaterfabrikConcerts;
    }

    private Collection<ConcertDTO> getOlympiaparkConcerts() {
        List<ConcertDTO> olypiaparkConcerts = new ArrayList<>();
        olympiaparkService.getConcerts().forEach(concert -> {
            if (concertRepository.findByTitleAndDate(concert.title(), concert.date()).isEmpty()) {
                Set<String> genres = genreService.getGenres(concert.title());
                olypiaparkConcerts.add(new ConcertDTO(concert.title(), concert.date(), concert.link(), genres, concert.location(), ""));
            }
        });
        return olypiaparkConcerts;
    }

    public Collection<ConcertDTO> getFeierwerkConcerts() {

        List<ConcertDTO> feierwerkConcerts = new ArrayList<>();

        Set<String> concertLinks = feierwerkService.getConcertLinks();
        for (String url : concertLinks) {
            if (concertRepository.findByLink(url).isEmpty()) {
                Optional<ConcertDTO> concertOptional = feierwerkService.getConcert(url);
                concertOptional.ifPresent(feierwerkConcerts::add);
            }
        }
        return feierwerkConcerts;
    }

    public List<ConcertDTO> getBackstageConcerts() {
        List<ConcertDTO> backstageConcerts = new ArrayList<>();
        List<ConcertDTO> concerts = backstageService.getConcerts();

        concerts.forEach(concert -> {
            if (concertRepository.findByTitleAndDate(concert.title(), concert.date()).isEmpty()) {
                String supportBands = backstageService.getSupportBands(concert.link());
                backstageConcerts.add(new ConcertDTO(concert.title(), concert.date(), concert.link(), concert.genre(), concert.location(), supportBands));
            }
        });
        return backstageConcerts;
    }

    List<ConcertDTO> getZenithConcerts() {
        List<ConcertDTO> zenithConcerts = new ArrayList<>();
        zenithService.getConcerts().forEach(concert -> {
            if (concertRepository.findByTitle(concert.title()).isEmpty()) {
                Set<String> genres = genreService.getGenres(concert.title());
                zenithConcerts.add(new ConcertDTO(concert.title(), concert.date(), concert.link(), genres, concert.location(), ""));

            }
        });
        return zenithConcerts;
    }

    List<ConcertDTO> getStromConcerts() {
        List<ConcertDTO> stromConcerts = new ArrayList<>();
        for (ConcertDTO concert : stromService.getConcerts()) {
            if (concertRepository.findByTitleAndDate(concert.title(), concert.date()).isEmpty()) {
                Set<String> genres = genreService.getGenres(concert.title());
                ConcertDTO concertDTO = new ConcertDTO(concert.title(), concert.date(), concert.link(), genres, "Strom", "");
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
                ConcertDTO concertDTO = new ConcertDTO(muffathalleConcert.title(), date, muffathalleConcert.link(), genres, muffathalleConcert.location(), "");
                muffatHalleConcerts.add(concertDTO);
            }
        }
        return muffatHalleConcerts;

    }

    @PostConstruct
    public void generateHtml() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd LLL yyyy");

        String tdOpenTag = "<td>";
        String tdCloseTag = "</td>";
        StringBuilder result = new StringBuilder("""
                  <!DOCTYPE html>
                  <html lang="de">
                  <head>
                  <meta http-equiv="Content-Type"
                        content="text/html; charset=utf-8"/>
                      <title>All Concerts in munich</title>
                       <style>
                        body {
                          font-family: Arial, sans-serif;
                          margin: 0;
                          padding: 0;
                          background-color: #121212;
                          color: #e0e0e0;
                        }
                        .collapsible {
                          background-color: #000;
                          color: #ff4081;
                          cursor: pointer;
                          padding: 10px;
                          width: 100%;
                          border: none;
                          text-align: left;
                          outline: none;
                          font-size: 18px;
                        }
                        .active, .collapsible:hover {
                          background-color: #444;
                        }
                        .content {
                          padding: 0 15px;
                          display: block;
                          overflow: hidden;
                          background-color: #1e1e1e;
                        }
                        table {
                          width: 95%;
                          margin: 20px auto;
                          border-collapse: collapse;
                          background-color: #1e1e1e;
                          box-shadow: 0 4px 8px rgba(0, 0, 0, 0.7);
                          table-layout: fixed;  /* Ermöglicht flexiblere Darstellung der Spalten */
                        }
                        th, td {
                          padding: 12px;
                          text-align: left;
                          border-bottom: 1px solid #444;
                          white-space: normal;  /* Normaler Textumbruch */
                          overflow-wrap: break-word; /* Umbruch innerhalb der Zellen */
                        }
                        th {
                          background-color: #000;
                          color: #ff4081;
                        }
                        tr:hover {
                          background-color: #333;
                        }
                        a {
                          color: #82b1ff;
                          text-decoration: none;
                        }
                        a:hover {
                          text-decoration: underline;
                        }
                      </style>
                
                        <script>
                          document.addEventListener("DOMContentLoaded", function() {
                            var coll = document.getElementsByClassName("collapsible");
                            for (var i = 0; i < coll.length; i++) {
                              coll[i].addEventListener("click", function() {
                                this.classList.toggle("active");
                                var content = this.nextElementSibling;
                                if (content.style.display === "block") {
                                  content.style.display = "none";
                                } else {
                                  content.style.display = "block";
                                }
                              });
                            }
                          });
                        </script>
                  </head>
                
                
                    Filter for Metal: <input type="checkbox" id="filterMetalCheckBox"> <a href="https://t.me/MunichMetalConcerts">join the telegram METAL channel to get the newest updates</a><br>
                    Filter for Rock:  <input type="checkbox" id="filterRockCheckBox">  <a href="https://t.me/MunichRockConcerts">join the telegram ROCK channel to get the newest updates</a> <br>
                    Filter for Punk:  <input type="checkbox" id="filterPunkCheckBox">  <a href="https://t.me/MunichPunkConcerts">join the telegram PUNK channel to get the newest updates</a><br>
                
                    <button onclick="filterMetal()">Apply Filter</button>
                
                
                  <script >
                
                      function filterMetal() {
                
                          var checkBox = document.getElementById("filterMetalCheckBox");
                
                          const filterList = new Array();
                
                          if (document.getElementById("filterMetalCheckBox").checked == true) {
                              filterList.push("METAL")
                          }
                          if (document.getElementById("filterRockCheckBox").checked == true) {
                              filterList.push("ROCK")
                          }
                          if (document.getElementById("filterPunkCheckBox").checked == true) {
                              filterList.push("PUNK")
                          }
                
                          console.log(filterList);
                          var input, table, tr, td, i, txtValue;
                          input = document.getElementById("myInput");
                          table = document.getElementById("concertTable");
                          tr = table.getElementsByTagName("tr");
                
                
                          for (i = 0; i < tr.length; i++) {
                              tr[i].style.display = "";
                          }
                
                          if (filterList.length != 0) {
                              // Loop through all table rows, and hide those who don't match the search query
                              for (i = 0; i < tr.length; i++) {
                                  td = tr[i].getElementsByTagName("td")[2];
                                  if (td) {
                                      txtValue = td.textContent || td.innerText;
                
                                      if (txtValue.toUpperCase().indexOf(filterList[0]) > -1 || txtValue.toUpperCase().indexOf(filterList[1]) > -1 || txtValue.toUpperCase().indexOf(filterList[2]) > -1) {
                                          tr[i].style.display = "";
                                      } else {
                                          tr[i].style.display = "none";
                                      }
                
                
                
                                  }
                              }
                          }
                      }
                
                      </script>
                
                   <table id="concertTable">
                     <tr>
                        <th width=95>Datum</th>
                        <th>Band</th>
                        <th>Genre</th>
                        <th>Support</th>
                        <th width=120>Location</th>
                     </tr>
                """);
        for (ConcertEntity concertEntity : concertRepository.findByDateAfterOrderByDate(LocalDate.now().minusDays(1))) {
            result.append("<tr>\n");
            String title = concertEntity.getTitle();

            result.append(tdOpenTag).append(concertEntity.getDate().format(formatter)).append(tdCloseTag);
            result.append(tdOpenTag).append("<a href=\"").append(concertEntity.getLink()).append("\">").append(title).append("</a>").append(tdCloseTag);
            String genre = String.join(", ", concertEntity.getGenre());

            result.append(tdOpenTag).append(genre).append(tdCloseTag);
            result.append(tdOpenTag).append(concertEntity.getSupportBands()).append(tdCloseTag);
            result.append(tdOpenTag).append(concertEntity.getLocation()).append(tdCloseTag);
            result.append("</tr>");
        }
        result.append("</table></html>");
        try (PrintWriter out = new PrintWriter("result.html")) {
            out.println(result);
        } catch (Exception ex) {

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


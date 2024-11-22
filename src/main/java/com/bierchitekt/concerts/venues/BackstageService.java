package com.bierchitekt.concerts.venues;


import com.bierchitekt.concerts.ConcertDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.bierchitekt.concerts.venues.XmlUtils.extractXpath;
import static com.bierchitekt.concerts.venues.XmlUtils.getDocument;

@Slf4j
@Service
public class BackstageService {

    private static final Map<String, Integer> calendarMap = Map.ofEntries(Map.entry("Januar", 1), Map.entry("Februar", 2), Map.entry("März", 3), Map.entry("April", 4), Map.entry("Mai", 5), Map.entry("Juni", 6), Map.entry("Juli", 7), Map.entry("August", 8), Map.entry("September", 9), Map.entry("Oktober", 10), Map.entry("November", 11), Map.entry("Dezember", 12));
    private static final int ITEMS_PER_PAGE = 25;
    private static final String OVERVIEW_URL = "https://backstage.eu/veranstaltungen/live.html?product_list_limit=";

    public List<ConcertDTO> getConcerts() {
        try {
            List<ConcertDTO> allConcerts = new ArrayList<>();
            String url = OVERVIEW_URL + ITEMS_PER_PAGE;
            int totalElements = getPages(url);
            log.info("Backstage total elements: {}", totalElements);
            int totalPages = totalElements / ITEMS_PER_PAGE + 2;
            log.info("Backstage total pages: {}", totalPages);

            for (int i = 1; i < totalPages; ++i) {
                log.info("Backstage getting page {} of {}", i, totalPages);
                url = OVERVIEW_URL + ITEMS_PER_PAGE + "&p=" + i;
                List<ConcertDTO> concerts = getConcerts(url);
                allConcerts.addAll(concerts);

            }

            log.info("received {} Backstage concerts", allConcerts.size());
            return allConcerts;
        } catch (Exception ex) {
            return List.of();
        }
    }

    public String getPrice(String url) {
        try {
            Document document = getDocument(url);
            String xpathPrice = "//span[@class='price']";

            return extractXpath(xpathPrice, document).trim().replace(".", "");

        } catch (CannotDownloadDocumentException | IOException | ParserConfigurationException |
                 XPathExpressionException e) {
            return null;
        }
    }

    @SuppressWarnings("java:S1192")
    private List<ConcertDTO> getConcerts(String url) throws XPathExpressionException, IOException, ParserConfigurationException {
        try {
            Document xmlDocument = getDocument(url);
            List<ConcertDTO> concerts = new ArrayList<>();

            for (int i = 0; i <= ITEMS_PER_PAGE; i++) {

                String xpathTitle = "//ol/li[" + i + "]//a[@class='product-item-link']";

                String title = extractXpath(xpathTitle, xmlDocument);

                if (!"".equals(title)) {
                    String xpathLink = "//ol/li[" + i + "]//a[@class='product-item-link']/@href";

                    String link = extractXpath(xpathLink, xmlDocument);

                    String xpathDay = "//ol/li[" + i + "]//strong[@class='product name product-item-name eventdate']/span[@class='day']";
                    String xpathMonth = "//ol/li[" + i + "]//strong[@class='product name product-item-name eventdate']/span[@class='month']";
                    String xpathYear = "//ol/li[" + i + "]//strong[@class='product name product-item-name eventdate']/span[@class='year']";
                    String xpathGenre = "//ol/li[" + i + "]//div[@class='product description product-item-description']";
                    String xpathLocation = "//ol/li[" + i + "]//strong[@class='product name product-item-name eventlocation']";

                    String genre = extractXpath(xpathGenre, xmlDocument).trim().replace("Learn More", "");
                    String[] split = genre.split(",");
                    List<String> allGenres = new ArrayList<>();

                    for (String genres : split) {
                        allGenres.add(genres.trim());
                    }

                    String day = extractXpath(xpathDay, xmlDocument).trim().replace(".", "");
                    String month = extractXpath(xpathMonth, xmlDocument).trim();
                    String year = extractXpath(xpathYear, xmlDocument).trim().replace(".", "");

                    String location = extractXpath(xpathLocation, xmlDocument).trim().replace(".", "");
                    LocalDate date = LocalDate.of(Integer.parseInt(year), calendarMap.get(month), Integer.parseInt(day));

                    title = StringEscapeUtils.unescapeHtml4(title);
                    title = title.trim();
                    ConcertDTO concert = new ConcertDTO(title, date, link, allGenres, location, null);
                    concerts.add(concert);
                }
            }

            return concerts;

        } catch (CannotDownloadDocumentException ex) {
            return List.of();
        }

    }


    private int getPages(String url) throws IOException, ParserConfigurationException, XPathExpressionException {
        try {
            Document xmlDocument = getDocument(url);
            String pagesXpath = "//div[contains(@class, 'amount-wrap')]/span[@class='toolbar-number'][3]/text()";
            String pages = extractXpath(pagesXpath, xmlDocument);
            return Integer.parseInt(pages);
        } catch (CannotDownloadDocumentException ex) {
            return 0;
        }
    }
}

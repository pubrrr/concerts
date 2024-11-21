package com.bierchitekt.concerts.venues;

import com.bierchitekt.concerts.ConcertDTO;
import com.bierchitekt.concerts.spotify.SpotifyClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import org.w3c.dom.NodeList;

import javax.xml.xpath.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ZenithService {

    private final SpotifyClient spotifyClient;
    String url = "https://muenchen.motorworld.de/";

    public List<ConcertDTO> getConcerts() {
        try {
            org.jsoup.nodes.Document document = Jsoup.connect(url).get();
            org.jsoup.nodes.Document.OutputSettings settings = new org.jsoup.nodes.Document.OutputSettings();
            settings.syntax(Document.OutputSettings.Syntax.xml);
            String xml = document.outputSettings(settings).html();
            TagNode tagNode = new HtmlCleaner().clean(xml);
            org.w3c.dom.Document doc = new DomSerializer(new CleanerProperties()).createDOM(tagNode);
            List<ConcertDTO> concerts = new ArrayList<>();
            for (int i = 1; i < 99; i++) {
                String xpathTitle = "/html/body/main/div/div/div[6]/div/div/div[1]/div/div/div[" + i + "]/a/div/div[2]/div/div[1]/div/h1";
                String xpathDate = "/html/body/main/div/div/div[6]/div/div/div[1]/div/div/div[" + i + "]/a/div/div[3]/div/div/div";
                String xpathLink = "/html/body/main/div/div/div[6]/div/div/div[1]/div/div/div[" + i + "]/a/@href";
                String title = extractXpath(xpathTitle, doc);
                if (title.isEmpty()) {
                    break;
                }
                title = StringEscapeUtils.unescapeHtml4(title);
                String date = extractXpath(xpathDate, doc).trim();
                String link = extractXpath(xpathLink, doc);

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

                LocalDate localDate = LocalDate.parse(date, formatter);

                List<String> genres = spotifyClient.getGenres(title);
                ConcertDTO concertDTO = new ConcertDTO(title, localDate, link, genres, "zenith");
                log.info("title {} date {}", title, date);
                concerts.add(concertDTO);
            }

            return concerts;
        } catch (Exception ex) {
            return List.of();
        }
    }


    private String extractXpath(String xpathGenre, org.w3c.dom.Document xmlDocument) throws XPathExpressionException {
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();
        XPathExpression expression = xPath.compile(xpathGenre);
        NodeList nodeList = (NodeList) expression.evaluate(xmlDocument, XPathConstants.NODESET);
        return nodeList.item(0) == null ? "" : nodeList.item(0).getTextContent();
    }
}

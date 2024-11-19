package com.bierchitekt.concerts;


import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document.OutputSettings.Syntax;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class Crawler {

    private static final Map<String, Integer> calendarMap = Map.ofEntries(Map.entry("Januar", 1), Map.entry("Februar", 2), Map.entry("März", 3), Map.entry("April", 4), Map.entry("Mai", 5), Map.entry("Juni", 6), Map.entry("Juli", 7), Map.entry("August", 8), Map.entry("September", 9), Map.entry("Oktober", 10), Map.entry("November", 11), Map.entry("Dezember", 12));

    public List<ConcertDTO> getBackstageConcerts() throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
        List<ConcertDTO> allConcerts = new ArrayList<>();
        String url = "https://backstage.eu/veranstaltungen/live.html?product_list_limit=25";
        int totalElements = getPages(url);
        log.info("" + totalElements);
        int totalPages = totalElements / 25 + 2;
        log.info("" + totalPages);

        for (int i = 1; i < totalPages; ++i) {
            log.info("getting page " + i + " of " + totalPages);
            url = "https://backstage.eu/veranstaltungen/live.html?product_list_limit=25&p=" + i;
            Optional<Document> xmlDocument = getDocument(url);
            if (xmlDocument.isPresent()) {
                List<ConcertDTO> concerts = getConcerts(xmlDocument.get());
                allConcerts.addAll(concerts);
            }
        }

        log.info("saved " + allConcerts.size() + "concerts");
        return allConcerts;
    }

    private List<ConcertDTO> getConcerts(Document xmlDocument) throws XPathExpressionException {
        List<ConcertDTO> concerts = new ArrayList<>();
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();

        for (int i = 0; i <= 25; ++i) {
            String xpathTitle = "//ol/li[" + i + "]//a[@class='product-item-link']";
            XPathExpression expression = xPath.compile(xpathTitle);
            NodeList nodeList = (NodeList) expression.evaluate(xmlDocument, XPathConstants.NODESET);
            String title = "";

            for (int j = 0; j < nodeList.getLength(); ++j) {
                title = nodeList.item(j).getTextContent();
            }

            if ("".equals(title)) {
                log.info("continuing");
            } else {
                String xpathLink = "//ol/li[" + i + "]//a[@class='product-item-link']/@href";
                expression = xPath.compile(xpathLink);
                nodeList = (NodeList) expression.evaluate(xmlDocument, XPathConstants.NODESET);
                String link = "";

                for (int j = 0; j < nodeList.getLength(); ++j) {
                    link = nodeList.item(j).getTextContent();
                }

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
                ConcertDTO concert = new ConcertDTO(title.trim(), date, link, allGenres, location);
                concerts.add(concert);
            }
        }

        return concerts;
    }

    private String extractXpath(String xpathGenre, Document xmlDocument) throws XPathExpressionException {
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();
        XPathExpression expression = xPath.compile(xpathGenre);
        NodeList nodeList = (NodeList) expression.evaluate(xmlDocument, XPathConstants.NODESET);
        return nodeList.item(0) == null ? "" : nodeList.item(0).getTextContent();
    }

    private Optional<Document> getDocument(String url) throws IOException, ParserConfigurationException {
        log.info("getting url {}", url);
        org.jsoup.nodes.Document document = Jsoup.connect(url).get();
        org.jsoup.nodes.Document.OutputSettings settings = new org.jsoup.nodes.Document.OutputSettings();
        settings.syntax(Syntax.xml);
        String xml = document.outputSettings(settings).html();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        try {
            return Optional.of(builder.parse(new InputSource(new StringReader(xml))));
        } catch (Exception ex) {

            log.info(xml);
        }
        return Optional.empty();
    }

    private int getPages(String url) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
        Document xmlDocument = getDocument(url).get();
        String pagesXpath = "//div[contains(@class, 'amount-wrap')]/span[@class='toolbar-number'][3]/text()";
        String pages = extractXpath(pagesXpath, xmlDocument);
        return Integer.parseInt(pages);
    }
}

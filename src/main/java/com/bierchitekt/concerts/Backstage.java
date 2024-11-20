package com.bierchitekt.concerts;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document.OutputSettings.Syntax;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class Backstage {

    private static final Map<String, Integer> calendarMap = Map.ofEntries(Map.entry("Januar", 1), Map.entry("Februar", 2), Map.entry("März", 3), Map.entry("April", 4), Map.entry("Mai", 5), Map.entry("Juni", 6), Map.entry("Juli", 7), Map.entry("August", 8), Map.entry("September", 9), Map.entry("Oktober", 10), Map.entry("November", 11), Map.entry("Dezember", 12));
    private static final int itemsPerPage = 25;
    private static final String overviewUrl = "https://backstage.eu/veranstaltungen/live.html?product_list_limit=";

    public List<ConcertDTO> getConcerts() throws IOException, ParserConfigurationException, XPathExpressionException {
        List<ConcertDTO> allConcerts = new ArrayList<>();
        String url = overviewUrl + itemsPerPage;
        int totalElements = getPages(url);
        log.info("" + totalElements);
        int totalPages = totalElements / itemsPerPage + 2;
        log.info("" + totalPages);

        for (int i = 1; i < totalPages; ++i) {
            log.info("getting page " + i + " of " + totalPages);
            url = overviewUrl + itemsPerPage + "&p=" + i;
            log.info(url);
            List<ConcertDTO> concerts = getConcerts(url);
            allConcerts.addAll(concerts);

        }

        log.info("saved {} concerts", allConcerts.size());
        return allConcerts;
    }

    public String getPrice(String url) {
        try {
            Optional<Document> documentOptional = getDocument(url);
            if(documentOptional.isEmpty()){
                return null;
            }
            Document document = documentOptional.get();
            String xpathPrice = "//span[@class='price']";

            return extractXpath(xpathPrice, document).trim().replace(".", "");

        } catch (IOException | ParserConfigurationException | XPathExpressionException e) {
            return null;
        }
    }

    private List<ConcertDTO> getConcerts(String url) throws XPathExpressionException, IOException, ParserConfigurationException {
        Optional<Document> xmlDocumentOptional = getDocument(url);
        if (xmlDocumentOptional.isEmpty()) {
            return List.of();
        } else {
            Document xmlDocument = xmlDocumentOptional.get();
            List<ConcertDTO> concerts = new ArrayList<>();
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();

            for (int i = 0; i <= 25; i++) {
                String xpathTitle = "//ol/li[" + i + "]//a[@class='product-item-link']";
                XPathExpression expression = xPath.compile(xpathTitle);
                NodeList nodeList = (NodeList) expression.evaluate(xmlDocument, XPathConstants.NODESET);
                String title = "";

                for (int j = 0; j < nodeList.getLength(); ++j) {
                    title = nodeList.item(j).getTextContent();
                }

                if (!"".equals(title)) {
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
                    title = title.trim();
                    title = StringEscapeUtils.unescapeHtml4(title);
                    ConcertDTO concert = new ConcertDTO(title.trim(), date, link, allGenres, location);
                    concerts.add(concert);
                }
            }

            return concerts;
        }
    }

    private String extractXpath(String xpathGenre, Document xmlDocument) throws XPathExpressionException {
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();
        XPathExpression expression = xPath.compile(xpathGenre);
        NodeList nodeList = (NodeList) expression.evaluate(xmlDocument, XPathConstants.NODESET);
        return nodeList.item(0) == null ? "" : nodeList.item(0).getTextContent();
    }

    private Optional<Document> getDocument(String url) throws IOException, ParserConfigurationException, XPathExpressionException {
        log.info("getting url {}", url);
        org.jsoup.nodes.Document document = Jsoup.connect(url).get();
        org.jsoup.nodes.Document.OutputSettings settings = new org.jsoup.nodes.Document.OutputSettings();
        settings.syntax(Syntax.xml);
        String xml = document.outputSettings(settings).html();
        TagNode tagNode = new HtmlCleaner().clean(xml);
        Document doc = new DomSerializer(new CleanerProperties()).createDOM(tagNode);
        return Optional.of(doc);

    }

    private int getPages(String url) throws IOException, ParserConfigurationException, XPathExpressionException {
        Optional<Document> xmlDocumentOptional = getDocument(url);
        if (xmlDocumentOptional.isEmpty()) {
            return 0;
        }
        Document xmlDocument = xmlDocumentOptional.get();
        String pagesXpath = "//div[contains(@class, 'amount-wrap')]/span[@class='toolbar-number'][3]/text()";
        String pages = extractXpath(pagesXpath, xmlDocument);
        return Integer.parseInt(pages);
    }
}

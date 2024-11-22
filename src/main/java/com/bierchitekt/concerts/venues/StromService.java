package com.bierchitekt.concerts.venues;

import com.bierchitekt.concerts.ConcertDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
public class StromService {

    private static final String url = "https://strom-muc.de/";

    public List<ConcertDTO> getConcerts() {
        List<ConcertDTO> concerts = new ArrayList<>();

        try {
            final Document document = Jsoup.connect(url).get();

            Document.OutputSettings settings = new Document.OutputSettings();
            settings.syntax(Document.OutputSettings.Syntax.xml);
            final String xml = document.outputSettings(settings).html();
            final TagNode tagNode = new HtmlCleaner().clean(xml);
            final org.w3c.dom.Document doc = new DomSerializer(new CleanerProperties()).createDOM(tagNode);
            for (int i = 3; i < 99; i++) {
                log.info("getting number {}", i);
                String xpathTitle = "/html/body/div/div[2]/div[1]/div/section/div/div[3]/div/div[" + i + "]/div/h3/a";
                String xpathLink = "/html/body/div/div[2]/div[1]/div/section/div/div[3]/div/div[" + i + "]/div/h3/a/@href";
                String xpathType = "/html/body/div/div[2]/div[1]/div/section/div/div[3]/div/div[" + i + "]/div/div[2]/a";

                String type = extractXpath(xpathType, doc);

                if (!"konzert".equalsIgnoreCase(type)) {
                    continue;
                }
                String title = extractXpath(xpathTitle, doc);

                title = StringEscapeUtils.unescapeHtml4(title);
                String link = extractXpath(xpathLink, doc);

                ConcertDTO concertDTO = new ConcertDTO(title, null, link, null, "Strom");
                log.info(concertDTO + "");
                concerts.add(concertDTO);
            }

        } catch (Exception ex) {
            log.error("ex", ex);
            return List.of();
        }
        return concerts;
    }


    private String extractXpath(String xpathGenre, org.w3c.dom.Document xmlDocument) throws XPathExpressionException {
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();
        XPathExpression expression = xPath.compile(xpathGenre);
        NodeList nodeList = (NodeList) expression.evaluate(xmlDocument, XPathConstants.NODESET);
        return nodeList.item(0) == null ? "" : nodeList.item(0).getTextContent();
    }

    public LocalDate getDate(String link) {
        try {
            Document detail = Jsoup.connect(link).get();

            Document.OutputSettings settings = new Document.OutputSettings();
            settings.syntax(Document.OutputSettings.Syntax.xml);
            String detailXml = detail.outputSettings(settings).html();
            TagNode detailTagNode = new HtmlCleaner().clean(detailXml);
            org.w3c.dom.Document detailDoc = new DomSerializer(new CleanerProperties()).createDOM(detailTagNode);

            String xpathDate = "/html/body/div/div[2]/div[1]/div/div/div[1]/div[1]/div/div[1]/div[1]/div[2]/div[1]";

            String date = extractXpath(xpathDate, detailDoc).trim();
            date = StringUtils.remove(date, "Datum:").trim();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            LocalDate localDate = LocalDate.MIN;
            if (StringUtils.isNotEmpty(date)) {
                return LocalDate.parse(date, formatter);
            }
            return localDate;
        } catch (Exception ex) {
            log.warn("exception", ex);
        }
        return LocalDate.MIN;

    }
}

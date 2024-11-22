package com.bierchitekt.concerts.venues;

import com.bierchitekt.concerts.ConcertDTO;
import lombok.extern.slf4j.Slf4j;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.w3c.dom.NodeList;

import javax.xml.xpath.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MuffathalleService {

    private  static final String url = "https://www.muffatwerk.de/de/events/concert";


    public static void main(String... args){
        getConcerts();
    }

    public static List<ConcertDTO> getConcerts() {
        try {
            org.jsoup.nodes.Document document = Jsoup.connect(url).get();

            org.jsoup.nodes.Document.OutputSettings settings = new org.jsoup.nodes.Document.OutputSettings();
            settings.syntax(Document.OutputSettings.Syntax.xml);
            String xml = document.outputSettings(settings).html();
            TagNode tagNode = new HtmlCleaner().clean(xml);
            org.w3c.dom.Document doc = new DomSerializer(new CleanerProperties()).createDOM(tagNode);
            List<ConcertDTO> concerts = new ArrayList<>();

            String xpathTitxle = "/html/body/table/tbody/tr[283]/td[2]/span[1]/a";


            String xpathTitle = "/html/body/div[2]/div[4]/div/div/div/div[2]/div[2][@onClick]";
            //                  "/html/body/div[2]/div[5]//a[@class='entry-data']"
//                              "/html/body/div[2]/div[4]/div/div/div/div[2]/div[2]/span[1]/"
            String title = extractXpath(xpathTitle, doc);

            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();


            String xpathLink = "/html/body/table/tbody/tr[588]/td[2]/span[1]/a/@href";

            XPathExpression expression  = xPath.compile(xpathLink);
            NodeList nodeList = (NodeList) expression.evaluate(doc, XPathConstants.NODESET);

            String link = "";

            for (int j = 0; j < nodeList.getLength(); ++j) {
                link = nodeList.item(j).getTextContent();
            }

            //<td class="line-content">						<span class="html-tag">&lt;a <span class="html-attribute-name">href</span>="<a class="html-attribute-value html-external-link" target="_blank" href="/de/events/view/6789/dis-m" rel="noreferrer noopener">/de/events/view/6789/dis-m</a>"&gt;</span>Info<span class="html-tag">&lt;/a&gt;</span></td>

            log.info(title);

        } catch (Exception ex) {
            return List.of();
        }
        return List.of();
    }



    private static String extractXpath(String xpathGenre, org.w3c.dom.Document xmlDocument) throws XPathExpressionException {
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();
        XPathExpression expression = xPath.compile(xpathGenre);
        NodeList nodeList = (NodeList) expression.evaluate(xmlDocument, XPathConstants.NODESET);
        log.info(nodeList.getLength() + "");
        return nodeList.item(0) == null ? "" : nodeList.item(0).getTextContent();
    }
}

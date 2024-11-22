package com.bierchitekt.concerts.venues;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.jsoup.Jsoup;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class XmlUtils {

    public static String extractXpath(String xpathExpression, Document xmlDocument) throws XPathExpressionException {
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();
        XPathExpression expression = xPath.compile(xpathExpression);
        NodeList nodeList = (NodeList) expression.evaluate(xmlDocument, XPathConstants.NODESET);
        return nodeList.item(0) == null ? "" : nodeList.item(0).getTextContent();
    }

    public static Document getDocument(String url) throws IOException, ParserConfigurationException, CannotDownloadDocumentException {
        org.jsoup.nodes.Document.OutputSettings settings = new org.jsoup.nodes.Document.OutputSettings();
        settings.syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml);
        String xml = Jsoup.connect(url).get().outputSettings(settings).html();
        TagNode tagNode = new HtmlCleaner().clean(xml);
        Document doc = new DomSerializer(new CleanerProperties()).createDOM(tagNode);
        if (doc == null) {
            throw new CannotDownloadDocumentException();
        }
        return doc;
    }
}

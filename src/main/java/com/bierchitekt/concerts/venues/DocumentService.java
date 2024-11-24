package com.bierchitekt.concerts.venues;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

@Component
public class DocumentService {

    public org.w3c.dom.Document getDocument(String url) throws IOException, ParserConfigurationException, CannotDownloadDocumentException {
        org.jsoup.nodes.Document.OutputSettings settings = new org.jsoup.nodes.Document.OutputSettings();
        settings.syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml);
        String xml = Jsoup.connect(url).get().outputSettings(settings).html();
        TagNode tagNode = new HtmlCleaner().clean(xml);
        org.w3c.dom.Document doc = new DomSerializer(new CleanerProperties()).createDOM(tagNode);
        if (doc == null) {
            throw new CannotDownloadDocumentException();
        }
        return doc;
    }

}

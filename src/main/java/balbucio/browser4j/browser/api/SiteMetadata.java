package balbucio.browser4j.browser.api;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class SiteMetadata {
    private final String title;
    private final String url;
    private final String icon;
    private final String description;
    private final String keywords;

    public SiteMetadata(String title, String url, String icon, String description, String keywords) {
        this.title = title;
        this.url = url;
        this.icon = icon;
        this.description = description;
        this.keywords = keywords;
    }

    public String getTitle() { return title; }
    public String getUrl() { return url; }
    public String getIcon() { return icon; }
    public String getDescription() { return description; }
    public String getKeywords() { return keywords; }

    public static SiteMetadata fromDocument(Document doc, String pageUrl) {
        if (doc == null) {
            return new SiteMetadata(null, pageUrl, null, null, null);
        }

        String title = doc.title();
        String icon = null;

        Element iconLink = doc.selectFirst("link[rel~=^(?i)(shortcut icon|icon|apple-touch-icon)$]");
        if (iconLink == null) {
            iconLink = doc.selectFirst("link[rel~=^(?i)(alternate icon)$]");
        }
        if (iconLink == null) {
            iconLink = doc.selectFirst("link[rel~=^(?i)(apple-touch-icon-precomposed)$]");
        }
        if (iconLink != null) {
            icon = iconLink.attr("href");
        }

        String description = null;
        Element desc = doc.selectFirst("meta[name=description], meta[property=og:description]");
        if (desc != null) {
            description = desc.attr("content");
        }

        String keywords = null;
        Element kw = doc.selectFirst("meta[name=keywords]");
        if (kw != null) {
            keywords = kw.attr("content");
        }

        return new SiteMetadata(title == null ? "" : title,
                pageUrl,
                icon == null ? "" : icon,
                description == null ? "" : description,
                keywords == null ? "" : keywords);
    }
}

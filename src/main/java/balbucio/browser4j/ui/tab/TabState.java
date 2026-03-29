package balbucio.browser4j.ui.tab;

public class TabState {
    private String title = "";
    private String url = "";
    private boolean loading = false;
    private long lastAccessedAt = System.currentTimeMillis();
    private boolean incognito = false;
    private boolean drmProtected = false;

    private String icon = "";
    private String description = "";
    private String keywords = "";

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getKeywords() { return keywords; }
    public void setKeywords(String keywords) { this.keywords = keywords; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public boolean isLoading() { return loading; }
    public void setLoading(boolean loading) { this.loading = loading; }

    public long getLastAccessedAt() { return lastAccessedAt; }
    public void markAccessed() { this.lastAccessedAt = System.currentTimeMillis(); }
    
    public boolean isIncognito() { return incognito; }
    public void setIncognito(boolean incognito) { this.incognito = incognito; }
    
    public boolean isDrmProtected() { return drmProtected; }
    public void setDrmProtected(boolean drmProtected) { this.drmProtected = drmProtected; }
}

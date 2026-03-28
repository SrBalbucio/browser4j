package balbucio.browser4j.ui.tab;

public class TabState {
    private String title = "";
    private String url = "";
    private boolean loading = false;
    private long lastAccessedAt = System.currentTimeMillis();

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public boolean isLoading() { return loading; }
    public void setLoading(boolean loading) { this.loading = loading; }

    public long getLastAccessedAt() { return lastAccessedAt; }
    public void markAccessed() { this.lastAccessedAt = System.currentTimeMillis(); }
}

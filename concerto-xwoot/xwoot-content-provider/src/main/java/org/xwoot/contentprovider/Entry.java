package org.xwoot.contentprovider;

public class Entry
{
    private String pageId;

    private long timestamp;

    private int version;

    private int minorVersion;

    private boolean cleared;

    public Entry(String pageId, long timestamp, int version, int minorVersion, boolean cleared)
    {
        this.pageId = pageId;
        this.timestamp = timestamp;
        this.version = version;
        this.minorVersion = minorVersion;
        this.cleared = cleared;
    }

    public String getPageId()
    {
        return pageId;
    }

    public long getTimestamp()
    {
        return timestamp;
    }

    public int getVersion()
    {
        return version;
    }

    public int getMinorVersion()
    {
        return minorVersion;
    }

    public boolean isCleared()
    {
        return cleared;
    }

}

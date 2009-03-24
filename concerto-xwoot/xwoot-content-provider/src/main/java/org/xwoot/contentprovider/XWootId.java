package org.xwoot.contentprovider;

/**
 * A class representing an XWoot Id, i.e. a page id and a timestamp. This id is used to reference a modification.
 * 
 * @vesion $Id$
 */
public class XWootId
{
    private String pageId;

    private long timestamp;

    private int version;

    private int minorVersion;

    public XWootId(String pageId, long timestamp, int version, int minorVersion)
    {
        this.pageId = pageId;
        this.timestamp = timestamp;
        this.version = version;
        this.minorVersion = minorVersion;
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

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + minorVersion;
        result = prime * result + ((pageId == null) ? 0 : pageId.hashCode());
        result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
        result = prime * result + version;
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        XWootId other = (XWootId) obj;
        if (minorVersion != other.minorVersion)
            return false;
        if (pageId == null) {
            if (other.pageId != null)
                return false;
        } else if (!pageId.equals(other.pageId))
            return false;
        if (timestamp != other.timestamp)
            return false;
        if (version != other.version)
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return String.format("[XWootId: %s %d %d.%d]", pageId, timestamp, version, minorVersion);
    }

}

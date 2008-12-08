package org.xwoot;

public class XWootId
{
    private String pageId;

    private long timestamp;

    public XWootId(String pageId, long timestamp)
    {
        this.pageId = pageId;
        this.timestamp = timestamp;
    }

    public String getPageId()
    {
        return pageId;
    }

    public long getTimestamp()
    {
        return timestamp;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((pageId == null) ? 0 : pageId.hashCode());
        result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
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
        if (pageId == null) {
            if (other.pageId != null)
                return false;
        } else if (!pageId.equals(other.pageId))
            return false;
        if (timestamp != other.timestamp)
            return false;
        return true;
    }

}

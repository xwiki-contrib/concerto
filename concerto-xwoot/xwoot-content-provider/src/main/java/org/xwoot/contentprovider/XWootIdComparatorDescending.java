package org.xwoot.contentprovider;

import java.util.Comparator;

public class XWootIdComparatorDescending implements Comparator<XWootId>
{
    public int compare(XWootId o1, XWootId o2)
    {
        return (int) (o2.getTimestamp() - o1.getTimestamp());
    }
}

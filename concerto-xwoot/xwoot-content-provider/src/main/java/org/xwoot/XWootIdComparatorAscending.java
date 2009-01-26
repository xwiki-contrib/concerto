package org.xwoot;

import java.util.Comparator;

public class XWootIdComparatorAscending implements Comparator<XWootId>
{
    public int compare(XWootId o1, XWootId o2)
    {
        return (int) (o1.getTimestamp() - o2.getTimestamp());
    }
}

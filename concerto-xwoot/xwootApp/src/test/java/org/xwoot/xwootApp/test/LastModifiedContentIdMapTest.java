package org.xwoot.xwootApp.test;

import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;
import org.xwoot.XWootId;
import org.xwoot.xwootApp.XWootException;
import org.xwoot.xwootApp.core.LastModifiedContentIdMap;

/**
 * Test the LastModifiedPageNameList class.
 * 
 * @version $Id:$
 */
public class LastModifiedContentIdMapTest extends AbstractXWootTest
{
    private String pn1 = "toto";

    // private String pn2="titi";
    //    
    // private String pn3="tata";

    /** content id for test. */
    private String cid1 = "page.mainPageContent";

    /** content id for test. */
    private String cid2 = "comment1.mainContent";

    /** content id for test. */
    private String cid3 = "tag.mainContent";

    /**
     * Test the add and remove methods on the last modified contentid list in {@link ContentManager}.
     * 
     * @throws WootEngineException if problems occur while loading/storing the page list.
     */
    @Test
    public void pageListTest() throws XWootException
    {
        // page toto have been modified to version => 1.0
        LastModifiedContentIdMap lmcim = new LastModifiedContentIdMap(WORKINGDIR);
        XWootId id1 = new XWootId(this.pn1, 10, 1, 0);
        lmcim.add(id1, this.cid1);
        lmcim.add(id1, this.cid2);
        lmcim.add(id1, this.cid3);

        Map<XWootId, Set<String>> currentMap = lmcim.getCurrentMap();

        Assert.assertTrue(currentMap.containsKey(id1));
        Assert.assertEquals(currentMap.size(), 1);
        Assert.assertEquals(currentMap.get(id1).size(), 3);
        Assert.assertTrue(currentMap.get(id1).contains(this.cid1));
        Assert.assertTrue(currentMap.get(id1).contains(this.cid2));
        Assert.assertTrue(currentMap.get(id1).contains(this.cid3));
        lmcim.remove(id1);
        Assert.assertTrue(currentMap.containsKey(id1));
        Assert.assertEquals(currentMap.get(id1).size(), 3);
        currentMap = lmcim.getCurrentMap();
        Assert.assertFalse(currentMap.containsKey(id1));
        Assert.assertEquals(currentMap.size(), 0);
    }

    /**
     * Test concurrency add and remove methods on the last modified contentid list in {@link ContentManager}.
     * 
     * @throws WootEngineException if problems occur while loading/storing the page list.
     */
    @Test
    public void concurrencyPageListTest() throws XWootException
    {
        // page toto have been modified to version => 1.0
        LastModifiedContentIdMap lmcim = new LastModifiedContentIdMap(WORKINGDIR);
        XWootId id1 = new XWootId(this.pn1, 10, 1, 0);
        lmcim.add(id1, this.cid1);
        lmcim.add(id1, this.cid2);
        lmcim.add(id1, this.cid3);

        Map<XWootId, Set<String>> currentMap = lmcim.getCurrentMap();

        XWootId id2 = new XWootId(this.pn1, 11, 1, 1);

        lmcim.add(id2, this.cid1);
        lmcim.add(id2, this.cid2);
        lmcim.add(id2, this.cid3);

        Assert.assertTrue(currentMap.containsKey(id1));
        Assert.assertEquals(currentMap.size(), 1);
        Assert.assertEquals(currentMap.get(id1).size(), 3);
        Assert.assertTrue(currentMap.get(id1).contains(this.cid1));
        Assert.assertTrue(currentMap.get(id1).contains(this.cid2));
        Assert.assertTrue(currentMap.get(id1).contains(this.cid3));
        Assert.assertFalse(currentMap.containsKey(id2));
        lmcim.remove(id1);
        Assert.assertTrue(currentMap.containsKey(id1));
        Assert.assertEquals(currentMap.get(id1).size(), 3);
        Assert.assertFalse(currentMap.containsKey(id2));
        currentMap = lmcim.getCurrentMap();
        Assert.assertFalse(currentMap.containsKey(id1));
        Assert.assertEquals(currentMap.size(), 1);
        Assert.assertTrue(currentMap.containsKey(id2));
        Assert.assertEquals(currentMap.get(id2).size(), 3);
        Assert.assertTrue(currentMap.get(id2).contains(this.cid1));
        Assert.assertTrue(currentMap.get(id2).contains(this.cid2));
        Assert.assertTrue(currentMap.get(id2).contains(this.cid3));
    }

}

package org.xwoot.wootEngine.test;

import junit.framework.Assert;

import org.junit.Test;
import org.xwoot.wootEngine.WootEngineException;


public class PageListTest extends AbstractWootEngineTest
{
    
    
    @Test
    /**
     * Test the add and remove methods on pagename list in {@link PageManager}
     * 
     * @throws WootEngineException if problems occur while loading/storing the page list.
     */
    public void pageListTest() throws WootEngineException{
        String pn1="toto";
        String pn2="titi";
        String pn3="tata";
        this.site0.getPageManager().getLastModifiedPageNameList().addPageNameInList(pn1);
        Assert.assertEquals(pn1,this.site0.getPageManager().getLastModifiedPageNameList().getCurrentPageNameList().get(0));
        this.site0.getPageManager().getLastModifiedPageNameList().addPageNameInList(pn2);
        this.site0.getPageManager().getLastModifiedPageNameList().addPageNameInList(pn1);
        this.site0.getPageManager().getLastModifiedPageNameList().addPageNameInList(pn3);
        this.site0.getPageManager().getLastModifiedPageNameList().addPageNameInList(pn1);
        Assert.assertEquals(5,this.site0.getPageManager().getLastModifiedPageNameList().getCurrentPageNameList().size());
        this.site0.getPageManager().getLastModifiedPageNameList().removeNOccurencesInList(pn1, 2);
        Assert.assertEquals(pn2,this.site0.getPageManager().getLastModifiedPageNameList().getCurrentPageNameList().get(0));
        Assert.assertEquals(pn3,this.site0.getPageManager().getLastModifiedPageNameList().getCurrentPageNameList().get(1));
        Assert.assertEquals(pn1,this.site0.getPageManager().getLastModifiedPageNameList().getCurrentPageNameList().get(2));
    }
}

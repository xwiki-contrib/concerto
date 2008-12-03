package org.xwoot.wootEngine.test;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.xwoot.wootEngine.PageManager;
import org.xwoot.wootEngine.WootEngineException;


public class PageListTest extends AbstractWootEngineTest
{
    private PageManager pm;
    
    @Before
    public void config() throws Exception{
        this.createEngine(0);
       this.pm=new PageManager(1234,this.site0.getWorkingDir());
    }
    
    @Test
    public void pageListTest() throws WootEngineException{
        String pn1="toto";
        String pn2="titi";
        String pn3="tata";
        this.pm.addPageNameInList(pn1);
        Assert.assertEquals(pn1,this.pm.getPageNameList().get(0));
        this.pm.addPageNameInList(pn2);
        this.pm.addPageNameInList(pn1);
        this.pm.addPageNameInList(pn3);
        this.pm.addPageNameInList(pn1);
        Assert.assertEquals(5,this.pm.getPageNameList().size());
        this.pm.removeNOccurencesInList(pn1, 2);
        Assert.assertEquals(pn2,this.pm.getPageNameList().get(0));
        Assert.assertEquals(pn3,this.pm.getPageNameList().get(1));
        Assert.assertEquals(pn1,this.pm.getPageNameList().get(2));
    }
}

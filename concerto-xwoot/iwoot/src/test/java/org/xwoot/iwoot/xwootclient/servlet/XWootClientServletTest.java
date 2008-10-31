package org.xwoot.iwoot.xwootclient.servlet;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xwoot.iwoot.xwootclient.XWootClientAPI;
import org.xwoot.iwoot.xwootclient.XWootClientFactory;

public class XWootClientServletTest
{
    static private XWootClientAPI xwoot;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        xwoot= XWootClientFactory.getServletFactory().createXWootClient("http://concerto.loria.fr:8080/xwoot1");
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception
    {
        //void
    }
    
    @Test
    public void testgetInfos()
    {  
       // assertTrue(xwoot.isContentManagerConnected());
    }

}

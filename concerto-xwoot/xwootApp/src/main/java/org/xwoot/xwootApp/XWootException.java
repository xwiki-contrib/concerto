package org.xwoot.xwootApp;

public class XWootException extends Exception
{

    /**
     * 
     */
    private static final long serialVersionUID = -2568698052987298242L;

    public XWootException()
    {
        super();
    }

    public XWootException(Throwable arg0)
    {
        super(arg0);
    }

    public XWootException(String arg0)
    {
        super(arg0);
    }

    public XWootException(String arg0, Throwable t)
    {
        super(arg0, t);
    }
}

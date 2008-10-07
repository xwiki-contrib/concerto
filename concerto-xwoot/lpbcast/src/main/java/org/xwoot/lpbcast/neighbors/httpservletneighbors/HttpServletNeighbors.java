package org.xwoot.lpbcast.neighbors.httpservletneighbors;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import org.xwoot.lpbcast.neighbors.Neighbors;
import org.xwoot.lpbcast.util.NetUtil;

public class HttpServletNeighbors extends Neighbors
{

    // thread for parallelism
    protected class NotifyNeighbors extends Thread
    {
        private Object message;

        private String neighbor = null;

        public void setNeighbor(String neighbor)
        {
            this.neighbor = neighbor;
        }

        protected void call(Object n) throws IOException
        {
            System.out.println("Send message to : " + n);
            URL to = new URL(n + "/receiveMessage.do");
            NetUtil.sendObjectViaHTTPRequest(to, this.message);
        }

        /**
         * DOCUMENT ME!
         * 
         * @return DOCUMENT ME!
         */
        public Object getMessage()
        {
            return this.message;
        }

        /**
         * DOCUMENT ME!
         */
        @Override
        public void run()
        {
            try {
                if (this.neighbor != null) {
                    this.call(this.neighbor);
                } else {
                    for (Iterator i = HttpServletNeighbors.this.neighborsList().iterator(); i.hasNext();) {
                        try {
                            // TODO externalize the communication
                            Object n = i.next();
                            this.call(n);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        /**
         * DOCUMENT ME!
         * 
         * @param message DOCUMENT ME!
         * @throws Exception DOCUMENT ME!
         */
        public void setMessage(Object message)
        {
            this.message = message;
        }
    }

    public HttpServletNeighbors(String neighborsFilePath, int maxNumber, Integer id) throws Exception
    {
        super(neighborsFilePath, maxNumber, id);
    }

    // send message to one neighbor
    /**
     * DOCUMENT ME!
     * 
     * @param neighbor DOCUMENT ME!
     * @param message DOCUMENT ME!
     * @throws IOException
     * @throws Exception DOCUMENT ME!
     */
    @Override
    public void notifyNeighbor(Object neighbor, Object message) throws IOException
    {
        NotifyNeighbors notify = new NotifyNeighbors();
        notify.setMessage(message);
        notify.setNeighbor((String) neighbor);
        notify.start();
    }

    // send message to neighbors
    /**
     * DOCUMENT ME!
     * 
     * @param message DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    @Override
    public void notifyNeighbors(Object message)
    {
        NotifyNeighbors notify = new NotifyNeighbors();
        notify.setMessage(message);
        notify.start();
    }
}

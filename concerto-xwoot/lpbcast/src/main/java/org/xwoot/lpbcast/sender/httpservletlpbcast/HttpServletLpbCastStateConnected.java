package org.xwoot.lpbcast.sender.httpservletlpbcast;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;

import java.io.IOException;

import java.io.OutputStream;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.net.HttpURLConnection;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xwoot.lpbcast.LpbCastException;
import org.xwoot.lpbcast.message.Message;
import org.xwoot.lpbcast.neighbors.Neighbors;
import org.xwoot.lpbcast.neighbors.NeighborsException;
import org.xwoot.lpbcast.util.NetUtil;

public class HttpServletLpbCastStateConnected extends HttpServletLpbCastState
{

    public HttpServletLpbCastStateConnected(HttpServletLpbCast connection)
    {
        super(connection);
    }

    @Override
    public void connectSender()
    {
        throw new IllegalStateException("Already connected");
    }

    @Override
    public void disconnectSender()
    {
        // disconnect somehow
        // finally set disconnected state of the connection instance
        this.connection.setState(this.connection.DISCONNECTED);
    }

    public Neighbors getNeighbors()
    {
        return this.connection.getNeighbors();
    }

    public Message getNewMessage(Object creatorPeerId, Object content, int action, int round)
    {
        return this.connection.getNewMessage(creatorPeerId, content, action, round);
    }

    public int getRound()
    {
        return this.connection.getRound();
    }

    public void gossip(Object from, Object message) throws HttpServletLpbCastException 
    {
        this.connection.logger.info(this.connection.getId() + " Send message to all neighbors\n\n");
        this.addNeighbor(from, ((Message) message).getOriginalPeerId());
        try {
            if (!this.connection.getNeighbors().neighborsList().isEmpty()) {
                this.connection.getNeighbors().notifyNeighbors(message);
            }
        } catch (NeighborsException e) {
            throw new HttpServletLpbCastException(this.connection.getId()+" : Problem to get neighbors list",e);
        }
    }

    @Override
    public boolean isSenderConnected()
    {
        return true;
    }

    public void processSendState(HttpServletRequest request, HttpServletResponse response, File state) throws HttpServletLpbCastException {
        if (state != null) {
            System.out.println("Send state");
            response.setHeader("Content-type", "application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=" + state.getName());

            OutputStream out;
            try {
                out = response.getOutputStream();
                FileInputStream fis = new FileInputStream(state);
                byte[] buffer = new byte[2048];
                int bytesIn = 0;

                while ((bytesIn = fis.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesIn);
                }

                out.flush();
                out.close();
            } catch (IOException e) {
               throw new HttpServletLpbCastException(this.connection.getId()+" : Problem to write message in http request\n",e);
            }
          
        } else {
            response.setHeader("Content-type", "null");
        }
    }
    
    public void processSendAE(HttpServletRequest request, HttpServletResponse response, Collection ae) throws HttpServletLpbCastException
    {
        if (ae != null) {
            System.out.println("Send state");
            response.setHeader("Content-type", "text/plain");
            try {
                ObjectOutputStream out = new ObjectOutputStream(response.getOutputStream());
                out.writeObject(ae);
                out.flush();
                out.close();
            } catch (IOException e) {
               throw new HttpServletLpbCastException(this.connection.getId()+" : Problem to write message in http request\n",e);
            }
        }else {
            response.setHeader("Content-type", "null");
        }
    }

    public void sendTo(Object to, Object toSend) 
    {
        this.connection.logger.info(this.connection.getId() + " Send a message to " + to + "\n\n"); 
        this.connection.getNeighbors().notifyNeighbor(to, toSend);

    }

    public boolean addNeighbor(Object from, Object neighbor)
    {
        boolean result = false;
        String neighborURL = "";
        if (neighbor != null && !((String) neighbor).equals(from) && !neighbor.equals("")) {
            try {
                neighborURL = NetUtil.normalize((String) neighbor);

                if (this.getNeighborsList().contains(neighbor)) {
                    return false;
                }
                if (from == null) {
                    this.getNeighbors().addNeighbor(neighborURL);
                    System.out.println("Add neighbor OK");
                    return true;
                }

                URL to;
                to = new URL(neighborURL + "/synchronize.do?test=true&url=" + from);
                HttpURLConnection init;
                init = (HttpURLConnection) to.openConnection();
                init.connect();
                String response = init.getHeaderField("Connected");

                if (response != null && response.equals("false")) {
                    result = false;
                } else {
                    this.getNeighbors().addNeighbor(neighborURL);
                    System.out.println("Add neighbor OK");
                    result = true;
                }
                init.disconnect();
            } catch (IOException e) {
                result=false;
                e.printStackTrace();
            } catch (LpbCastException e) {  
                result=false;
                e.printStackTrace();
            } catch (URISyntaxException e) {
                result=false;
                e.printStackTrace();
            } 

        } else {
            System.out.println("void neighbor or same : " + neighbor);
        }
        System.out.println(result);
        return result;
    }

    public Collection getNeighborsList() throws HttpServletLpbCastException 
    {
        return this.connection.getNeighborsList();
    }

    public void removeNeighbor(Object neighbor) throws HttpServletLpbCastException 
    {
        this.connection.removeNeighbor(neighbor);

    }

    public void clearWorkingDir()
    {
        this.connection.clearWorkingDir();
    }

}

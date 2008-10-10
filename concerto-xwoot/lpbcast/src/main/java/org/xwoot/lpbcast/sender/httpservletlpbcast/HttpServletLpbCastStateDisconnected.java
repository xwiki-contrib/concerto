package org.xwoot.lpbcast.sender.httpservletlpbcast;

import java.io.File;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xwoot.lpbcast.message.Message;
import org.xwoot.lpbcast.neighbors.Neighbors;

public class HttpServletLpbCastStateDisconnected extends HttpServletLpbCastState
{

    public HttpServletLpbCastStateDisconnected(HttpServletLpbCast connection)
    {
        super(connection);
    }

    public boolean addNeighbor(Object from, Object neighbor)
    {
        this.connection.logger.info(from + " Add neighbor when sender is disconnected -- return null.");
        return false;
    }

    @Override
    public void connectSender()
    {
        // connect somehow ...

        // finally set connected state of the connection instance
        this.connection.setState(this.connection.CONNECTED);
    }

    @Override
    public void disconnectSender()
    {
        throw new IllegalStateException("Already disconnected");
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

    public void gossip(Object from, Object message)
    {
        this.connection.logger.info(this.connection.getId()
            + " Try to send message when sender is disconnected -- send nothing.");
    }

    @Override
    public boolean isSenderConnected()
    {
        return false;
    }

    public void processSendState(HttpServletRequest request, HttpServletResponse response, File state)
    {
        this.connection.logger.info(" Try to send state when sender is disconnected -- send nothing.");

    }

    public void sendTo(Object peerId, Object toSend)
    {
        this.connection.logger.info(this.connection.getId() + " Try to send message to -- " + peerId
            + " -- when sender is disconnected -- send nothing.");

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

    public void processSendAE(HttpServletRequest request, HttpServletResponse response, Collection ae)
    {
        this.connection.logger.info(" Try to send anti entropy diff when sender is disconnected -- send nothing.");
    }

}

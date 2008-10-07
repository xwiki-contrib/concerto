package org.xwoot.lpbcast.sender.httpservletlpbcast;

import org.xwoot.lpbcast.sender.LpbCastAPI;

public abstract class HttpServletLpbCastState implements LpbCastAPI
{

    // childs need to have access to the connection instance too
    protected final HttpServletLpbCast connection;

    public HttpServletLpbCastState(HttpServletLpbCast connection)
    {
        this.connection = connection;
    }

    public abstract void connectSender();

    public abstract void disconnectSender();

    public abstract boolean isSenderConnected();
}

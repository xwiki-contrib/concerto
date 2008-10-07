/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.xwoot.manager;

import java.util.List;

/**
 * Helper component for managing an XWoot server.
 * 
 * @version $Id$
 */
public interface XWootManager
{
    /** This component's role, used when code needs to look it up. */
    String ROLE = XWootManager.class.getName();

    /**
     * Checks if the configured XWoot server is available, meaning that a connection can be established.
     * 
     * @return <code>true</code> if the server could be contacted and the
     */
    boolean isAvailable();

    boolean isInitialised();

    String getBootstrapURL();

    boolean isDocumentManaged(String documentName);

    void manageDocument(String documentName);

    void unmanageDocument(String documentName);

    boolean isWikiConnected();

    void disconnectWiki();

    void connectWiki();

    boolean isP2PConnected();

    void disconnectP2P();

    void connectP2P();

    List<String> listPeers();

    void synchronize();
}

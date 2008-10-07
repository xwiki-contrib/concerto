/**
 * 
 *        -- class header / Copyright (C) 2008  100 % INRIA / LGPL v2.1 --
 * 
 *  +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *  Copyright (C) 2008  100 % INRIA
 *  Authors :
 *                       
 *                       Gerome Canals
 *                     Nabil Hachicha
 *                     Gerald Hoster
 *                     Florent Jouille
 *                     Julien Maire
 *                     Pascal Molli
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 *  INRIA disclaims all copyright interest in the application XWoot written
 *  by :    
 *          
 *          Gerome Canals
 *         Nabil Hachicha
 *         Gerald Hoster
 *         Florent Jouille
 *         Julien Maire
 *         Pascal Molli
 * 
 *  contact : maire@loria.fr
 *  ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *  
 */

package org.xwoot.lpbcast.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;

import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class NetUtil
{
    /** DOCUMENT ME! */
    public static final int READ_TIME_OUT = 60000;

    private NetUtil()
    {
        // void;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param url DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws IOException
     * @throws Exception DOCUMENT ME!
     */
    public static synchronized File getFileViaHTTPRequest(URL url) throws IOException
    {
        URLConnection con = url.openConnection();

        if ((con == null) || con.getHeaderField("Content-type").equals("null")) {
            return null;
        }

        File file = File.createTempFile("state", ".zip");
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file.getAbsolutePath()));

        InputStream in = con.getInputStream();
        byte[] buffer = new byte[1024];
        int numRead;

        while ((numRead = in.read(buffer)) != -1) {
            bos.write(buffer, 0, numRead);
            bos.flush();
        }

        return file;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param url DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws URISyntaxException
     * @throws Exception DOCUMENT ME!
     */
    public static String normalize(String url) throws URISyntaxException
    {
        URI uri = new URI(url);
        uri = uri.normalize();

        String path = uri.getPath();

        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        String urlStr = uri.getScheme() + "://" + uri.getHost();
        int port = uri.getPort();

        if (port != -1) {
            urlStr = urlStr + ":" + port;
        }

        urlStr = urlStr + path;

        return urlStr;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param url DOCUMENT ME!
     * @param object DOCUMENT ME!
     * @throws IOException
     * @throws Exception DOCUMENT ME!
     */
    public static synchronized void sendObjectViaHTTPRequest(URL url, Object object) throws IOException
    {
        HttpURLConnection init = (HttpURLConnection) url.openConnection();
        init.setConnectTimeout(NetUtil.READ_TIME_OUT);
        init.setReadTimeout(NetUtil.READ_TIME_OUT);
        init.setUseCaches(false);
        init.setDoOutput(true);
        init.setRequestProperty("Content-type", "application/octet-stream");

        ObjectOutputStream out = new ObjectOutputStream(init.getOutputStream());
        out.writeObject(object);
        out.flush();
        try {
            init.getResponseCode();
        } catch (SocketTimeoutException s) {
            System.out.println("Read timed out - try another time...");
            init.getResponseCode();
        }

        out.close();
        init.disconnect();
    }

    /**
     * DOCUMENT ME!
     * 
     * @param url DOCUMENT ME!
     * @param header DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws IOException
     * @throws Exception DOCUMENT ME!
     */
    public static synchronized boolean testHTTPRequestHeader(URL url, String header) throws IOException
    {
        HttpURLConnection init = (HttpURLConnection) url.openConnection();

        init.setConnectTimeout(NetUtil.READ_TIME_OUT);
        init.setReadTimeout(NetUtil.READ_TIME_OUT);
        init.setUseCaches(false);

        init.setRequestMethod("GET");

        return (init.getHeaderField(header) != null);
    }
}

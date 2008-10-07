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

import java.net.InetAddress;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class Guid
{
    private static String hexServerIP = null;

    private static final java.security.SecureRandom SEEDER = new java.security.SecureRandom();

    private Guid()
    {
        // void
    }

    /**
     * A 32 byte GUID generator (Globally Unique ID).
     * 
     * @param o DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    public static final String generateGUID(Object o)
    {
        StringBuffer tmpBuffer = new StringBuffer(16);

        if (Guid.hexServerIP == null) {
            InetAddress localInetAddress = null;
            byte[] serverIP = new byte[] {127, 0, 0, 1};

            try {
                // get the inet address
                localInetAddress = InetAddress.getLocalHost();
                serverIP = localInetAddress.getAddress();
            } catch (java.net.UnknownHostException uhe) {
                // 
            }

            Guid.hexServerIP = Guid.hexFormat(Guid.getInt(serverIP), 8);
        }

        String hashcode = Guid.hexFormat(System.identityHashCode(o), 8);
        tmpBuffer.append(Guid.hexServerIP);
        tmpBuffer.append(hashcode);

        long timeNow = System.currentTimeMillis();
        int timeLow = (int) timeNow & 0xFFFFFFFF;
        int node = Guid.SEEDER.nextInt();

        StringBuffer guid = new StringBuffer(32);
        guid.append(Guid.hexFormat(timeLow, 8));
        guid.append(tmpBuffer.toString());
        guid.append(Guid.hexFormat(node, 8));

        return guid.toString();
    }

    private static int getInt(byte[] bytes)
    {
        int i = 0;
        int j = 24;

        for (int k = 0; j >= 0; k++) {
            int l = bytes[k] & 0xff;
            i += (l << j);
            j -= 8;
        }

        return i;
    }

    private static String hexFormat(int i, int j)
    {
        String s = Integer.toHexString(i);

        return Guid.padHex(s, j) + s;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param args DOCUMENT ME!
     */
    public static void main(String[] args)
    {
        Guid.generateGUID("tagada");
    }

    private static String padHex(String s, int i)
    {
        StringBuffer tmpBuffer = new StringBuffer();

        if (s.length() < i) {
            for (int j = 0; j < (i - s.length()); j++) {
                tmpBuffer.append('0');
            }
        }

        return tmpBuffer.toString();
    }
}

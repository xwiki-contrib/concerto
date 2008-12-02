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
 * A 32 byte GUID generator (Globally Unique ID).
 * 
 * @version $Id:$
 */
public final class Guid
{
    /** The server's ip address in hex format. */
    private static String hexServerIP;

    /** Secure random number generator. */
    private static final java.security.SecureRandom SEEDER = new java.security.SecureRandom();

    /** The default server ip in raw format to fall back on if getting the localhost fails. */
    private static final byte[] DEFAULT_SERVER_IP = new byte[] {127, 0, 0, 1};

    /** Disable utility class instantiation. */
    private Guid()
    {
        // void
    }

    /**
     * Generate a 32 byte GUID (Globally Unique ID) using the hashcode of the given object.
     * <p>
     * The resulted Guid looks something like: &lt;integer as lower part in bites of the current time&gt; &lt;the
     * server's IP address&gt; &lt;the object's hashcode&gt; &lt;a secure random integer&gt;, each representing 8 digit
     * hex numbers.
     * 
     * @param o the object to use.
     * @return the GUID in hex format.
     */
    public static String generateGUID(Object o)
    {
        StringBuffer tmpBuffer = new StringBuffer(16);

        if (Guid.hexServerIP == null) {
            InetAddress localInetAddress = null;
            byte[] serverIP = DEFAULT_SERVER_IP;

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

    /**
     * Convert a byte array to int.
     * 
     * @param bytes the byte array having 4 elements.
     * @return the int value.
     */
    protected static int getInt(byte[] bytes)
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

    /**
     * @param number the number to convert.
     * @param numberOfDigits the numberOfDigits of the resulting number.
     * @return the hex string number having the specified number of digits with padded 0s in fronf of it in order to
     *         reach that.
     */
    private static String hexFormat(int number, int numberOfDigits)
    {
        String s = Integer.toHexString(number);

        return Guid.padHex(s, numberOfDigits) + s;
    }

    /**
     * @param hexNumber the number in hex format to pad.
     * @param desiredLength the desired length to pad to.
     * @return a string containing the padded 0s needed by the number to reach the deiredLength in digits.
     */
    private static String padHex(String hexNumber, int desiredLength)
    {
        StringBuffer tmpBuffer = new StringBuffer();

        if (hexNumber.length() < desiredLength) {
            for (int j = 0; j < (desiredLength - hexNumber.length()); j++) {
                tmpBuffer.append('0');
            }
        }

        return tmpBuffer.toString();
    }
}

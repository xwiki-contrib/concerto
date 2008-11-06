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

package org.xwoot.xwootUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.codec.binary.Base64;

/**
 * DOCUMENT ME!
 * 
 * @author nabil
 */
public class FileUtil
{
    /** DOCUMENT ME! */
    public static final int BUFFER = 2048;

    private FileUtil()
    {
        // void
    }

    /**
     * DOCUMENT ME!
     * 
     * @param fileName DOCUMENT ME!
     * @param dstPath DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     */
    public static void copyFile(String fileName, String dstPath) throws IOException
    {
        FileChannel sourceChannel = new FileInputStream(fileName).getChannel();
        FileChannel destinationChannel = new FileOutputStream(dstPath).getChannel();
        sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);
        sourceChannel.close();
        destinationChannel.close();
    }

    /**
     * DOCUMENT ME!
     * 
     * @param strPath DOCUMENT ME!
     * @param dstPath DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     */
    public static void copyFiles(String strPath, String dstPath) throws IOException
    {
        File sourceDir = new File(strPath);

        for (File file : sourceDir.listFiles()) {
            if (file.isFile() && file.canRead()) {
                FileUtil.copyFile(file.toString(), dstPath + File.separator + file.getName());
            }
        }
    }

    private static void copyInputStream(InputStream in, OutputStream out) throws IOException
    {
        byte[] buffer = new byte[BUFFER];
        int len;

        while ((len = in.read(buffer)) >= 0) {
            out.write(buffer, 0, len);
        }

        in.close();
        out.close();
    }

    /**
     * DOCUMENT ME!
     * 
     * @param dir DOCUMENT ME!
     */
    public static void deleteDirectory(File dir)
    {
        if (dir.exists()) {
            String[] children = dir.list();

            for (String element : children) {
                File f = new File(dir, element);
                if (f.isDirectory()) {
                    deleteDirectory(f);
                } else {
                    f.delete();
                }
            }

            dir.delete();
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @param filename DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws UnsupportedEncodingException DOCUMENT ME!
     */
    public static String getDecodedFileName(String filename) throws UnsupportedEncodingException
    {
        String result = new String(Base64.decodeBase64(filename.getBytes("UTF-8")), "UTF-8");
        return result;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param pageId DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws UnsupportedEncodingException DOCUMENT ME!
     */
    public static String getEncodedFileName(String pageId) throws UnsupportedEncodingException
    {
        // System.out.println("encoded "+pageId+" =>"+Base64.encode(pageId.
        // getBytes("UTF-8")).trim()+"<=");
        return new String(Base64.encodeBase64(pageId.getBytes("UTF-8")), "UTF-8");
    }

    // load xwoot properties
    public static Properties loadXWootPropertiesFile(String xwootInitFile) throws Exception
    {
        // try to read properties from working dir value
        Properties xwootProps = new Properties();
        xwootProps.load(new FileInputStream(xwootInitFile));

        String workingDir = xwootProps.getProperty("xwoot.working.dir");

        if (workingDir == null) {
            throw new RuntimeException("Please specify a working directory.");
        }

        try {
            SecurityManager security = System.getSecurityManager();

            if (security != null) {
                security.checkWrite(workingDir);
            }
        } catch (SecurityException e) {
            throw new RuntimeException("The specified directory \"" + workingDir + "\" is not writable !", e);
        }

        // check delay
        String siteIdString = xwootProps.getProperty("xwoot.site.id");
        int siteId = -1;

        try {
            siteId = Integer.parseInt(siteIdString);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid site id value ! [unable to read an integer]", e);
        }

        if (!(siteId > 0)) {
            throw new RuntimeException("Invalid site id value ! [must be > 0]");
        }

        // check server url
        String serverUrlString = xwootProps.getProperty("xwoot.server.url");

        try {
            new URL(serverUrlString);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid server url !", e);
        }

        // check delay
        String delayString = xwootProps.getProperty("xwoot.refresh.log.delay");
        int delay = -1;

        try {
            delay = Integer.parseInt(delayString);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid delay value ! [unable to read an integer]", e);
        }

        if (!(delay > 0)) {
            throw new RuntimeException("Invalid delay value ! [must be > 0]");
        }

        // check neighbors number
        String neighborsNumbersString = xwootProps.getProperty("xwoot.neighbors.list.size");
        int neighborsNumber = -1;

        try {
            neighborsNumber = Integer.parseInt(neighborsNumbersString);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid neighbors number value ! [unable to read an integer]", e);
        }

        if (!(neighborsNumber > 0)) {
            throw new RuntimeException("Invalid neighborsNumber value ! [must be > 0]");
        }

        // check neighbors number
        String roundString = xwootProps.getProperty("xwoot.pbcast.round");
        int round = -1;

        try {
            round = Integer.parseInt(roundString);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid round value ! [unable to read an integer]", e);
        }

        if (!(round > 0)) {
            throw new RuntimeException("Invalid round value ! [must be > 0]");
        }

        String serverName = xwootProps.getProperty("xwoot.server.name");

        if (serverName == null) {
            serverName = "Chewbacca";
        }

        return xwootProps;
    }

    // private
    private static String normalizeName(String string)
    {
        String temp = FileUtil.removeAccents(string);

        return temp.replaceAll("[^\\p{ASCII}]", "");
    }

    /**
     * DOCUMENT ME!
     * 
     * @param s DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    public static String removeAccents(String s)
    {
        String sSemAcento = s;
        sSemAcento = sSemAcento.replaceAll("[áàâãä]", "a");
        sSemAcento = sSemAcento.replaceAll("[ÁÀÂÃÄ]", "A");
        sSemAcento = sSemAcento.replaceAll("[éèêë]", "e");
        sSemAcento = sSemAcento.replaceAll("[ÉÈÊË]", "E");
        sSemAcento = sSemAcento.replaceAll("[íìîï]", "i");
        sSemAcento = sSemAcento.replaceAll("[ÍÌÎÏ]", "I");
        sSemAcento = sSemAcento.replaceAll("[óòôõö]", "o");
        sSemAcento = sSemAcento.replaceAll("[ÓÒÔÕÖ]", "O");
        sSemAcento = sSemAcento.replaceAll("[úùûü]", "u");
        sSemAcento = sSemAcento.replaceAll("[ÚÙÛÜ]", "U");
        sSemAcento = sSemAcento.replaceAll("ç", "c");
        sSemAcento = sSemAcento.replaceAll("Ç", "C");
        sSemAcento = sSemAcento.replaceAll("ñ", "n");
        sSemAcento = sSemAcento.replaceAll("Ñ", "N");

        return sSemAcento;
    }

    /*
     * public static String zipDirectory(String dirPath) throws Exception { File dir = new File(dirPath); File file =
     * File.createTempFile(dir.getName(), "zip"); FileOutputStream fos = new FileOutputStream(file);
     * BufferedOutputStream bos = new BufferedOutputStream(fos); ZipOutputStream zos = new ZipOutputStream(bos);
     * zos.setMethod(ZipOutputStream.DEFLATED); zos.setLevel(Deflater.BEST_COMPRESSION); byte data[] = new byte[BUFFER];
     * String files[] = dir.list(); for (int i = 0; i < files.length; i++) { FileInputStream fi = new
     * FileInputStream(dirPath + File.separator + files[i]); BufferedInputStream buffer = new BufferedInputStream(fi,
     * BUFFER); ZipEntry entry = new ZipEntry(normalizeName(files[i])); zos.putNextEntry(entry); int count; while
     * ((count = buffer.read(data, 0, BUFFER)) != -1) { zos.write(data, 0, count); } zos.closeEntry(); buffer.close(); }
     * zos.close(); bos.close(); fos.close(); return file.getAbsolutePath(); }
     */
    /**
     * DOCUMENT ME!
     * 
     * @param zipFile DOCUMENT ME!
     * @param dirPath DOCUMENT ME!
     * @throws IOException
     * @throws FileNotFoundException
     * @throws Exception DOCUMENT ME!
     */
    public static List unzipInDirectory(ZipFile zipFile, String dirPath) throws IOException
    {
        Enumeration entries = zipFile.entries();
        ArrayList result = new ArrayList<String>();
        while (entries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            FileUtil.copyInputStream(zipFile.getInputStream(entry), new BufferedOutputStream(new FileOutputStream(
                dirPath + File.separator + entry.getName())));
            result.add(entry.getName());
        }

        zipFile.close();
        return result;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param dirPath DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws IOException
     * @throws Exception DOCUMENT ME!
     */
    public static String zipDirectory(String dirPath,String resultFilePath) throws IOException
    {
        File dir = new File(dirPath);
        String[] files = dir.list();

        if (files.length < 1) {
            return null;
        }

        File file = new File(resultFilePath);
        FileOutputStream fos = new FileOutputStream(file);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        ZipOutputStream zos = new ZipOutputStream(bos);
        zos.setMethod(ZipOutputStream.DEFLATED);
        zos.setLevel(Deflater.BEST_COMPRESSION);

        byte[] data = new byte[FileUtil.BUFFER];

        for (String file2 : files) {
            FileInputStream fi = new FileInputStream(dirPath + File.separator + file2);
            BufferedInputStream buffer = new BufferedInputStream(fi, FileUtil.BUFFER);
            ZipEntry entry = new ZipEntry(FileUtil.normalizeName(file2));
            zos.putNextEntry(entry);

            int count;

            while ((count = buffer.read(data, 0, FileUtil.BUFFER)) != -1) {
                zos.write(data, 0, count);
            }

            zos.closeEntry();
            buffer.close();
        }

        zos.close();
        bos.close();
        fos.close();

        return file.getAbsolutePath();
    }
    
    /**
     * Checks if the given directory path exists and if it's a valid and writable directory. If it doesn't exist, it is created.
     * 
     * @param directoryPath : the path to check.
     * @throws IOException if the directory did not exist and can not be created. It is also thrown if the path is valid but not writable.
     * @throws InvalidParameterException if the given path is valid but it points to a file instead of pointing to a directory.
     */
    public static void checkDirectoryPath(String directoryPath) throws IOException, InvalidParameterException
    {            
        File directory = new File(directoryPath);

        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new IOException("Can't create directory: " + directoryPath);
            }
        } else if (!directory.isDirectory()) {
            throw new InvalidParameterException(directoryPath + " -- is not a directory");
        } else if (!directory.canWrite()) {
            throw new IOException(directoryPath + " -- isn't writable");
        }
    }
}

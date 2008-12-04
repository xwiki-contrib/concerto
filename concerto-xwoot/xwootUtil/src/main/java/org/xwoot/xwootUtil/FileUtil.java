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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.codec.binary.Base64;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * DOCUMENT ME!
 * 
 * @author nabil
 */
public class FileUtil
{
    /** DOCUMENT ME! */
    public static final int BUFFER = 2048;

    public static final String TESTS_DIRECTORY_NAME = "xwootTests";

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

    /**
     * Copies data from the InputStream to the OutputStream using {@link #BUFFER} bytes at a time.
     * <p>
     * NOTE: Both streams are <b>not</b> automatically closed, so the user will have to take care of that.
     * 
     * @param in the source Stream.
     * @param out the destination Stream.
     * @throws IOException if transfer problems occur.
     */
    private static void copyInputStream(InputStream in, OutputStream out) throws IOException
    {
        byte[] buffer = new byte[BUFFER];
        int len;

        while ((len = in.read(buffer)) >= 0) {
            out.write(buffer, 0, len);
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @param dir DOCUMENT ME!
     */
    public static void deleteDirectory(File dir)
    {
        if (dir == null) {
            throw new NullPointerException("A null value was provided instead of a File object.");
        }

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

    public static void deleteDirectory(String directoryPath)
    {
        if (directoryPath == null || directoryPath.length() == 0) {
            throw new InvalidParameterException("An empty or null value was provided for directory path.");
        }

        deleteDirectory(new File(directoryPath));
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
     * Extracts a a zip file file to a directory.
     * 
     * @param zippedFilePath the location of the zip file.
     * @param dirPath the directory where to extract the zip file.
     * @throws IOException if I/O problems occur.
     * @throws ZipException if zip format errors occur.
     * @see ZipFile
     */
    public static List<String> unzipInDirectory(String zippedFilePath, String dirPath) throws IOException, ZipException
    {
        List<String> result = new ArrayList<String>();
        ZipFile zippedFile = null;

        try {
            zippedFile = new ZipFile(zippedFilePath);

            Enumeration< ? extends ZipEntry> entries = zippedFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String currentDestinationFilePath = dirPath + File.separator + entry.getName();

                InputStream currentZipEntryInputStream = zippedFile.getInputStream(entry);
                BufferedOutputStream bosToFile =
                    new BufferedOutputStream(new FileOutputStream(currentDestinationFilePath));

                try {
                    FileUtil.copyInputStream(currentZipEntryInputStream, bosToFile);
                } catch (IOException ioe) {
                    throw new IOException("Error unzipping entry " + entry.getName()
                        + ". Check disk space or write access.");
                } finally {
                    try {
                        currentZipEntryInputStream.close();
                        bosToFile.close();
                    } catch (Exception e) {
                        throw new IOException("Unable to close file " + currentDestinationFilePath);
                    }
                }

                result.add(entry.getName());
            }
        } finally {
            try {
                if (zippedFile != null) {
                    zippedFile.close();
                }
            } catch (Exception e) {
                throw new IOException("Unable to close zip file ");
            }
        }

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
    public static String zipDirectory(String dirPath, String resultFilePath) throws IOException
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
     * Zips a directory and saves the resulting file in the temporary directory. The file will be named
     * "&lt;directory_name&gt;.zip".
     * 
     * @param directoryPath the directory to zip.
     * @return the actual location of the resulting temporary zip file.
     * @throws IOException if problems occur.
     */
    public static String zipDirectory(String directoryPath) throws IOException
    {
        File tempFile = File.createTempFile(new File(directoryPath).getName(), ".zip");

        return zipDirectory(directoryPath, tempFile.toString());
    }

    /**
     * Checks if the given directory path exists and if it's a valid and writable directory. If it doesn't exist, it is
     * created.
     * 
     * @param directoryPath : the path to check.
     * @throws IOException if the directory did not exist and can not be created. It is also thrown if the path is valid
     *             but not writable.
     * @throws InvalidParameterException if the given path is valid but it points to a file instead of pointing to a
     *             directory.
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

    /**
     * Serializes an object to file using the {@link ObjectOutputStream#writeObject(Object)} method.
     * 
     * @param object the object to save.
     * @param filePath the path of the file where to save the object.
     * @throws Exception if problems occur saving the object.
     * @see {@link #loadObjectFromFile(String)}
     */
    public static void saveObjectToFile(Object object, String filePath) throws Exception
    {
        FileOutputStream fout = null;
        ObjectOutputStream oos = null;

        try {
            fout = new FileOutputStream(filePath);
            oos = new ObjectOutputStream(fout);

            oos.writeObject(object);
            oos.flush();
        } catch (Exception e) {
            throw new Exception("Problems while storing an object in the file: " + filePath, e);
        } finally {
            try {
                if (oos != null) {
                    oos.close();
                }
                if (fout != null) {
                    fout.close();
                }
            } catch (Exception e) {
                throw new Exception("Problems closing the file " + filePath + " after storing an object to it: ", e);
            }
        }
    }

    /**
     * Serializes a Collection to file using the {@link ObjectOutputStream#writeObject(Object)} method.
     * 
     * @param collection the collection to save.
     * @param filePath the path of the file where to save the collection.
     * @throws Exception if problems occur saving the object.
     * @see {@link #saveObjectToFile(Object, String)}
     * @see {@link #loadObjectFromFile(String)}
     */
    @SuppressWarnings("unchecked")
    public static void saveCollectionToFile(Collection collection, String filePath) throws Exception
    {
        if (collection == null) {
            throw new NullPointerException("A null value was provided instead of a valid collection.");
        }

        if (collection.isEmpty()) {
            File file = new File(filePath);

            if (file.exists()) {
                file.delete();
            }

            return;
        }

        saveObjectToFile(collection, filePath);
    }
    
    /**
     * Serializes a Map to file using the {@link ObjectOutputStream#writeObject(Object)} method.
     * 
     * @param map the map to save.
     * @param filePath the path of the file where to save the object.
     * @throws Exception if problems occur saving the object.
     * @see {@link #saveObjectToFile(Object, String)}
     * @see {@link #loadObjectFromFile(String)}
     */
    @SuppressWarnings("unchecked")
    public static void saveMapToFile(Map map, String filePath) throws Exception
    {
        if (map == null) {
            throw new NullPointerException("A null value was provided instead of a valid map.");
        }

        if (map.isEmpty()) {
            File file = new File(filePath);

            if (file.exists()) {
                file.delete();
            }

            return;
        }

        saveObjectToFile(map, filePath);
    }

    /**
     * Loads an object from file. The object must have been previously serialized using the
     * {@link ObjectOutputStream#writeObject(Object)} method.
     * 
     * @param filePath the file to load from.
     * @return the read object.
     * @throws Exception if the file was not found or problems reading the object occured.
     * @see #saveObjectToFile(Object, String)
     */
    public static Object loadObjectFromFile(String filePath) throws Exception
    {
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            fis = new FileInputStream(filePath);
            ois = new ObjectInputStream(fis);

            return ois.readObject();
        } catch (Exception e) {
            throw new Exception("Problems while loading an object from the file: " + filePath, e);
        } finally {
            try {
                if (ois != null) {
                    ois.close();
                }
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                throw new Exception("Problems closing the file " + filePath + " after loading an object from it: ", e);
            }
        }
    }

    public static void saveObjectToXml(Object object, String xmlFilePath) throws Exception
    {
        XStream xstream = new XStream(new DomDriver());
        Charset fileEncodingCharset = Charset.forName(System.getProperty("file.encoding"));

        OutputStreamWriter osw = null;
        PrintWriter output = null;

        try {
            osw = new OutputStreamWriter(new FileOutputStream(xmlFilePath), fileEncodingCharset);
            output = new PrintWriter(osw);
            output.print(xstream.toXML(object));
            output.flush();
        } catch (Exception e) {
            throw new Exception("Problems saving an object to xml file " + xmlFilePath, e);
        } finally {
            try {
                if (osw != null) {
                    osw.close();
                }
                if (output != null) {
                    output.close();
                }
            } catch (Exception e) {
                throw new Exception("Problems closing file " + xmlFilePath + " after saving an object in it.", e);
            }
        }
    }

    public static void saveCollectionToXml(Collection collection, String xmlFilePath) throws Exception
    {
        if (collection == null) {
            throw new NullPointerException("A null value was provided instead of a valid collection.");
        }

        if (collection.isEmpty()) {
            File file = new File(xmlFilePath);

            if (file.exists()) {
                file.delete();
            }

            return;
        }

        saveObjectToXml(collection, xmlFilePath);
    }

    public static Object loadObjectFromXml(String xmlFilePath) throws Exception
    {
        XStream xstream = new XStream(new DomDriver());

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(xmlFilePath);
            return xstream.fromXML(fis);
        } catch (Exception e) {
            throw new Exception("Problems loading object from file " + xmlFilePath, e);
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception e) {
                throw new Exception("Problems closing file " + xmlFilePath, e);
            }
        }

    }

    /**
     * @return the operating-system-independent temporary directory.
     */
    public static String getSystemTemporaryDirectory()
    {
        return System.getProperty("java.io.tmpdir");
    }

    /**
     * @return the working directory for tests.
     */
    public static String getTestsWorkingDirectoryPath()
    {
        return getSystemTemporaryDirectory() + File.separator + TESTS_DIRECTORY_NAME;
    }

    /**
     * @param moduleName the name of the module.
     * @return the working directory for tests for the specified module.
     */
    public static String getTestsWorkingDirectoryPathForModule(String moduleName)
    {
        if (moduleName == null || moduleName.length() == 0) {
            throw new InvalidParameterException("Module name must not be null or empty.");
        }

        return getTestsWorkingDirectoryPath() + File.separator + moduleName;
    }
}

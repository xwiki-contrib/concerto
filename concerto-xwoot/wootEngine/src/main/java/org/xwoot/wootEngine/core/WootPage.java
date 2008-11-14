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

package org.xwoot.wootEngine.core;

import org.xwoot.xwootUtil.FileUtil;

import org.xwoot.wootEngine.WootEngineException;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class WootPage implements Serializable
{

    private static final long serialVersionUID = -7726342430092268721L;

    /** DOCUMENT ME! */
    public final static String SAVED_FILE_EXTENSION = ".sav";

    private List<WootRow> rows = new ArrayList<WootRow>();

    private String pageName;

    private boolean isSavedPage;

    // constructor
    /**
     * Creates a new WootPage object.
     * 
     * @param b DOCUMENT ME!
     */
    public WootPage(boolean addStartEndMakers)
    {
        if (addStartEndMakers) {
            this.getRows().add(WootRow.RB);
            this.getRows().add(WootRow.RE);
        }
    }
    
    public WootPage(String pageName) throws IllegalArgumentException
    {
        this(true);
        setPageName(pageName);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param wr DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    public boolean contains(WootRow wr)
    {
        return this.rows.contains(wr);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param id DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    public boolean containsById(WootId id)
    {
        return this.indexOfId(id) >= 0;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param i DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    public WootRow elementAt(int i)
    {
        return this.rows.get(i);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param o DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    @Override
    public boolean equals(Object o)
    {
        // assert o instanceof WootPage;
        return (o instanceof WootPage) && ((WootPage) o).pageName.equals(this.pageName)
            && ((WootPage) o).rows.equals(this.rows);
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     * @throws UnsupportedEncodingException DOCUMENT ME!
     */
    public String getFileName() throws WootEngineException
    {
        String filename = "";
        
        try {
            if (this.isSavedPage) {
                filename = FileUtil.getEncodedFileName(this.getPageName() + WootPage.SAVED_FILE_EXTENSION);
            }
             else {
                filename = FileUtil.getEncodedFileName(this.getPageName());
            }
        }catch (UnsupportedEncodingException e) {
           throw new WootEngineException("Problem with filename encoding",e);
        }

        /* filename=this.getPageName(); */
        return filename;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public String getPageName()
    {
        return this.pageName;
    }

    /*
     * public String getId() { return id; } public void setId(String id) { this.id = id; }
     */
    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public List<WootRow> getRows()
    {
        return this.rows;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param wr DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    public int indexOf(WootRow wr)
    {
        // boolean b = this.contains(wr);
        return this.getRows().indexOf(wr) - 1;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param id DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    public int indexOfId(WootId id)
    {
        int i = 0;

        for (WootRow r : this.rows) {
            if (r.getWootId().equals(id)) {
                return i;
            }

            ++i;
        }

        return -1;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param from DOCUMENT ME!
     * @param id DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    public int indexOfIdFrom(int from, WootId id)
    {
        for (int i = from + 1; i < this.rows.size(); ++i) {
            if (this.rows.get(i).getWootId().equals(id)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param index DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    public int indexOfVisible(int index)
    {
        if ((index >= this.getRows().size()) || (index < 0)) {
            return -1;
        }

        int count = 0;
        int j = 0;

        for (WootRow r : this.rows) {
            if (r.isVisible()) {
                // count++;
                if (count == index) {
                    return j;
                }

                count++;
            }

            j++;
        }

        return -1;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param from DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    public int indexOfVisibleNext(int from)
    {
        int n = this.rows.size();

        if ((from < 0) || (from >= n)) {
            return -1;
        }

        for (int i = from + 1; i < n; ++i) {
            if (this.rows.get(i).isVisible()) {
                return i;
            }
        }

        return -1;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param wr DOCUMENT ME!
     * @param pos DOCUMENT ME!
     */
    public void insert(WootRow wr, int pos)
    {
        this.rows.add(pos + 1, wr);
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public boolean isEmpty()
    {
        return this.size() == 0;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public boolean isSavedPage()
    {
        return this.isSavedPage;
    }

    /**
     * @param pageName the pageName to set.
     * @throws IllegalArgumentException if pageName is a null or empty String.
     */
    public void setPageName(String pageName) throws IllegalArgumentException
    {
        if (pageName == null || pageName.isEmpty())
            throw new IllegalArgumentException("Empty page names not allowed.");
        
        this.pageName = pageName;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param rows DOCUMENT ME!
     */
    public void setRows(List<WootRow> rows)
    {
        this.rows = rows;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param isSavedPage DOCUMENT ME!
     */
    public void setSavedPage(boolean isSavedPage)
    {
        this.isSavedPage = isSavedPage;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public int size()
    {
        return this.rows.size() - 2;
    }

    /*
     * public WootRow getR(int pos) { if (pos == 0) { return WootRow.RB; } int nb = 0; for (int i = 0; i < this.size();
     * i++) { WootRow res; if ((res = this.elementAt(i)).isVisible()) { nb++; } if (nb == pos) { return res; } } return
     * WootRow.RE; }
     */
    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public String toHumanString()
    {
        StringBuffer sb = new StringBuffer();

        for (int i = 1; i < (this.rows.size() - 1); i++) {
            WootRow r = this.rows.get(i);

            if (r.isVisible()) {
                sb.append(r.getValue());
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    // public List subSeq(WootRow b, WootRow e) {
    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        Iterator i = this.getRows().iterator();

        while (i.hasNext()) {
            WootRow r = (WootRow) i.next();
            sb.append(r.getValue());
        }

        return sb.toString();
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public String toStringInternal()
    {
        StringBuffer sbfull = new StringBuffer();

        for (WootRow r : this.rows) {
            sbfull.append(r.toString());
        }

        return sbfull.toString();
    }

    @Override
    public int hashCode()
    {
        // TODO Auto-generated method stub
        return super.hashCode();
    }
}

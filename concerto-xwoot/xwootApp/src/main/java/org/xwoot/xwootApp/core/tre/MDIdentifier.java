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

package org.xwoot.xwootApp.core.tre;

import org.xwoot.thomasRuleEngine.core.Identifier;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class MDIdentifier extends Identifier
{
    /**  */
    private static final long serialVersionUID = 4717183124930677075L;

    private String pageName;

    private String metaDataId;

    private String id;

    /**
     * Creates a new MDIdentifier object.
     * 
     * @param pageName DOCUMENT ME!
     * @param metaDataId DOCUMENT ME!
     */
    public MDIdentifier(String pageName, String metaDataId)
    {
        this.pageName = pageName;
        this.metaDataId = metaDataId;
        this.setId();
    }

    /**
     * DOCUMENT ME!
     * 
     * @param with DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    @Override
    public boolean equals(Object with)
    {
        if (!(with instanceof Identifier)) {
            return false;
        }

        return this.id.equals(((MDIdentifier) with).getId());
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public String getId()
    {
        return this.id;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public String getMetaDataId()
    {
        return this.metaDataId;
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

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    @Override
    public int hashCode()
    {
        return this.id.hashCode();
    }

    private void setId()
    {
        this.id = this.pageName + "." + this.metaDataId;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param metaDataId DOCUMENT ME!
     */
    public void setMetaDataId(String metaDataId)
    {
        this.metaDataId = metaDataId;
        this.setId();
    }

    /**
     * DOCUMENT ME!
     * 
     * @param pageName DOCUMENT ME!
     */
    public void setPageName(String pageName)
    {
        this.pageName = pageName;
        this.setId();
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    @Override
    public String toString()
    {
        return this.id;
    }
}

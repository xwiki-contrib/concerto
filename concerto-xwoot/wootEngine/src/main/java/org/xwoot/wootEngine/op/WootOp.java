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

package org.xwoot.wootEngine.op;

import org.xwoot.wootEngine.core.WootId;
import org.xwoot.wootEngine.core.WootPage;

/**
 * Describes the structure of a Woot operation that each class implementing this interface will have to provide.
 * 
 * @version $Id$
 */
public interface WootOp
{
    /**
     * Applies this operation on a WootPage.
     * <p>
     * Users have to implement the behavior of the operation trough this method.
     * 
     * @param page the page where to execute this operation.
     */
    void execute(WootPage page);

    /**
     * Checks whether this operation can be applied on a page or not.
     * 
     * @param page the page the check.
     * @return true if it can be applied, false otherwise.
     */
    boolean canExecute(WootPage page);

    /**
     * @param page the page this operation affects.
     * @return the indexes of the {@link WootRow}s affected by this operation.
     */
    Object getAffectedRowIndexes(WootPage page);

    /**
     * @return the associated WootId of this operation.
     */
    WootId getOpId();

    /**
     * @param opId the opId to set.
     * @see #getOpId()
     */
    void setOpId(WootId opId);

    /**
     * @return the name of the page on which this operation will be applied.
     */
    String getPageName();

    /**
     * @param pageName the pageName to set.
     * @see #getPageName()
     */
    void setPageName(String pageName);
}

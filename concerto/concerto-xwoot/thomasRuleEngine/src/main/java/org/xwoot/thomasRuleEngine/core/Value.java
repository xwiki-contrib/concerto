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
package org.xwoot.thomasRuleEngine.core;

import java.io.Serializable;

/**
 * Defines the values stored in {@link Entry} objects.
 * 
 * @version $Id$
 */
public interface Value extends Serializable
{
    /** @return the actual value of this instance. */
    Serializable get();

    /** {@inheritDoc} */
    @Override
    String toString();

    /** {@inheritDoc} */
    @Override
    boolean equals(Object with);

    /** {@inheritDoc} */
    @Override
    int hashCode();
}

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
package org.xwiki.rest;

import java.util.Iterator;
import java.util.List;

/**
 * This class wraps a List<T> and provides a view of it as if had only the elements that were included in the specified
 * range (start, number)
 * 
 * @version $Id$
 */
public class RangeIterable<T> implements Iterable<T>
{
    private List<T> list;

    private int start;

    private int number;

    public RangeIterable(List<T> list, int start, int number)
    {
        this.list = list;

        if (start < 0) {
            start = 0;
        }

        if (start > list.size()) {
            start = list.size();
        }

        if (number < 0) {
            number = list.size();
        }

        if (start + number > list.size()) {
            number = list.size() - start;
        }

        this.start = start;
        this.number = number;

    }

    public Iterator<T> iterator()
    {
        return new Iterator<T>()
        {
            private int i = 0;

            public boolean hasNext()
            {
                if (i < number) {
                    if (i + start < list.size()) {
                        return true;
                    }
                }

                return false;
            }

            public T next()
            {
                T result = list.get(i + start);
                i++;

                return result;
            }

            public void remove()
            {
            }
        };
    }
}

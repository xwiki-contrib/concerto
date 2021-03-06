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
package org.xwiki.rest.resources.pages;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.xwiki.rest.DomainObjectFactory;
import org.xwiki.rest.RangeIterable;
import org.xwiki.rest.Utils;
import org.xwiki.rest.XWikiResource;
import org.xwiki.rest.model.jaxb.Pages;

import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Document;

/**
 * @version $Id$
 */
@Path("/wikis/{wikiName}/spaces/{spaceName}/pages")
public class PagesResource extends XWikiResource
{
    @GET
    public Pages getPages(@PathParam("wikiName") String wikiName, @PathParam("spaceName") String spaceName,
        @QueryParam("start") @DefaultValue("0") Integer start,
        @QueryParam("number") @DefaultValue("-1") Integer number, @QueryParam("parent") String parentFilterExpression)
        throws XWikiException
    {
        String database = xwikiContext.getDatabase();

        Pages pages = objectFactory.createPages();

        /* This try is just needed for executing the finally clause. Exceptions are actually re-thrown. */
        try {
            xwikiContext.setDatabase(wikiName);

            List<String> pageNames = xwikiApi.getSpaceDocsName(spaceName);
            Collections.sort(pageNames);

            Pattern parentFilter = null;
            if (parentFilterExpression != null) {
                if (parentFilterExpression.equals("null")) {
                    parentFilter = Pattern.compile("");
                } else {
                    parentFilter = Pattern.compile(parentFilterExpression);
                }
            }

            RangeIterable<String> ri = new RangeIterable<String>(pageNames, start, number);

            for (String pageName : ri) {
                String pageFullName = Utils.getPageId(wikiName, spaceName, pageName);

                if (!xwikiApi.exists(pageFullName)) {
                    logger.warning(String
                        .format("[Page '%s' appears to be in space '%s' but no information is available.]", pageName,
                            spaceName));
                } else {
                    Document doc = xwikiApi.getDocument(pageFullName);

                    /* We only add pages we have the right to access */
                    if (doc != null) {
                        boolean add = true;

                        if (parentFilter != null) {
                            String parent = doc.getParent();
                            if (parent == null) {
                                parent = "";
                            }

                            add = parentFilter.matcher(doc.getParent()).matches();
                        }

                        if (add) {
                            pages.getPageSummaries().add(
                                DomainObjectFactory.createPageSummary(objectFactory, uriInfo.getBaseUri(), doc));
                        }
                    }
                }
            }
        } finally {
            xwikiContext.setDatabase(database);
        }

        return pages;
    }
}

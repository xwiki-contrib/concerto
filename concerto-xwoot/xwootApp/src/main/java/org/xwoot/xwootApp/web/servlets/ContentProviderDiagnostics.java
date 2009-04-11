package org.xwoot.xwootApp.web.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xwoot.contentprovider.XWootContentProviderInterface;
import org.xwoot.xwootApp.XWootAPI;
import org.xwoot.xwootApp.web.XWootSite;

public class ContentProviderDiagnostics extends HttpServlet
{
    private static final long serialVersionUID = -3266228974643536434L;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
        IOException
    {
        XWootSite site = XWootSite.getInstance();
        XWootAPI xwootAPI = site.getXWootEngine();
        if (xwootAPI == null) {
            response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/bootstrap.do"));
            return;
        }

        XWootContentProviderInterface xwcp = xwootAPI.getContentProvider();
        request.setAttribute("content_provider", xwcp);
        request.setAttribute("config", xwcp.getConfiguration());
        request.setAttribute("entries", xwcp.getEntries(null, 0, -1));

        request.getRequestDispatcher("/pages/ContentProviderDiagnostics.jsp").forward(request, response);

    }

}

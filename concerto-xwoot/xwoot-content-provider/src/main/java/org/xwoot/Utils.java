package org.xwoot;

import java.util.ArrayList;
import java.util.List;

import org.xwiki.xmlrpc.model.XWikiPage;

/**
 * A class containing utility methods.
 * 
 * @version $Id$
 */
public class Utils
{
    /**
     * Convert an XWikiPage to an XWootObject.
     * 
     * @param page The XWikiPage to be converted.
     * @param newlyCreated The value of the newlyCreated field in the returned XWootObject.
     * @return The XWootObject corresponding to the XWiki page.
     */
    public static XWootObject xwikiPageToXWootObject(XWikiPage page, boolean newlyCreated)
    {
        List<XWootObjectField> fields = new ArrayList<XWootObjectField>();

        XWootObjectField field;

        field = new XWootObjectField("content", page.getContent(), true);
        fields.add(field);

        field = new XWootObjectField("title", page.getTitle(), true);
        fields.add(field);

        XWootObject result =
            new XWootObject(page.getId(), String.format("%s:%s", Constants.PAGE_NAMESPACE, page.getId()), false,
                fields, newlyCreated);

        return result;
    }

    /**
     * This function removes from the current object all the fields that have not changed with respect to the reference
     * object.
     * 
     * @param currentObject
     * @param referenceObject
     * @return A copy of currentObject with all the fields that haven't changed with respect to the reference object
     *         removed.
     */
    public static XWootObject removeUnchangedFields(XWootObject currentObject, XWootObject referenceObject)
    {
        List<XWootObjectField> resultFields = new ArrayList<XWootObjectField>();

        for (XWootObjectField field : currentObject.getFields()) {
            if (field.getValue() != null) {
                if (!field.getValue().equals(referenceObject.getFieldValue(field.getName()))) {
                    resultFields.add(field);
                }
            }
        }

        return new XWootObject(currentObject.getPageId(), currentObject.getGuid(), currentObject.isCumulative(),
            resultFields, currentObject.isNewlyCreated());
    }
}

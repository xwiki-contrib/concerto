package org.xwoot.wootEngine.test;

import junit.framework.Assert;

import org.junit.Test;
import org.xwoot.wootEngine.core.WootContent;
import org.xwoot.wootEngine.WootEngineException;

/**
 * Tests multiples contents id in one page.
 * 
 * @version $Id$
 */
public class MultiIdTest extends AbstractWootEngineTest
{

    /**
     * Tests multiples contents id in one page.
     * 
     * @throws WootEngineException for serializing/deserializing problems
     */
    @Test
    public void basicTest() throws WootEngineException
    {
        WootContent wootContent =
            this.site0.getContentManager().loadWootContent(this.pageName, this.objectId, this.fieldId);

        this.site0.insert(wootContent, this.line1, 0);
        this.site0.insert(wootContent, this.line2, 1);
        this.site0.insert(wootContent, this.line3, 2);

        this.site0.getContentManager().unloadWootContent(wootContent);

        Assert.assertEquals(wrapStartEndMarkers(this.line1 + this.line2 + this.line3), this.site0.getContentManager()
            .getContentInternal(this.pageName, this.objectId, this.fieldId));

        WootContent wootContent2 =
            this.site0.getContentManager().loadWootContent(this.pageName, this.objectId + "2", this.fieldId);
        this.site0.insert(wootContent2, this.line1, 0);
        this.site0.insert(wootContent2, this.line2, 1);
        this.site0.insert(wootContent2, this.line3, 2);
        this.site0.getContentManager().unloadWootContent(wootContent2);
        Assert.assertEquals(wrapStartEndMarkers(this.line1 + this.line2 + this.line3), this.site0.getContentManager()
            .getContentInternal(this.pageName, this.objectId, this.fieldId));
        Assert.assertEquals(wrapStartEndMarkers(this.line1 + this.line2 + this.line3), this.site0.getContentManager()
            .getContentInternal(this.pageName, this.objectId + "2", this.fieldId));
    }
}

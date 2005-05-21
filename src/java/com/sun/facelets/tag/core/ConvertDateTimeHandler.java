/**
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * Licensed under the Common Development and Distribution License,
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.sun.com/cddl/
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.sun.facelets.tag.core;

import java.util.TimeZone;

import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.convert.Converter;
import javax.faces.convert.DateTimeConverter;

import com.sun.facelets.FaceletContext;
import com.sun.facelets.FaceletException;
import com.sun.facelets.tag.AbstractComponentHandler;
import com.sun.facelets.tag.ConvertHandler;
import com.sun.facelets.tag.TagAttribute;
import com.sun.facelets.tag.TagAttributeException;
import com.sun.facelets.tag.TagConfig;

/**
 * Register a DateTimeConverter instance on the UIComponent associated with the
 * closest parent UIComponent custom action. <p/> See <a target="_new"
 * href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/tlddocs/f/convertDateTime.html">tag
 * documentation</a>.
 * 
 * @author Jacob Hookom
 * @version $Id: ConvertDateTimeHandler.java,v 1.1 2005/05/21 17:54:43 jhook Exp $
 */
public final class ConvertDateTimeHandler extends ConvertHandler {

    protected final TagAttribute dateStyle;

    protected final TagAttribute locale;

    protected final TagAttribute pattern;

    protected final TagAttribute timeStyle;

    protected final TagAttribute timeZone;

    protected final TagAttribute type;

    /**
     * @param config
     */
    public ConvertDateTimeHandler(TagConfig config) {
        super(config);
        this.dateStyle = this.getAttribute("dateStyle");
        this.locale = this.getAttribute("locale");
        this.pattern = this.getAttribute("pattern");
        this.timeStyle = this.getAttribute("timeStyle");
        this.timeZone = this.getAttribute("timeZone");
        this.type = this.getAttribute("type");
    }

    /**
     * Returns a new DateTimeConverter
     * 
     * @see DateTimeConverter
     * @see com.sun.facelets.tag.ConvertHandler#createConverter(com.sun.facelets.FaceletContext)
     */
    protected Converter createConverter(FaceletContext ctx)
            throws FacesException, ELException, FaceletException {
        return new DateTimeConverter();

    }

    /**
     * Implements tag spec, see taglib documentation.
     * 
     * @see com.sun.facelets.tag.ObjectHandler#setAttributes(com.sun.facelets.FaceletContext,
     *      java.lang.Object)
     */
    protected void setAttributes(FaceletContext ctx, Object obj) {
        DateTimeConverter c = (DateTimeConverter) obj;
        if (this.locale != null) {
            c.setLocale(AbstractComponentHandler.getLocale(ctx, this.locale));
        }
        if (this.pattern != null) {
            c.setPattern(this.pattern.getValue());
        } else {
            if (this.type != null) {
                c.setType(this.type.getValue(ctx));
            }
            if (this.dateStyle != null) {
                c.setDateStyle(this.dateStyle.getValue(ctx));
            }
            if (this.timeStyle != null) {
                c.setTimeStyle(this.timeStyle.getValue(ctx));
            }
            if (this.timeZone != null) {
                Object t = this.timeZone.getObject(ctx);
                if (t instanceof TimeZone) {
                    c.setTimeZone((TimeZone) t);
                } else if (t instanceof String) {
                    c.setTimeZone(TimeZone.getTimeZone((String) t));
                } else {
                    throw new TagAttributeException(
                            this.tag,
                            this.timeZone,
                            "Illegal TimeZone, must evaluate to either a java.util.TimeZone or String, is type: "
                                    + t);
                }
            }
        }
    }

    /**
     * Always returns true, blocks all unknown attributes
     * @param n
     * @return
     */
    protected boolean isAttributeHandled(String n) {
        return true;
    }
}
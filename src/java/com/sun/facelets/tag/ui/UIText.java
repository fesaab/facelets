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

package com.sun.facelets.tag.ui;

import java.io.IOException;

import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.sun.facelets.el.ELText;

/**
 * @author Jacob Hookom
 * @version $Id: UIText.java,v 1.1 2005/05/21 17:54:57 jhook Exp $
 */
public final class UIText extends UIComponentBase {

    protected final ELText txt;

    public UIText(ELText txt) {
        this.txt = txt;
    }

    public String getFamily() {
        return null;
    }

    public void encodeBegin(FacesContext context) throws IOException {
        ResponseWriter out = context.getResponseWriter();
        txt.write(out, context.getELContext());
    }

    public String getRendererType() {
        return null;
    }

    public boolean getRendersChildren() {
        return true;
    }

    public boolean isRendered() {
        return true;
    }

    public boolean isTransient() {
        return true;
    }
}
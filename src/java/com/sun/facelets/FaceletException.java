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

package com.sun.facelets;

import javax.faces.FacesException;

/**
 * An Exception from Facelet implementation
 * 
 * @author Jacob Hookom
 * @version $Id: FaceletException.java,v 1.1 2005/05/21 17:55:00 jhook Exp $
 */
public class FaceletException extends FacesException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    public FaceletException() {
        super();
    }

    /**
     * @param message
     */
    public FaceletException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public FaceletException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public FaceletException(String message, Throwable cause) {
        super(message, cause);
    }

}
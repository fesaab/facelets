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

package com.sun.facelets.tag;

/**
 * Provides the ability to completely change the Tag before it's processed for
 * compiling with the associated TagHandler.
 * <p />
 * You could take &lt;input type="text" /> and convert it to &lth:inputText />
 * before compiling.
 * 
 * @author Jacob Hookom
 * @version $Id: TagDecorator.java,v 1.1 2005/05/21 17:54:39 jhook Exp $
 */
public interface TagDecorator {

    /**
     * If handled, return a new Tag instance, otherwise return null
     * 
     * @param tag
     *            tag to be decorated
     * @return a decorated tag, otherwise null
     */
    public Tag decorate(Tag tag);
}
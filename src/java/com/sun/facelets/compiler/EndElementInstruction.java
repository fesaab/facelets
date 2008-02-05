/**
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

package com.sun.facelets.compiler;


import java.io.IOException;

import javax.el.ELContext;
import javax.el.ExpressionFactory;

import javax.faces.component.HackComponentPackagePrivateAccess;
import javax.faces.context.FacesContext;

import javax.faces.component.UIComponent;

final class EndElementInstruction implements Instruction {
    private final String element;

    public EndElementInstruction(String element) {
        this.element = element;
    }

    public void write(FacesContext context, UIComponent component) throws IOException {
        // The implementation needs to add afterStart and beforeEnd listeners
        // to the html, head, body, and form elements.  It needs to handle the 
        // case where the element is declared in template text (template case),
        // and also the
        // case where the element is generated by a UIComponent.  
        // (component case).  The listener
        // lists for these events must be stored in the view and participate in
        // the view state.  
        
        // Template Case

        //   The template case is handled by modifying the StartElementInstruction
        //   and the EndElementInstruction to check for the existence of a 
        //   listener for the appropriate event, for that kind of element
        
        // Component Case
        
        //   This only works for components that use the delegated rendering
        //   model.  
        
        //   We modify the contract of Renderer such that if it adds an 
        //   attribute to the component attribute map during its encodeBegin 
        //   with the key Renderer.TOP_LEVEL_ELEMENT_ATTR_NAME (value = 
        //   "javax.faces.render.TOP_LEVEL_ELEMENT", and the value equal to the 
        //   string of the element that it will render for the top level markup,
        //   The runtime will be able to notify listeners for that kind of 
        //   markup of the afterStartElement and beforeEndElement events 
        //   pertaining to that markup.  
        
        //   UIComponentBase.encodeBegin has a new contract that says, after
        //   calling renderer.encodeBegin(), check the attribute
        //   set for an entry under the key Renderer.TOP_LEVEL_ELEMENT_ATTR_NAME,
        //   Check for the existence of a
        //   listener for the appropriate event (afterStart or beforeEnd), for 
        //   that kind of element listener.  
        
        //   Do the same thing for the encodeEnd, but do it BEFORE calling
        //   the renderer.  Remove the entry in the component attribute set
        //   before returning.
        
        // I want a way for components to be told they have been added to the 
        // view.  The easiest way is to add a methods to component/renderer.  
        // Something like: willAddToParent(), didAddToParent().  Components 
        // can then use these hooks to register for different lifecycle events,
        // such as "before/after tree render", "before/after component render"
        
        // Ask the facesContext: do we have any listeners for this
        // write text event?
        
        // UIViewRoot keeps a Map<String element, List<ComponentLifecycleListener>> for
        // each kind of renderlistener.
        // It exposes an addAfterStartElementListener(), addBeforeEndElementListener()
        
        // It would be really great to have a jsf-api internal way for the
        // pdl to get access to the map so it could be quickly tested for 
        // membership, without exposing the map in the public API.  C++
        // friend classes anyone?  Talk to Stanley Ho.  I thing the packaging
        // API allows this.  In the meantime, we use a hack to access package 
        // private methods on UIViewRoot.
        
        // Add a boolean property to FacesContext: ajaxEnabled
        
        // The runtme adds a beforeEndElementListener for any component that
        // renders a <form>.
        
        HackComponentPackagePrivateAccess.packageCallBeforeEndContextCallbacks(
                context.getViewRoot(), context, this.element);
        context.getResponseWriter().endElement(this.element);
    }

    public Instruction apply(ExpressionFactory factory, ELContext ctx) {
        return this;
    }

    public boolean isLiteral() {
        return true;
    }
}

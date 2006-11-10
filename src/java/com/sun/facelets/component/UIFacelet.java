package com.sun.facelets.component;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.application.StateManager;
import javax.faces.component.ContextCallback;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.context.ResponseWriterWrapper;

import com.sun.facelets.event.EventCallback;
import com.sun.facelets.event.Events;
import com.sun.facelets.util.FastWriter;

public class UIFacelet extends UIViewRoot {

    private static final Logger log = Logger.getLogger("facelets.UIFacelet");

    public static final ContextCallback Update = new ContextCallback() {
        public void invokeContextCallback(FacesContext faces, UIComponent c) {
            if (log.isLoggable(Level.FINE)) {
                log.fine("Updating[" + c.getClientId(faces) + "] "
                        + c.getClass().getName());
            }
            c.processUpdates(faces);
        }
    };

    public static final ContextCallback Validate = new ContextCallback() {
        public void invokeContextCallback(FacesContext faces, UIComponent c) {
            if (log.isLoggable(Level.FINE)) {
                log.fine("Validating[" + c.getClientId(faces) + "] "
                        + c.getClass().getName());
            }
            c.processValidators(faces);
        }
    };

    public static final ContextCallback Encode = new ContextCallback() {
        public void invokeContextCallback(FacesContext faces, UIComponent c) {
            try {
                if (log.isLoggable(Level.FINE)) {
                    log.fine("Encoding[" + c.getClientId(faces) + "] "
                            + c.getClass().getName());
                }
                c.encodeAll(faces);
            } catch (IOException e) {
                throw new FacesException(e);
            }
        }
    };

    public static final String PARAM_ENCODE = "javax.faces.Encode";

    public static final String PARAM_UPDATE = "javax.faces.Update";

    public final static String PARAM_ASYNC = "javax.faces.Async";

    private transient Set<String> encode = new HashSet<String>();

    private transient Set<String> update = new HashSet<String>();

    public UIFacelet() {
        super();
    }

    private void process(FacesContext faces, Set<String> ids, ContextCallback c) {
        UIViewRoot root = faces.getViewRoot();
        boolean success = false;
        for (String id : ids) {
            success = root.invokeOnComponent(faces, id, c);
            if (!success) {
                log.warning(id + " not found");
            }
        }
    }

    public void processUpdates(FacesContext faces) {
        log.fine("Processing Updates");
        if (!this.update.isEmpty()) {
            this.process(faces, this.update, Update);
        } else {
            super.processUpdates(faces);
        }
    }

    public void processValidators(FacesContext faces) {
        log.fine("Processing Validators");
        if (!this.update.isEmpty()) {
            this.process(faces, this.update, Validate);
        } else {
            super.processValidators(faces);
        }
    }

    public void encodeAll(FacesContext faces) throws IOException {

        // determine event callback
        EventCallback c = Events.getEventCallback(faces);
        if (c != null && !c.isImmediate()) {
            if (log.isLoggable(Level.FINE)) {
                log.fine(c.toString());
            }
            c.invoke(faces);
        }

        // see if there were any extras queued
        boolean partial = this.encodeQueued(faces);

        if (!partial && c == null && !AsyncResponse.exists()) {
            super.encodeAll(faces);
        } else {
            if (log.isLoggable(Level.FINE)) {
                log.fine("Skipped EncodeAll since Async was queued");
            }
        }
        
        if (log.isLoggable(Level.FINE)) {
            Iterator<FacesMessage> itr = faces.getMessages();
            FacesMessage msg;
            while (itr.hasNext()) {
                msg = itr.next();
                log.fine(msg.getSeverity() + " " + msg.getDetail());
            }
        }
    }

    private boolean encodeQueued(FacesContext faces) throws IOException {
        if (!this.encode.isEmpty()) {
            ExternalContext ctx = faces.getExternalContext();
            Object resp = ctx.getResponse();
            
            AsyncResponse async = AsyncResponse.getInstance();
            if (async == null) return false;

            UIViewRoot root = faces.getViewRoot();
            boolean success = false;
            ResponseWriter rw = faces.getResponseWriter();
            FastWriter fw = new FastWriter(256);
            ResponseWriter crw;
            
            try {
                for (String id : this.encode) {
                    fw.reset();
                    crw = rw.cloneWithWriter(fw);
                    faces.setResponseWriter(crw);
                    success = root.invokeOnComponent(faces, id, Encode);
                    if (!success) {
                        log.warning(id + " not found");
                    }
                    async.getEncoded().put(id, fw.toString());
                }
                
                // write state
                StateCapture sc = new StateCapture(rw.cloneWithWriter(fw));
                faces.setResponseWriter(sc);
                StateManager sm = faces.getApplication().getStateManager();
                Object stateObj = sm.saveSerializedView(faces);
                sm.writeState(faces,
                        (StateManager.SerializedView) stateObj);
                async.setViewState(sc.getState());
                
            } catch (FacesException e) {
                if (e.getCause() instanceof IOException) {
                    throw (IOException) e.getCause();
                } else {
                    throw e;
                }
            } finally {
                faces.setResponseWriter(rw);
            }
            return true;
        }
        return !this.update.isEmpty();
    }

    public void decode(FacesContext faces) {
        ExternalContext ctx = faces.getExternalContext();

        // get these parameters
        Map<String, String> params = ctx.getRequestHeaderMap();
        String param = params.get(PARAM_UPDATE);
        String[] update = (param != null) ? param.split(",") : null;
        param = params.get(PARAM_ENCODE);
        String[] encode = (param != null) ? param.split(",") : null;

        if (update != null && update.length > 0) {
            for (String id : update) {
                this.update.add(id);
            }
            if (log.isLoggable(Level.FINE)) {
                log.fine("Updating Only: " + this.update);
            }
        }

        if (encode != null && encode.length > 0) {
            for (String id : encode) {
                this.encode.add(id);
            }
            if (log.isLoggable(Level.FINE)) {
                log.fine("Encoding Only: " + this.encode);
            }
        }

        super.decode(faces);
    }

    public Set<String> getEncodeIdSet() {
        return this.encode;
    }

    public Set<String> getUpdateIdSet() {
        return this.update;
    }
    
    private static class StateCapture extends ResponseWriterWrapper {
        
        protected final ResponseWriter orig;
        private Object state;
        
        public StateCapture(ResponseWriter orig) {
            this.orig = orig;
        }

        protected ResponseWriter getWrapped() {
            return this.orig;
        }

        public void writeAttribute(String name, Object value, String property) throws IOException {
            if ("value".equals(name)) {
                this.state = value;
            }
        }
        
        public String getState() {
            return this.state != null ? this.state.toString() : "";
        }
        
    }
}
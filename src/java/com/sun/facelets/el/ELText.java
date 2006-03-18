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

package com.sun.facelets.el;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;

import com.sun.facelets.util.FastWriter;

/**
 * Handles parsing EL Strings in accordance with the EL-API Specification. The
 * parser accepts either <code>${..}</code> or <code>#{..}</code>.
 * 
 * @author Jacob Hookom
 * @version $Id: ELText.java,v 1.3.8.1 2006/03/18 23:29:16 adamwiner Exp $
 */
public class ELText {

    private static final class LiteralValueExpression extends ValueExpression {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        private final String text;

        public LiteralValueExpression(String text) {
            this.text = text;
        }

        public boolean isLiteralText() {
            return false;
        }

        public int hashCode() {
            return 0;
        }

        public String getExpressionString() {
            return this.text;
        }

        public boolean equals(Object obj) {
            return false;
        }

        public void setValue(ELContext context, Object value) {
        }

        public boolean isReadOnly(ELContext context) {
            return false;
        }

        public Object getValue(ELContext context) {
            return null;
        }

        public Class getType(ELContext context) {
            return null;
        }

        public Class getExpectedType() {
            return null;
        }

    }

    private static final class ELTextComposite extends ELText {
        private final ELText[] txt;

        public ELTextComposite(ELText[] txt) {
            super(null);
            this.txt = txt;
        }

        public void write(Writer out, ELContext ctx) throws ELException,
                IOException {
            for (int i = 0; i < this.txt.length; i++) {
                this.txt[i].write(out, ctx);
            }
        }

        public String toString(ELContext ctx) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < this.txt.length; i++) {
                sb.append(this.txt[i].toString(ctx));
            }
            return sb.toString();
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < this.txt.length; i++) {
                sb.append(this.txt[i].toString());
            }
            return sb.toString();
        }

        public boolean isLiteral() {
            return false;
        }

        public ELText apply(ExpressionFactory factory, ELContext ctx) {
            int len = this.txt.length;
            ELText[] nt = new ELText[len];
            for (int i = 0; i < len; i++) {
                nt[i] = this.txt[i].apply(factory, ctx);
            }
            return new ELTextComposite(nt);
        }
    }

    private static final class ELTextVariable extends ELText {
        private final ValueExpression ve;

        public ELTextVariable(ValueExpression ve) {
            super(ve.getExpressionString());
            this.ve = ve;
        }

        public boolean isLiteral() {
            return false;
        }

        public ELText apply(ExpressionFactory factory, ELContext ctx) {
            return new ELTextVariable(factory.createValueExpression(ctx,
                    this.ve.getExpressionString(), String.class));
        }

        public void write(Writer out, ELContext ctx) throws ELException,
                IOException {
            Object v = this.ve.getValue(ctx);
            if (v != null) {
                out.write((String) v);
            }
        }

        public String toString(ELContext ctx) throws ELException {
            Object v = this.ve.getValue(ctx);
            if (v != null) {
              return v.toString();
            }
            
            return null;
        }
    }

    protected final String literal;

    public ELText(String literal) {
        this.literal = literal;
    }

    /**
     * If it's literal text
     * 
     * @return true if the String is literal (doesn't contain <code>#{..}</code>
     *         or <code>${..}</code>)
     */
    public boolean isLiteral() {
        return true;
    }

    /**
     * Return an instance of <code>this</code> that is applicable given the
     * ELContext and ExpressionFactory state.
     * 
     * @param factory
     *            the ExpressionFactory to use
     * @param ctx
     *            the ELContext to use
     * @return an ELText instance
     */
    public ELText apply(ExpressionFactory factory, ELContext ctx) {
        return this;
    }

    /**
     * Allow this instance to write to the passed Writer, given the ELContext
     * state
     * 
     * @param out
     *            Writer to write to
     * @param ctx
     *            current ELContext state
     * @throws ELException
     * @throws IOException
     */
    public void write(Writer out, ELContext ctx) throws ELException,
            IOException {
        out.write(this.literal);
    }

    /**
     * Evaluates the ELText to a String
     * 
     * @param ctx
     *            current ELContext state
     * @throws ELException
     * @return the evaluated String
     */
    public String toString(ELContext ctx) throws ELException {
        // Optimized in subclasses
        Writer out = new FastWriter();
        try {
            write(out, ctx);
        } catch (IOException ioe) { }

        return out.toString();
    }

    public String toString() {
        return this.literal;
    }

    /**
     * Parses the passed string to determine if it's literal or not
     * 
     * @param in
     *            input String
     * @return true if the String is literal (doesn't contain <code>#{..}</code>
     *         or <code>${..}</code>)
     */
    public static boolean isLiteral(String in) {
        ELText txt = parse(in);
        return txt == null || txt.isLiteral();
    }

    /**
     * Factory method for creating an unvalidated ELText instance. NOTE: All
     * expressions in the passed String are treated as
     * {@link com.sun.facelets.el.LiteralValueExpression LiteralValueExpressions}.
     * 
     * @param in
     *            String to parse
     * @return ELText instance that knows if the String was literal or not
     * @throws javax.el.ELException
     */
    public static ELText parse(String in) throws ELException {
        return parse(null, null, in);
    }

    /**
     * Factory method for creating a validated ELText instance. When an
     * Expression is hit, it will use the ExpressionFactory to create a
     * ValueExpression instance, resolving any functions at that time. <p/>
     * Variables and properties will not be evaluated.
     * 
     * @param fact
     *            ExpressionFactory to use
     * @param ctx
     *            ELContext to validate against
     * @param in
     *            String to parse
     * @return ELText that can be re-applied later
     * @throws javax.el.ELException
     */
    public static ELText parse(ExpressionFactory fact, ELContext ctx, String in)
            throws ELException {
        char[] ca = in.toCharArray();
        int i = 0;
        char c = 0;
        int len = ca.length;
        int end = len - 1;
        boolean esc = false;
        int vlen = 0;

        StringBuffer buff = new StringBuffer(128);
        List text = new ArrayList();
        ELText t = null;
        ValueExpression ve = null;

        while (i < len) {
            c = ca[i];
            if ('\\' == c) {
                esc = !esc;
            } else if (!esc && ('$' == c || '#' == c)) {
                if (i < end) {
                    if ('{' == ca[i + 1]) {
                        if (buff.length() > 0) {
                            text.add(new ELText(buff.toString()));
                            buff.setLength(0);
                        }
                        vlen = findVarLength(ca, i);
                        if (ctx != null && fact != null) {
                            ve = fact.createValueExpression(ctx, new String(ca,
                                    i, vlen), String.class);
                            t = new ELTextVariable(ve);
                        } else {
                            t = new ELTextVariable(new LiteralValueExpression(
                                    new String(ca, i, vlen)));
                        }
                        text.add(t);
                        i += vlen;
                        continue;
                    }
                }
            }
            buff.append(c);
            i++;
        }

        if (buff.length() > 0) {
            text.add(new ELText(new String(buff.toString())));
            buff.setLength(0);
        }

        if (text.size() == 0) {
            return null;
        } else if (text.size() == 1) {
            return (ELText) text.get(0);
        } else {
            ELText[] ta = (ELText[]) text.toArray(new ELText[text.size()]);
            return new ELTextComposite(ta);
        }
    }

    private static int findVarLength(char[] ca, int s) throws ELException {
        int i = s;
        int len = ca.length;
        char c = 0;
        int str = 0;
        boolean esc = false;
        while (i < len) {
            c = ca[i];
            if ('\\' == c) {
                esc = true;
            } else if (!esc && ('\'' == c || '"' == c)) {
                if (str == c) {
                    str = 0;
                } else {
                    str = c;
                }
            } else if (!esc && str == 0 && ('}' == c)) {
                return i - s + 1;
            }
            i++;
        }
        throw new ELException("EL Expression Unbalanced: ... "
                + new String(ca, s, i - s));
    }

}

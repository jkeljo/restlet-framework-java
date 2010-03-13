/**
 * Copyright 2005-2010 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.engine.http.header;

import java.io.IOException;
import java.io.StringWriter;

import org.restlet.data.Parameter;

/**
 * HTTP-style header builder. It builds an internal buffer that can be retrieved
 * at the end via the {@link #toString()} method.
 * 
 * @author Jerome Louvel
 */
public class HeaderWriter extends StringWriter {

    /** Indicates if the first parameter is written. */
    private volatile boolean firstParameter;

    /**
     * Constructor.
     */
    public HeaderWriter() {
        this.firstParameter = true;
    }

    @Override
    public HeaderWriter append(char c) {
        super.append(c);
        return this;
    }

    @Override
    public HeaderWriter append(CharSequence csq) {
        super.append(csq);
        return this;
    }

    /**
     * Appends a string as an HTTP comment, surrounded by parenthesis and with
     * properly quote content if needed.
     * 
     * @param content
     *            The comment to write.
     * @return The current builder.
     * @throws IOException
     */
    public HeaderWriter appendComment(String content) throws IOException {
        append('(');

        char c;
        for (int i = 0; i < content.length(); i++) {
            c = content.charAt(i);

            if (HeaderUtils.isCommentText(c)) {
                append(c);
            } else {
                appendQuotedPair(c);
            }
        }

        append(')');
        return this;
    }

    /**
     * Appends a new parameter, prefixed with a comma. The value is separated
     * from the name by an '=' character.
     * 
     * @param parameter
     *            The parameter.
     * @return The current builder.
     * @throws IOException
     */
    public HeaderWriter appendParameter(Parameter parameter) throws IOException {
        return appendParameter(parameter.getName(), parameter.getValue());
    }

    /**
     * Appends a new parameter, prefixed with a comma.
     * 
     * @param name
     *            The parameter name.
     * @return The current builder.
     * @throws IOException
     */
    public HeaderWriter appendParameter(String name) throws IOException {
        appendParameterSeparator();
        return appendToken(name);
    }

    /**
     * Appends a new parameter, prefixed with a comma. The value is separated
     * from the name by an '=' character.
     * 
     * @param name
     *            The parameter name.
     * @param value
     *            The parameter value.
     * @return The current builder.
     * @throws IOException
     */
    public HeaderWriter appendParameter(String name, String value)
            throws IOException {
        appendParameterSeparator();

        if (name != null) {
            appendToken(name);
        }

        if (value != null) {
            append('=');
            appendToken(value);
        }

        return this;
    }

    /**
     * Appends a comma as a separator if the first parameter has already been
     * written.
     * 
     * @return The current builder.
     * @throws IOException
     */
    public HeaderWriter appendParameterSeparator() throws IOException {
        if (isFirstParameter()) {
            setFirstParameter(false);
        } else {
            append(", ");
        }

        return this;
    }

    /**
     * Appends a quoted character, prefixing it with a backslash.
     * 
     * @param character
     *            The character to quote.
     * @return The current builder.
     */
    protected HeaderWriter appendQuotedPair(char character) {
        append('\\').append(character);
        return this;
    }

    /**
     * Appends a new parameter, prefixed with a comma. The value is separated
     * from the name by an '=' character.
     * 
     * @param parameter
     *            The parameter.
     * @throws IOException
     * @return The current builder.
     */
    public HeaderWriter appendQuotedParameter(Parameter parameter)
            throws IOException {
        return appendQuotedParameter(parameter.getName(), parameter.getValue());
    }

    /**
     * Appends a new parameter, prefixed with a comma. The value is quoted and
     * separated from the name by an '=' character.
     * 
     * @param name
     *            The parameter name.
     * @param value
     *            The parameter value to quote.
     * @throws IOException
     * @return The current builder.
     */
    public HeaderWriter appendQuotedParameter(String name, String value)
            throws IOException {
        appendParameterSeparator();

        if (name != null) {
            appendToken(name);
        }

        if (value != null) {
            append('=');
            appendQuotedString(value);
        }

        return this;
    }

    /**
     * Appends a quoted string.
     * 
     * @param content
     *            The string to quote and write.
     * @return The current builder.
     */
    public HeaderWriter appendQuotedString(String content) {
        append('"');

        char c;
        for (int i = 0; i < content.length(); i++) {
            c = content.charAt(i);

            if (HeaderUtils.isQuotedText(c)) {
                append(c);
            } else {
                appendQuotedPair(c);
            }
        }

        append('"');
        return this;
    }

    /**
     * Appends a space character.
     * 
     * @return The current builder.
     */
    public HeaderWriter appendSpace() {
        append(' ');
        return this;
    }

    /**
     * Appends a token.
     * 
     * @param token
     *            The token to write.
     * @return The current builder.
     * @throws IOException
     */
    public HeaderWriter appendToken(String token) throws IOException {
        if (HeaderUtils.isToken(token)) {
            append(token);
        } else {
            throw new IOException("Unexpected character found in token: "
                    + token);
        }

        return this;
    }

    /**
     * Indicates if the first parameter is written.
     * 
     * @return True if the first parameter is written.
     */
    public boolean isFirstParameter() {
        return firstParameter;
    }

    /**
     * Indicates if the first parameter is written.
     * 
     * @param firstParameter
     *            True if the first parameter is written.
     */
    public void setFirstParameter(boolean firstParameter) {
        this.firstParameter = firstParameter;
    }

}

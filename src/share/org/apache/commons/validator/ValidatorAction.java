/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//validator/src/share/org/apache/commons/validator/ValidatorAction.java,v 1.16 2003/11/17 03:34:50 rleland Exp $
 * $Revision: 1.16 $
 * $Date: 2003/11/17 03:34:50 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names, "Apache", "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.commons.validator;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>Contains the information to dynamically create and run a validation
 * method.  This is the class representation of a pluggable validator that can be
 * defined in an xml file with the &lt;validator&gt; element.</p>
 *
 * <strong>Note</strong>: The validation method is assumed to be thread safe.
 *
 * @author David Winterfeldt
 * @author David Graham
 * @version $Revision: 1.16 $ $Date: 2003/11/17 03:34:50 $
 */
public class ValidatorAction implements Serializable {

    /**
     * The name of the validation.
     */
    private String name = null;

    /**
     * The full class name of the class containing
     * the validation method associated with this action.
     */
    private String classname = null;

    /**
     * The full method name of the validation to be performed.  The method
     * must be thread safe.
     */
    private String method = null;

    // Default for Struts
    /**
     * <p>The method signature of the validation method.  This should be a comma
     * delimited list of the full class names of each parameter in the correct order that
     * the method takes.</p>
     *
     * <p>Note: <code>java.lang.Object</code> is reserved for the
     * JavaBean that is being validated.  The <code>ValidatorAction</code>
     * and <code>Field</code> that are associated with a fields
     * validation will automatically be populated if they are
     * specified in the method signature.
     * </p>
     */
    private String methodParams =
            Validator.BEAN_PARAM
            + ","
            + Validator.VALIDATOR_ACTION_PARAM
            + ","
            + Validator.FIELD_PARAM;

    /**
     * The other <code>ValidatorAction</code>s that this one depends on.  If any
     * errors occur in an action that this one depends on, this action will not be
     * processsed.
     */
    private String depends = null;

    /**
     * The default error message associated with this action.
     */
    private String msg = null;

    /**
     * An optional field to contain the name to be used if JavaScript is generated.
     */
    private String jsFunctionName = null;

    /**
     * An optional field to contain the class path to be used to retrieve the
     * JavaScript function.
     */
    private String jsFunction = null;

    /**
     * An optional field to containing a JavaScript representation of the
     * java method assocated with this action.
     */
    private String javascript = null;

    /**
     * If the java method matching the correct signature isn't static, the instance is
     * stored in the action.  This assumes the method is thread safe.
     */
    private Object instance = null;

    /**
     * Logger.
     */
    private static final Log log = LogFactory.getLog(ValidatorAction.class);

    /**
     * An internal List representation of the other <code>ValidatorAction</code>s
     * this one depends on (if any).  This List gets updated
     * whenever setDepends() gets called.  This is synchronized so a call to
     * setDepends() (which clears the List) won't interfere with a call to
     * isDependency().
     */
    private List dependencyList = Collections.synchronizedList(new ArrayList());

    /**
     * An internal List representation of all the validation method's parameters defined
     * in the methodParams String.
     */
    private List methodParameterList = new ArrayList();

    /**
     * Gets the name of the validator action.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the validator action.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the class of the validator action.
     */
    public String getClassname() {
        return classname;
    }

    /**
     * Sets the class of the validator action.
     */
    public void setClassname(String classname) {
        this.classname = classname;
    }

    /**
     * Gets the name of method being called for the validator action.
     */
    public String getMethod() {
        return method;
    }

    /**
     * Sets the name of method being called for the validator action.
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * Gets the method parameters for the method.
     */
    public String getMethodParams() {
        return methodParams;
    }

    /**
     * Sets the method parameters for the method.
     * @param methodParams A comma separated list of parameters.
     */
    public void setMethodParams(String methodParams) {
        this.methodParams = methodParams;

        this.methodParameterList.clear();

        StringTokenizer st = new StringTokenizer(methodParams, ",");
        while (st.hasMoreTokens()) {
            String value = st.nextToken().trim();

            if (value != null && value.length() > 0) {
                this.methodParameterList.add(value);
            }
        }
    }

    /**
     * Gets the method parameters for the method as an unmodifiable List.
     */
    public List getMethodParamsList() {
        return Collections.unmodifiableList(this.methodParameterList);
    }

    /**
     * Gets the dependencies of the validator action as a comma separated list of
     * validator names.
     */
    public String getDepends() {
        return this.depends;
    }

    /**
     * Sets the dependencies of the validator action.
     * @param depends A comma separated list of validator names.
     */
    public void setDepends(String depends) {
        this.depends = depends;

        this.dependencyList.clear();

        StringTokenizer st = new StringTokenizer(depends, ",");
        while (st.hasMoreTokens()) {
            String depend = st.nextToken().trim();

            if (depend != null && depend.length() > 0) {
                this.dependencyList.add(depend);
            }
        }
    }

    /**
     * Gets the message associated with the validator action.
     */
    public String getMsg() {
        return msg;
    }

    /**
     * Sets the message associated with the validator action.
     */
    public void setMsg(String msg) {
        this.msg = msg;
    }

    /**
     * Gets the Javascript function name.  This is optional and can
     * be used instead of validator action name for the name of the
     * Javascript function/object.
     */
    public String getJsFunctionName() {
        return jsFunctionName;
    }

    /**
     * Sets the Javascript function name.  This is optional and can
     * be used instead of validator action name for the name of the
     * Javascript function/object.
     */
    public void setJsFunctionName(String jsFunctionName) {
        this.jsFunctionName = jsFunctionName;
    }

    /**
     * Sets the fully qualified class path of the Javascript function.
     * <p>
     * This is optional and can be used <strong>instead</strong> of the setJavascript().
     * Attempting to call both <code>setJsFunction</code> and <code>setJavascript</code>
     * will result in an <code>IllegalStateException</code> being thrown. </p><p>
     * If <strong>neither</strong> setJsFunction or setJavascript is set then validator will attempt
     * to load the default javascript definition.   </p>
     * <pre>
     * <b>Examples</b>
     *   If in the validator.xml :
     * #1:
     *      &lt;validator name="tire"
     *            jsFunction="com.yourcompany.project.tireFuncion"&gt;
     *     Validator will attempt to load com.yourcompany.project.validateTireFunction.js from
     *     its class path.
     * #2:
     *    &lt;validator name="tire"&gt;
     *      Validator will use the name attribute to try and load
     *         org.apache.commons.validator.javascript.validateTire.js
     *      which is the default javascript definition.
     * </pre>
     */
    public void setJsFunction(String jsFunction) {
        if (javascript != null) {
            throw new IllegalStateException("Cannot call setJsFunction() after calling setJavascript()");
        }

        this.jsFunction = jsFunction;
    }

    /**
     * Gets the Javascript equivalent of the java class and method
     * associated with this action.
     */
    public String getJavascript() {
        return javascript;
    }

    /**
     * Sets the Javascript equivalent of the java class and method
     * associated with this action.
     */
    public void setJavascript(String javascript) {
        if (jsFunction != null) {
            throw new IllegalStateException("Cannot call setJavascript() after calling setJsFunction()");
        }

        this.javascript = javascript;
    }

    /**
     * Gets an instance based on the validator action's classname.
     */
    public Object getClassnameInstance() {
        return instance;
    }

    /**
     * Sets an instance based on the validator action's classname.
     */
    public void setClassnameInstance(Object instance) {
        this.instance = instance;
    }

    /**
     * Initialize based on set.
     */
    protected void init() {
        this.loadJavascriptFunction();
    }

    /**
     * Load the javascript function specified by the given path.  For this
     * implementation, the <code>jsFunction</code> property should contain a fully
     * qualified package and script name, separated by periods, to be loaded from
     * the class loader that created this instance.
     *
     * TODO if the path begins with a '/' the path will be intepreted as absolute, and remain unchanged.
     * If this fails then it will attempt to treat the path as a file path.
     * It is assumed the script ends with a '.js'.
     */
    protected synchronized void loadJavascriptFunction() {

        if (this.javascriptAlreadyLoaded()) {
            return;
        }

        if (log.isTraceEnabled()) {
            log.trace("  Loading function begun");
        }

        if (this.jsFunction == null) {
            this.jsFunction = this.generateJsFunction();
        }

        String javascriptFileName = this.formatJavascriptFileName();

        if (log.isTraceEnabled()) {
            log.trace("  Loading js function '" + javascriptFileName + "'");
        }

        this.javascript = this.readJavascriptFile(javascriptFileName);

        if (log.isTraceEnabled()) {
            log.trace("  Loading javascript function completed");
        }

    }

    /**
     * Read a javascript function from a file.
     * @param javascriptFileName The file containing the javascript.
     * @return The javascript function or null if it could not be loaded.
     */
    private String readJavascriptFile(String javascriptFileName) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = this.getClass().getClassLoader();
        }

        InputStream is = classLoader.getResourceAsStream(javascriptFileName);
        if (is == null) {
            is = this.getClass().getResourceAsStream(javascriptFileName);
        }

        if (is == null) {
            log.debug("  Unable to read javascript name "+javascriptFileName);
            return null;
        }

        StringBuffer function = new StringBuffer();
        try {
            int bufferSize = is.available();
            int bytesRead;
            while (bufferSize > 0) {
                byte[] buffer = new byte[bufferSize];
                bytesRead = is.read(buffer, 0, bufferSize);
                if (bytesRead > 0) {
                    String functionPart = new String(buffer,0,bytesRead);
                    function.append(functionPart);
                }
                bufferSize = is.available();
            }

        } catch(IOException e) {
            log.error("readJavascriptFile()", e);

        } finally {
            try {
                is.close();
            } catch(IOException e) {
                log.error("readJavascriptFile()", e);
            }
        }
        String strFunction = function.toString();
        return strFunction.equals("") ? null : strFunction;
    }

    /**
     * @return A filename suitable for passing to a ClassLoader.getResourceAsStream()
     * method.
     */
    private String formatJavascriptFileName() {
        String name = this.jsFunction.substring(1);

        if (!this.jsFunction.startsWith("/")) {
            name = jsFunction.replace('.', '/') + ".js";
        }

        return name;
    }

    /**
     * @return true if the javascript for this action has already been loaded.
     */
    private boolean javascriptAlreadyLoaded() {
        return (this.javascript != null);
    }

    /**
     * Used to generate the javascript name when it is not specified.
     */
    private String generateJsFunction() {
        StringBuffer jsName =
                new StringBuffer("org.apache.commons.validator.javascript");

        jsName.append(".validate");
        jsName.append(name.substring(0, 1).toUpperCase());
        jsName.append(name.substring(1, name.length()));

        return jsName.toString();
    }

    /**
     * Creates a <code>FastHashMap</code> for the isDependency method
     * based on depends.
     * @deprecated This functionality has been moved to other methods.  It's no longer
     * required to call this method to initialize this object.
     */
    public synchronized void process(Map globalConstants) {
// do nothing
    }

    /**
     * Checks whether or not the value passed in is in the depends field.
     */
    public boolean isDependency(String validatorName) {
        return this.dependencyList.contains(validatorName);
    }

    /**
     * Gets the dependencies as a <code>Collection</code>.
     * @deprecated Use getDependencyList() instead.
     */
    public Collection getDependencies() {
        return this.getDependencyList();
    }

    /**
     * Returns the dependent validator names as an unmodifiable
     * <code>List</code>.
     */
    public List getDependencyList() {
        return Collections.unmodifiableList(this.dependencyList);
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        StringBuffer results = new StringBuffer("ValidatorAction: ");
        results.append(name);
        results.append("\n");

        return results.toString();
    }

}
/*-
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 						reserved.
 * ================================================================================
 * Modifications Copyright (C) 2018 IBM.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.core.slipluginutils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A SvcLogicJavaPlugin that exposes java.lang.String functions to DirectedGraph
 */
public class SliStringUtils implements SvcLogicJavaPlugin {
	private static final Logger LOG = LoggerFactory.getLogger(SliStringUtils.class);
	public static final String INPUT_PARAM_KEY = "key";
	public static final String INPUT_PARAM_SOURCE = "source";
	public static final String INPUT_PARAM_TARGET = "target";
    public static final String TRUE_CONSTANT = "true";
    public static final String FALSE_CONSTANT = "false";

	public SliStringUtils() {}

	/**
	 * Provides split functionality to Directed Graphs.
	 * @param parameters HashMap<String,String> of parameters passed by the DG to this function
	 * <table border="1">
	 * 	<thead><th>parameter</th><th>Mandatory/Optional</th><th>description</th></thead>
	 * 	<tbody>
	 * 		<tr><td>original_string</td><td>Mandatory</td><td>String to perform split on</td></tr>
	 * 		<tr><td>regex</td><td>Mandatory</td><td>the delimiting regular expression</td></tr>
	 * 		<tr><td>limit</td><td>Optional</td><td>result threshold. See String.split method for further description. Defaults to 0</td></tr>
	 * 		<tr><td>ctx_memory_result_key</td><td>Mandatory</td><td>Key in context memory to populate the resulting array of strings under</td></tr>
	 * 	</tbody>
	 * </table>
	 * @param ctx Reference to context memory
	 * @throws SvcLogicException
	 * @since 11.0.2
	 * @see String#split(String, int)
	 */
	public void split( Map<String, String> parameters, SvcLogicContext ctx ) throws SvcLogicException {
		final String original_string = parameters.get("original_string");
		LOG.trace("original_string = " + original_string);
		final String regex = parameters.get("regex");
		LOG.trace("regex = " + regex);
		final String limit_str = parameters.get("limit");
		LOG.trace("limit_str = " + limit_str);
		final String ctx_memory_result_key = parameters.get("ctx_memory_result_key");
		LOG.trace("ctx_memory_result_key = " + ctx_memory_result_key);

		try {
			// Validation that parameters are not null
			SliPluginUtils.checkParameters( parameters, new String[]{"original_string","regex","ctx_memory_result_key"}, LOG );

			// Read limit from context memory. Default to 0 if null/empty
			int limit = 0;
			if( StringUtils.isNotEmpty(limit_str) ) {
				try {
					limit = Integer.parseInt(limit_str);
				}
				catch( NumberFormatException e ) {
					throw new IllegalArgumentException( "The limit parameter of the SliStringUtils.split() function must be a number, empty string, or null", e );
				}
			}

			// Call String.split(regex,limit) on string passed in
			String[] split_string = original_string.split(regex, limit);

			// Populate context memory with results
			for( int i = 0; i < split_string.length; i++ ) {
				SliPluginUtils.ctxSetAttribute(ctx, ctx_memory_result_key + '[' + i + ']', split_string[i], LOG, SliPluginUtils.LogLevel.DEBUG);
			}
			SliPluginUtils.ctxSetAttribute(ctx, ctx_memory_result_key + "_length", new Integer(split_string.length), LOG, SliPluginUtils.LogLevel.DEBUG);
		}
		catch( Exception e ) {
			// Have error message print parameters
			throw new SvcLogicException( "An error occurred during SliStringUtils.split() where original_string = " + quotedOrNULL(regex) +
					" regex = " + quotedOrNULL(regex) +
					" limit = " + quotedOrNULL(regex) +
					" ctx_memory_result_key = " + quotedOrNULL(regex), e );
		}
	}

	public static String quotedOrNULL( String str ) {
		return (str == null) ? "NULL" : '"' + str + '"';
	}

    /**
     * exposes equalsIgnoreCase to directed graph
     * @param parameters HashMap<String,String> of parameters passed by the DG to this function
     * emits a true or false outcome
     * <table border="1">
     *  <thead><th>parameter</th><th>Mandatory/Optional</th><th>description</th></thead>
     *  <tbody>
     *      <tr><td>source</td><td>Mandatory</td><td>source string</td></tr>
     *      <tr><td>target</td><td>Mandatory</td><td>target string</td></tr>
     *  </tbody>
     * </table>
     * @param ctx Reference to context memory
     * @throws SvcLogicException
     * @since 11.0.2
     */
    public static String equalsIgnoreCase(Map<String, String> parameters, SvcLogicContext ctx) throws SvcLogicException {
        SliPluginUtils.checkParameters(parameters, new String[] {INPUT_PARAM_SOURCE, INPUT_PARAM_TARGET}, LOG);
        if (parameters.get(INPUT_PARAM_SOURCE).equalsIgnoreCase(parameters.get(INPUT_PARAM_TARGET))) {
            return TRUE_CONSTANT;
        }
        return FALSE_CONSTANT;
    }

    /**
     * exposes toUpperCase to directed graph
     * writes an upperCase version of source to outputPath
     * @param parameters HashMap<String,String> of parameters passed by the DG to this function
     * <table border="1">
     *  <thead><th>parameter</th><th>Mandatory/Optional</th><th>description</th></thead>
     *  <tbody>
     *      <tr><td>source</td><td>Mandatory</td><td>source string</td></tr>
     *      <tr><td>outputPath</td><td>Mandatory</td><td>the location in context memory the result is written to</td></tr>
     *  </tbody>
     * </table>
     * @param ctx Reference to context memory
     * @throws SvcLogicException
     * @since 11.0.2
     */
    public static void toUpper(Map<String, String> parameters, SvcLogicContext ctx) throws SvcLogicException {
        SliPluginUtils.checkParameters(parameters, new String[]{INPUT_PARAM_SOURCE,"outputPath"}, LOG);
        ctx.setAttribute(parameters.get("outputPath"), parameters.get(INPUT_PARAM_SOURCE).toUpperCase());
    }

    /**
     * exposes toLowerCase to directed graph
     * writes a lowerCase version of source to outputPath
     * @param parameters HashMap<String,String> of parameters passed by the DG to this function
     * <table border="1">
     *  <thead><th>parameter</th><th>Mandatory/Optional</th><th>description</th></thead>
     *  <tbody>
     *      <tr><td>source</td><td>Mandatory</td><td>source string</td></tr>
     *      <tr><td>outputPath</td><td>Mandatory</td><td>the location in context memory the result is written to</td></tr>
     *  </tbody>
     * </table>
     * @param ctx Reference to context memory
     * @throws SvcLogicException
     * @since 11.0.2
     */
    public static void toLower(Map<String, String> parameters, SvcLogicContext ctx) throws SvcLogicException {
        SliPluginUtils.checkParameters(parameters, new String[]{INPUT_PARAM_SOURCE,"outputPath"}, LOG);
        ctx.setAttribute(parameters.get("outputPath"), parameters.get(INPUT_PARAM_SOURCE).toLowerCase());
    }

    /**
     * exposes contains to directed graph to test if one string contains another
     * tests if the source contains the target
     * @param parameters HashMap<String,String> of parameters passed by the DG to this function
     * emits a true or false outcome
     * <table border="1">
     *  <thead><th>parameter</th><th>Mandatory/Optional</th><th>description</th></thead>
     *  <tbody>
     *      <tr><td>source</td><td>Mandatory</td><td>source string</td></tr>
     *      <tr><td>target</td><td>Mandatory</td><td>target string</td></tr>
     *  </tbody>
     * </table>
     * @param ctx Reference to context memory
     * @throws SvcLogicException
     * @since 11.0.2
     */
    public static String contains(Map<String, String> parameters, SvcLogicContext ctx) throws SvcLogicException {
        SliPluginUtils.checkParameters(parameters, new String[] {INPUT_PARAM_SOURCE, INPUT_PARAM_TARGET}, LOG);
        if (parameters.get(INPUT_PARAM_SOURCE).contains(parameters.get(INPUT_PARAM_TARGET))) {
            return TRUE_CONSTANT;
        }
        return FALSE_CONSTANT;
    }

    /**
     * exposes endsWith to directed graph to test if one string endsWith another string
     * tests if the source ends with the target
     * @param parameters HashMap<String,String> of parameters passed by the DG to this function
     * emits a true or false outcome
     * <table border="1">
     *  <thead><th>parameter</th><th>Mandatory/Optional</th><th>description</th></thead>
     *  <tbody>
     *      <tr><td>source</td><td>Mandatory</td><td>source string</td></tr>
     *      <tr><td>target</td><td>Mandatory</td><td>target string</td></tr>
     *  </tbody>
     * </table>
     * @param ctx Reference to context memory
     * @throws SvcLogicException
     * @since 11.0.2
     */
    public static String endsWith(Map<String, String> parameters, SvcLogicContext ctx) throws SvcLogicException {
        SliPluginUtils.checkParameters(parameters, new String[] {INPUT_PARAM_SOURCE, INPUT_PARAM_TARGET}, LOG);
        if (parameters.get(INPUT_PARAM_SOURCE).endsWith(parameters.get(INPUT_PARAM_TARGET))) {
            return TRUE_CONSTANT;
        }
        return FALSE_CONSTANT;
    }

    /**
     * exposes startsWith to directed graph to test if one string endsWith another string
     * tests if the source ends with the target
     * @param parameters HashMap<String,String> of parameters passed by the DG to this function
     * emits a true or false outcome
     * <table border="1">
     *  <thead><th>parameter</th><th>Mandatory/Optional</th><th>description</th></thead>
     *  <tbody>
     *      <tr><td>source</td><td>Mandatory</td><td>source string</td></tr>
     *      <tr><td>target</td><td>Mandatory</td><td>target string</td></tr>
     *  </tbody>
     * </table>
     * @param ctx Reference to context memory
     * @throws SvcLogicException
     * @since 11.0.2
     */
    public static String startsWith(Map<String, String> parameters, SvcLogicContext ctx) throws SvcLogicException {
        SliPluginUtils.checkParameters(parameters, new String[] {INPUT_PARAM_SOURCE, INPUT_PARAM_TARGET}, LOG);
        if (parameters.get(INPUT_PARAM_SOURCE).startsWith(parameters.get(INPUT_PARAM_TARGET))) {
            return TRUE_CONSTANT;
        }
        return FALSE_CONSTANT;
    }

    /**
     * exposes trim to directed graph
     * writes a trimmed version of the string to the outputPath
     * @param parameters HashMap<String,String> of parameters passed by the DG to this function
     * <table border="1">
     *  <thead><th>parameter</th><th>Mandatory/Optional</th><th>description</th></thead>
     *  <tbody>
     *      <tr><td>source</td><td>Mandatory</td><td>source string</td></tr>
     *      <tr><td>outputPath</td><td>Mandatory</td><td>the location in context memory the result is written to</td></tr>
     *  </tbody>
     * </table>
     * @param ctx Reference to context memory
     * @throws SvcLogicException
     * @since 11.0.2
     */
    public static void trim(Map<String, String> parameters, SvcLogicContext ctx) throws SvcLogicException {
        SliPluginUtils.checkParameters(parameters, new String[]{INPUT_PARAM_SOURCE,"outputPath"}, LOG);
        ctx.setAttribute(parameters.get("outputPath"), parameters.get(INPUT_PARAM_SOURCE).trim());
    }

    /**
     * exposes String.length() to directed graph
     * writes the length of source to outputPath
     * @param parameters HashMap<String,String> of parameters passed by the DG to this function
     * <table border="1">
     *  <thead><th>parameter</th><th>Mandatory/Optional</th><th>description</th></thead>
     *  <tbody>
     *      <tr><td>source</td><td>Mandatory</td><td>source string</td></tr>
     *      <tr><td>outputPath</td><td>Mandatory</td><td>the location in context memory the result is written to</td></tr>
     *  </tbody>
     * </table>
     * @param ctx Reference to context memory
     * @throws SvcLogicException
     * @since 11.0.2
     */
    public static void getLength(Map<String, String> parameters, SvcLogicContext ctx) throws SvcLogicException {
        SliPluginUtils.checkParameters(parameters, new String[]{INPUT_PARAM_SOURCE,"outputPath"}, LOG);
        ctx.setAttribute(parameters.get("outputPath"), String.valueOf(parameters.get(INPUT_PARAM_SOURCE).length()));
    }

    /**
     * exposes replace to directed graph
     * writes the length of source to outputPath
     * @param parameters HashMap<String,String> of parameters passed by the DG to this function
     * <table border="1">
     *  <thead><th>parameter</th><th>Mandatory/Optional</th><th>description</th></thead>
     *  <tbody>
     *      <tr><td>source</td><td>Mandatory</td><td>source string</td></tr>
     *      <tr><td>target</td><td>Mandatory</td><td>The sequence of char values to be replaced</td></tr>
     *      <tr><td>replacement</td><td>Mandatory</td><td>The replacement sequence of char values</td></tr>
     *      <tr><td>outputPath</td><td>Mandatory</td><td>the location in context memory the result is written to</td></tr>
     *  </tbody>
     * </table>
     * @param ctx Reference to context memory
     * @throws SvcLogicException
     * @since 11.0.2
     */
    public static void replace(Map<String, String> parameters, SvcLogicContext ctx) throws SvcLogicException {
        SliPluginUtils.checkParameters(parameters,
                new String[] {INPUT_PARAM_SOURCE, "outputPath", INPUT_PARAM_TARGET, "replacement"}, LOG);
        ctx.setAttribute(parameters.get("outputPath"), (parameters.get(INPUT_PARAM_SOURCE)
                .replace(parameters.get(INPUT_PARAM_TARGET), parameters.get("replacement"))));
    }

    /**
     * exposes replaceAll to directed graph
     * writes the length of source to outputPath
     * @param parameters HashMap<String,String> of parameters passed by the DG to this function
     * <table border="1">
     *  <thead><th>parameter</th><th>Mandatory/Optional</th><th>description</th></thead>
     *  <tbody>
     *      <tr><td>source</td><td>Mandatory</td><td>source string</td></tr>
     *      <tr><td>target</td><td>Mandatory</td><td>This should be a valid regular expression</td></tr>
     *      <tr><td>replacement</td><td>Mandatory</td><td>The replacement sequence of char values</td></tr>
     *      <tr><td>outputPath</td><td>Mandatory</td><td>the location in context memory the result is written to</td></tr>
     *  </tbody>
     * </table>
     * @param ctx Reference to context memory
     * @throws SvcLogicException
     * @since 11.0.2
     */
    public static void replaceAll(Map<String, String> parameters, SvcLogicContext ctx) throws SvcLogicException {
        SliPluginUtils.checkParameters(parameters,
                new String[] {INPUT_PARAM_SOURCE, "outputPath", INPUT_PARAM_TARGET, "replacement"}, LOG);
        ctx.setAttribute(parameters.get("outputPath"), parameters.get(INPUT_PARAM_SOURCE)
                .replaceAll(parameters.get(INPUT_PARAM_TARGET), parameters.get("replacement")));
    }
    
    /**
     * Provides substring functionality to Directed Graphs.
     * <p>
     * Calls either String.substring(String beginIndex) or
     * String.substring(String beginInded, String endIndex) if the end-index
     * is present or not.
     * @param parameters HashMap<String,String> of parameters passed by the DG to this function
     * <table border="1">
     *  <thead><th>parameter</th><th>Mandatory/Optional</th><th>description</th></thead>
     *  <tbody>
     *      <tr><td>string</td><td>Mandatory</td><td>String to perform substring on</td></tr>
     *      <tr><td>result</td><td>Mandatory</td><td>Key in context memory to populate the resulting string in</td></tr>
     *      <tr><td>begin-index</td><td>Mandatory</td><td>Beginning index to pass to Java substring function</td></tr>
     *      <tr><td>end-index</td><td>Optional</td><td>Ending index to pass to Java substring function. If not included, String.substring(begin) will be called.</td></tr>
     *  </tbody>
     * </table>
     * @param ctx Reference to context memory
     * @throws SvcLogicException
     * @since 11.0.2
     */
    public void substring( Map<String, String> parameters, SvcLogicContext ctx ) throws SvcLogicException {
        try {
            SliPluginUtils.checkParameters( parameters, new String[]{"string","begin-index","result"}, LOG );
            final String string = parameters.get("string");
            final String result = parameters.get("result");
            final String begin = parameters.get("begin-index");
            final String end = parameters.get("end-index");
            if( StringUtils.isEmpty(end) ) {
                ctx.setAttribute( result, string.substring(Integer.parseInt(begin)) );
            }
            else {
                ctx.setAttribute( result, string.substring(Integer.parseInt(begin), Integer.parseInt(end)) );
            }
        }
        catch( Exception e ) {
            throw new SvcLogicException( "An error occurred while the Directed Graph was performing a substring", e );
        }
    }

    /**
     * Provides concat functionality to Directed Graphs.
     * <p>
     * Will concat target to source and write the result to outputPath
     * @param parameters HashMap<String,String> of parameters passed by the DG to this function
     * <table border="1">
     *  <thead><th>parameter</th><th>Mandatory/Optional</th><th>description</th></thead>
     *  <tbody>
     *      <tr><td>source</td><td>Mandatory</td><td>source string</td></tr>
     *      <tr><td>target</td><td>Mandatory</td><td>The sequence of char values to be replaced</td></tr>
     *      <tr><td>outputPath</td><td>Mandatory</td><td>the location in context memory the result is written to</td></tr>
     *  </tbody>
     * </table>
     * @param ctx Reference to context memory
     * @throws SvcLogicException
     * @since 11.0.2
     */
    public static void concat( Map<String, String> parameters, SvcLogicContext ctx ) throws SvcLogicException {
        SliPluginUtils.checkParameters(parameters, new String[] {INPUT_PARAM_SOURCE, INPUT_PARAM_TARGET, "outputPath"},
                LOG);
        String result = parameters.get(INPUT_PARAM_SOURCE).concat(parameters.get(INPUT_PARAM_TARGET));
            ctx.setAttribute(parameters.get("outputPath"), result);
    }

    /**
     * Provides url encoding functionality to Directed Graphs.
     * <p>
     * Will url encode the source and write the result to outputPath
     * @param parameters HashMap<String,String> of parameters passed by the DG to this function
     * <table border="1">
     *  <thead><th>parameter</th><th>Mandatory/Optional</th><th>description</th></thead>
     *  <tbody>
     *      <tr><td>source</td><td>Mandatory</td><td>source string</td></tr>
     *      <tr><td>encoding</td><td>Optional</td><td>the name of a supported character encoding, defaulted to UTF-8 if not supplied</td></tr>
     *      <tr><td>outputPath</td><td>Mandatory</td><td>the location in context memory the result is written to</td></tr>
     *  </tbody>
     * </table>
     * @param ctx Reference to context memory
     * @throws SvcLogicException
     */
    public static void urlEncode(Map<String, String> parameters, SvcLogicContext ctx) throws SvcLogicException {
        SliPluginUtils.checkParameters(parameters, new String[] { INPUT_PARAM_SOURCE, "outputPath" }, LOG);
        String encoding = parameters.get("encoding");
        if (encoding == null) {
            encoding = "UTF-8";
        }
        try {
            String result = URLEncoder.encode(parameters.get(INPUT_PARAM_SOURCE), encoding);
            ctx.setAttribute(parameters.get("outputPath"), result);
        } catch (UnsupportedEncodingException e) {
            throw new SvcLogicException("Url encode failed.", e);
        }
    }

	/**
	 * xmlEscapeText() will be used to format input xml with text.
	 *
	 * @param inParams
	 *            accepts the instance of {@link Map} holds the input xml in string
	 *            format.
	 * @param ctx
	 *            accepts the instance of {@link SvcLogicContext} holds the service
	 *            logic context.
	 *
	 */
	public static void xmlEscapeText(Map<String, String> inParams, SvcLogicContext ctx) {
		String source = inParams.get(INPUT_PARAM_SOURCE);
		String target = inParams.get(INPUT_PARAM_TARGET);
		source = StringEscapeUtils.escapeXml(source);
		ctx.setAttribute(target, source);
	}

	/**
	* unescapeJsonString takes an escaped json string stored as a single property in context memory and unescapes it storing it as a single property
	* @param parameters - requires source and outputPath to not be null.
	* @param ctx Reference to context memory
	* @throws SvcLogicException if a required parameter is missing an exception is thrown
	*/
    public static void unescapeJsonString(Map<String, String> parameters, SvcLogicContext ctx) throws SvcLogicException {
	SliPluginUtils.checkParameters(parameters, new String[] { INPUT_PARAM_SOURCE, INPUT_PARAM_TARGET }, LOG);
	try {
	    String source = parameters.get(INPUT_PARAM_SOURCE);
	    String target = parameters.get(INPUT_PARAM_TARGET);
	    String unescapedJson = StringEscapeUtils.unescapeJson(source);
	    ctx.setAttribute(target, unescapedJson);
	} catch (Exception ex) {
	    ex.printStackTrace();
	    throw new SvcLogicException("problem with unescapeJsonString", ex);
	}
    }

	/**
	* escapeJsonString takes json stored as a single string in context memory and escapes it storing it as a single property
	* @param parameters - requires source and outputPath to not be null.
	* @param ctx Reference to context memory
	* @throws SvcLogicException if a required parameter is missing an exception is thrown
	*/
    public static void escapeJsonString(Map<String, String> parameters, SvcLogicContext ctx) throws SvcLogicException {
	SliPluginUtils.checkParameters(parameters, new String[] { INPUT_PARAM_SOURCE, INPUT_PARAM_TARGET }, LOG);
	try {
	    String source = parameters.get(INPUT_PARAM_SOURCE);
	    String target = parameters.get(INPUT_PARAM_TARGET);
	    String unescapedJson = StringEscapeUtils.escapeJson(source);
	    ctx.setAttribute(target, unescapedJson);
	} catch (Exception ex) {
	    ex.printStackTrace();
	    throw new SvcLogicException("problem with escapeJsonString", ex);
	}
    }

    public static String isBlank(Map<String, String> parameters, SvcLogicContext ctx) throws SvcLogicException {
        String ctxLocation = parameters.get(INPUT_PARAM_KEY);
        String str = ctx.getAttribute(ctxLocation);
        if (str == null || str.isEmpty() || " ".equals(str)) {
            return TRUE_CONSTANT;
        }
        return FALSE_CONSTANT;
    }

    public static String isEmpty(Map<String, String> parameters, SvcLogicContext ctx) throws SvcLogicException {
        String ctxLocation = parameters.get(INPUT_PARAM_KEY);
        String str = ctx.getAttribute(ctxLocation);
        if (str == null || str.isEmpty()) {
            return TRUE_CONSTANT;
        }
        return FALSE_CONSTANT;
    }

    public static String isNull(Map<String, String> parameters, SvcLogicContext ctx) throws SvcLogicException {
        String ctxLocation = parameters.get(INPUT_PARAM_KEY);
        String str = ctx.getAttribute(ctxLocation);
        if (str == null) {
            return TRUE_CONSTANT;
        }
        return FALSE_CONSTANT;
    }
}

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * A utility class used to streamline the interface between Java plugins,
 * the Service Logic Context, and Directed Graphs.
 * @version 7.0.1
 * @see org.onap.ccsdk.sli.core.sli.SvcLogicContext
 */
public class SliPluginUtils implements SvcLogicJavaPlugin {
	public enum LogLevel {
		TRACE, DEBUG, INFO, WARN, ERROR;
	}

	private static final Logger LOG = LoggerFactory.getLogger(SliPluginUtils.class);
	private static final String LOG_MSG="extracting list from context memory";
	private static final String LOG_MSG1="removing elements from list";
	private static final String LENGTH="_length";


	// ========== CONSTRUCTORS ==========

	public SliPluginUtils() {}

	public SliPluginUtils( Properties props ) {}


	// ========== CONTEXT MEMORY FUNCTIONS ==========

	/**
	 * Removes 1 or more elements from a list in context memory.
	 * <p>
	 * Values are removed based on either the index in the list, a key-value
	 * pair, or a list of key-value pairs that all must match in the element.
	 * @param parameters
	 * @param ctx Reference to context memory
	 * @throws SvcLogicException All exceptions are wrapped in
	 * SvcLogicException for compatibility with SLI.
	 * @since 7.0.1
	 */
	public void ctxListRemove( Map<String,String> parameters, SvcLogicContext ctx ) throws SvcLogicException {
		try{
			LOG.debug( "ENTERING Execute Node \"ctxListRemove\"" );

			// Validate, Log, & read parameters
			checkParameters(parameters, new String[]{"list_pfx"}, LOG);
			logExecuteNodeParameters(parameters, LOG, LogLevel.DEBUG);
			String list_pfx = parameters.get("list_pfx");
			String param_index = parameters.get("index");
			String param_key = parameters.get("key");
			String param_value = parameters.get("value");
			String param_keys_length = parameters.get("keys_length");

			// Initialize context memory list mimic
			SvcLogicContextList list;

			// Process based on input parameters:
			//   index: remove object at specific index
			//   key & value: remove all objects with key-value pair
			//   keys_length: remove all objects that match all key-value pairs
			//                in list
			if( param_index != null ) {
				// Parse index
				LOG.trace("executing remove by index logic");
				int index;
				try {
					index = Integer.parseInt(param_index);
				}
				catch( NumberFormatException e ) {
					throw new IllegalArgumentException("\"index\" parameter is not a number. index = " + param_index, e);
				}

				// Extract list from context memory & remove object @ index
				LOG.trace(LOG_MSG);
				list = SvcLogicContextList.extract(ctx, list_pfx);
				LOG.trace(LOG_MSG1);
				list.remove(index);
			}
			else if( param_value != null ) {
				if( param_key == null ) { param_key = ""; }

				// Extract list from context memory & remove objects with
				// key-value pair
				LOG.trace("executing remove by key-value pair logic");
				LOG.trace(LOG_MSG);
				list = SvcLogicContextList.extract(ctx, list_pfx);
				LOG.trace(LOG_MSG1);
				list.remove( param_key, param_value );
			}
			else if( param_keys_length != null ) {
				// Parse keys_length
				LOG.trace("executing remove by key-value pair list logic");
				int keys_length;
				try {
					keys_length = Integer.parseInt(param_keys_length);
				}
				catch( NumberFormatException e ) {
					throw new IllegalArgumentException("\"keys_length\" parameters is not a number. keys_length = " + param_keys_length, e);
				}

				// Obtain key-value pairs to check from parameters
				LOG.trace("reading keys parameter list");
				HashMap<String,String> keys_values = new HashMap<>();
				for( int i = 0; i < keys_length; i++ ) {
					keys_values.put(parameters.get("keys[" + i + "].key"), parameters.get("keys[" + i + "].value"));
				}

				// Extract list from context memory & remove objects with all
				// key-value pairs matching
				LOG.trace(LOG_MSG);
				list = SvcLogicContextList.extract(ctx, list_pfx);
				LOG.trace(LOG_MSG1);
				list.remove(keys_values);
			}
			else {
				throw new IllegalArgumentException("Required parameters missing. Requires one of: index, key & value, or keys_length array");
			}

			// Remove index from list
			LOG.trace("writing list back into context memory");
			list.writeToContext(ctx);
		}
		catch( Exception e ) {
			throw new SvcLogicException( "An error occurred in the ctxListRemove Execute node", e );
		}
		finally {
			LOG.debug( "EXITING Execute Node \"ctxListRemove\"" );
		}
	}

    /**
     * ctxSortList
     * @param parameters - the set of required parameters must contain list and delimiter.
     * @param ctx Reference to context memory
     * @throws SvcLogicException if a required parameter is missing an exception is thrown
     */
	public void ctxSortList( Map<String, String> parameters, SvcLogicContext ctx ) throws SvcLogicException {
		checkParameters(parameters, new String[]{"list","delimiter"}, LOG);
		ArrayList<SortableCtxListElement> list = new ArrayList<>();

		String[] sort_fields = null;
		if( parameters.containsKey("sort-fields") ) {
			sort_fields = parameters.get("sort-fields").split(parameters.get("delimiter"), 0);
		}

		String ctx_list_str = parameters.get("list");
		int listSz = getArrayLength(ctx, ctx_list_str);



		for( int i = 0; i < listSz; i++ ) {
			list.add( new SortableCtxListElement(ctx, ctx_list_str + '[' + i + ']', sort_fields) );
		}
		Collections.sort(list);

		ctxBulkErase(ctx, ctx_list_str);
		int i = 0;
		for( SortableCtxListElement list_element : list ) {
			for( Map.Entry<String,String> entry : list_element.child_elements.entrySet() ) {
				if( sort_fields == null ) {
					ctx.setAttribute(ctx_list_str + '[' + i + ']', entry.getValue());
				}
				else {
					ctx.setAttribute(ctx_list_str + '[' + i + "]." + entry.getKey(), entry.getValue());
				}
			}
			i++;
		}
		// Reset list length (removed by ctxBulkErase above)
		ctx.setAttribute(ctx_list_str+LENGTH, Integer.toString(listSz));
	}

    /**
     * generates a UUID and writes it to context memory
     * @param parameters - ctx-destination is a required parameter
     * @param ctx Reference to context memory
     * @throws SvcLogicException thrown if a UUID cannot be generated or if ctx-destination is missing or null
     */
	public void generateUUID( Map<String, String> parameters, SvcLogicContext ctx )  throws SvcLogicException {
		checkParameters(parameters, new String[]{"ctx-destination"}, LOG);
		ctx.setAttribute(parameters.get("ctx-destination"), UUID.randomUUID().toString() );
	}

	/**
	 * Provides substring functionality to Directed Graphs.
	 * <p>
	 * Calls either String.substring(String beginIndex) or
	 * String.substring(String beginInded, String endIndex) if the end-index
	 * is present or not.
	 * @param parameters HashMap<String,String> of parameters passed by the DG to this function
	 * <table border="1">
	 * 	<thead><th>parameter</th><th>Mandatory/Optional</th><th>description</th></thead>
	 * 	<tbody>
	 * 		<tr><td>string</td><td>Mandatory</td><td>String to perform substring on</td></tr>
	 * 		<tr><td>result</td><td>Mandatory</td><td>Key in context memory to populate the resulting string in</td></tr>
	 * 		<tr><td>begin-index</td><td>Mandatory</td><td>Beginning index to pass to Java substring function</td></tr>
	 * 		<tr><td>end-index</td><td>Optional</td><td>Ending index to pass to Java substring function. If not included, String.substring(begin) will be called.</td></tr>
	 * 	</tbody>
	 * </table>
	 * @param ctx Reference to context memory
	 * @throws SvcLogicException
	 * @since 8.0.1
	 * @see SliPluginUtils#substring(Map, SvcLogicContext)
	 */
	@Deprecated
	public void substring( Map<String, String> parameters, SvcLogicContext ctx ) throws SvcLogicException {
		try {
			checkParameters( parameters, new String[]{"string","begin-index","result"}, LOG );
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



	// ========== PUBLIC STATIC UTILITY FUNCTIONS ==========

	/**
	 * Throws an exception and writes an error to the log file if a required
	 * parameters is not found in the parametersMap.
	 * <p>
	 * Use at the beginning of functions that can be called by Directed Graphs
	 * and can take parameters to verify that all parameters have been provided
	 * by the Directed Graph.
	 * @param parametersMap parameters Map passed to this node
	 * @param requiredParams Array of parameters required by the calling function
	 * @param log Reference to Logger to log to
	 * @throws SvcLogicException if a String in the requiredParams array is
	 * not a key in parametersMap.
	 * @since 1.0
	 */
        public static final void checkParameters(Map<String, String> parametersMap, String[] requiredParams, Logger log) throws SvcLogicException {
        if( requiredParams == null || requiredParams.length < 1){
            log.debug("required parameters was empty, exiting early.");
            return;
        }
        if (parametersMap == null || parametersMap.keySet().isEmpty()){
            String errorMessage = "This method requires the parameters [" +   StringUtils.join(requiredParams,",") + "], but no parameters were passed in.";
            log.error(errorMessage);
            throw new SvcLogicException(errorMessage);
        }

    	for (String param : requiredParams) {
    	    if (!parametersMap.containsKey(param)) {
    		String errorMessage = "Required parameter \"" + param + "\" was not found in parameter list.";
    		log.error(errorMessage);
    		log.error("Total list of required parameters is [" + StringUtils.join(requiredParams, ",") + "].");
    		throw new SvcLogicException(errorMessage);
    	    }
    	}
        }

	/**
	 * Removes all key-value pairs with keys that begin with pfx
	 * @param ctx Reference to context memory
	 * @param pfx Prefix of key-value pairs to remove
	 * @since 1.0
	 */
	public static final void ctxBulkErase( SvcLogicContext ctx, String pfx ) {
		ArrayList<String> Keys = new ArrayList<>(ctx.getAttributeKeySet());
		for( String key : Keys ) {
			if( key.startsWith( pfx ) ) {
				ctx.setAttribute( pfx + key.substring(pfx.length()) , null);
			}
		}
	}

	/**
	 * Copies all context memory key-value pairs that start with src_pfx to
	 * the keys that start with dest_pfx + suffix, where suffix is the result
	 * of {@code key.substring(src_pfx.length())}.
	 * <p>
	 * Does NOT guarantee removal of all keys at the destination before
	 * copying, but will overwrite any destination keys that have a
	 * corresponding source key. Use {@link #ctxBulkErase(SvcLogicContext, String) ctxBulkErase}
	 * before copy to erase destination root before copying from source.
	 * @param ctx Reference to context memory.
	 * @param src_pfx Prefix of the keys to copy values from.
	 * @param dest_pfx Prefix of the keys to copy values to.
	 * @since 1.0
	 */
	public static final void ctxBulkCopy( SvcLogicContext ctx, String src_pfx, String dest_pfx ) {
		// Remove trailing period from dest_pfx
		if( dest_pfx.charAt(dest_pfx.length()-1) == '.' ) {
			dest_pfx = dest_pfx.substring(0,dest_pfx.length()-1);
		}

		// For each context key that begins with src_pfx, set the value of the
		// key dest_pfx + the suffix of the key to the key's value
		ArrayList<String> Keys = new ArrayList<>(ctx.getAttributeKeySet());
		for( String key : Keys ) {
			if( key.startsWith(src_pfx) ) {
				// Get suffix (no leading period)
				String suffix = key.substring(src_pfx.length());
				if( suffix.charAt(0) == '.') {
					suffix = suffix.substring(1);
				}

				// Set destination's value to key's value
				ctx.setAttribute(dest_pfx + '.' + suffix, ctx.getAttribute(key));
			}
		}
	}

	/**
	 * Creates and returns a {@code Map<String, String>} that is a subset of
	 * context memory where all keys begin with the prefix.
	 * @param ctx Reference to context memory.
	 * @param prefix Returned map's keys should all begin with this value.
	 * @return A {@code Map<String, String>} containing all the key-value pairs
	 * in ctx whose key begins with prefix.
	 */
	public static final Map<String, String> ctxGetBeginsWith( SvcLogicContext ctx, String prefix ) {
		Map<String, String> prefixMap = new HashMap<>();

		for( String key : ctx.getAttributeKeySet() ) {
			if( key.startsWith(prefix) ) {
				prefixMap.put( key, ctx.getAttribute(key) );
			}
		}

		return prefixMap;
	}

	/**
	 * Returns true if key's value in context memory is "" or if it doesn't
	 * exist in context memory.
	 * @param ctx Reference to context memory.
	 * @param key Key to search for.
	 * @return true if key's value in context memory is "" or if it doesn't
	 * exist in context memory.
	 * @since 1.0
	 */
	public static final boolean ctxKeyEmpty( SvcLogicContext ctx, String key ) {
		String value = ctx.getAttribute(key);
		return value == null || value.isEmpty();
	}

	/**
	 * Adds all key-value pairs in the entries Map to context memory.
	 * @param ctx Reference to context memory. Value's {@code toString()}
	 * function is used to add it.
	 * @param entries {@code Map<String, ?>} of key-value pairs to add to
	 * context memory. Value's {@code toString()} function is used to add it.
	 * @return Reference to context memory to be used for function chaining.
	 */
	public static final SvcLogicContext ctxPutAll( SvcLogicContext ctx, Map<String, ?> entries ) {
		for( Map.Entry<String, ?> entry : entries.entrySet() ) {
			ctxSetAttribute( ctx, entry.getKey(), entry.getValue() );
			//ctx.setAttribute(entry.getKey(), entry.getValue().toString());
		}

		return ctx;
	}

	/**
	 * Sets a key in context memory to the output of object's toString(). The
	 * key is deleted from context memory if object is null.
	 * @param ctx Reference to context memory.
	 * @param key Key to set.
	 * @param object Object whose toString() will be the value set
	 */
	public static final void ctxSetAttribute( SvcLogicContext ctx, String key, Object object ) {
		if( object == null ) {
			ctx.setAttribute(key, null);
		}
		else {
			ctx.setAttribute(key, object.toString());
		}
	}

	/**
	 * Sets a key in context memory to the output of object's toString().
	 * <p>
	 * The key is deleted from context memory if object is null. The key and
	 * value set in context memory are logged to the Logger at the provided
	 * logLevel level.
	 * @param <O> Any Java object
	 * @param ctx Reference to context memory.
	 * @param key Key to set.
	 * @param obj Object whose toString() will be the value set
	 * @param LOG Logger to log to
	 * @param logLevel level to log at in Logger
	 */
	public static final <O extends Object> void ctxSetAttribute( SvcLogicContext ctx, String key, O obj, Logger LOG, LogLevel logLevel ) {
		String value = Objects.toString( obj, null );
		ctx.setAttribute( key, value );
		if( logLevelIsEnabled(LOG, logLevel ) ) {
			if( value == null ) {
				logMessageAtLevel( LOG, logLevel, "Deleting " + key );
			}
			else {
				logMessageAtLevel( LOG, logLevel, "Setting " + key + " = " + value );
			}
		}
	}

	/**
	 * Utility function used to get an array's length from context memory.
	 * Will return 0 if key doesn't exist in context memory or isn't numeric.
	 * <p>
	 * Use to obtain a context memory array length without having to worry
	 * about throwing a NumberFormatException.
	 * @param ctx Reference to context memory
	 * @param key Key in context memory whose value is the array's length. If
	 * the key doesn't end in "_length", then "_length is appended.
	 * @param log Reference to Logger to log to
	 * @return The array length or 0 if the key is not found in context memory.
	 * @since 1.0
	 */
	public static final int getArrayLength( SvcLogicContext ctx, String key ) {
		return getArrayLength(ctx, key, null, null, null);
	}

	/**
	 * Utility function used to get an array's length from context memory.
	 * Will return 0 if key doesn't exist in context memory or isn't numeric
	 * and print the provided log message to the configured log file.
	 * <p>
	 * Use to obtain a context memory array length without having to worry
	 * about throwing a NumberFormatException.
	 * @param ctx Reference to context memory.
	 * @param key Key in context memory whose value is the array's length. If
	 * the key doesn't end in "_length", then "_length is appended.
	 * @param log Reference to Logger to log to. Doesn't log if null.
	 * @param logLevel Logging level to log the message at if the context
	 * memory key isn't found. Doesn't log if null.
	 * @param log_message Message to log if the context memory key isn't found.
	 * Doesn't log if null.
	 * @return The array length or 0 if the key is not found in context memory.
	 * @since 1.0
	 */
	public static final int getArrayLength( SvcLogicContext ctx, String key, Logger log, LogLevel logLevel, String log_message ) {
		String ctxKey = key.endsWith(LENGTH) ? key : key + LENGTH;
		try {
			return Integer.parseInt(ctx.getAttribute(ctxKey));
		}
		catch( NumberFormatException e ) {
			if( log != null && logLevel != null && log_message != null ) {
				switch( logLevel ) {
					case TRACE:
						log.trace(log_message);
						break;
					case DEBUG:
						log.debug(log_message);
						break;
					case INFO:
						log.info(log_message);
						break;
					case WARN:
						log.warn(log_message);
						break;
					case ERROR:
						log.error(log_message);
						break;
				}
			}
		}

		return 0;
	}

	/**
	 * Prints sorted context memory key-value pairs to the log file at the log
	 * level. Returns immediately if the log level isn't enabled.
	 * <p>
	 * O(n log(n)) time where n = size of context memory
	 * @param ctx Reference to context memory
	 * @param log Reference to Logger to log to
	 * @param logLevel Logging level to log the context memory key-value pairs
	 * at.
	 * @since 1.0
	 */
	public static final void logContextMemory( SvcLogicContext ctx, Logger log, LogLevel logLevel ) {
		logLevelIsEnabled( log, logLevel );

		// Print sorted context memory key-value pairs to the log
		ArrayList<String> keys = new ArrayList<>(ctx.getAttributeKeySet());
		Collections.sort(keys);
		for( String key : keys ) {
			logMessageAtLevel( log, logLevel, key + " = " + ctx.getAttribute(key) );
		}
	}



	// ========== PRIVATE FUNCTIONS ==========

	// TODO: javadoc
	/**
	 *
	 * @param parameters
	 * @param log
	 * @param loglevel
	 * @since 7.0.1
	 */
	public static final void logExecuteNodeParameters( Map<String,String> parameters, Logger log, LogLevel loglevel ) {
		logLevelIsEnabled( log, loglevel );

		for( Map.Entry<String,String> param : parameters.entrySet() ) {
			logMessageAtLevel( log, loglevel, "PARAM: " + param.getKey() + " = " + param.getValue() );
		}
	}

	// TODO: javadoc
	/**
	 * Returns true if the loglevel is enabled. Otherwise, returns false.
	 * @param log Reference to logger
	 * @param loglevel Log level to check if enabled
	 * @return True if the loglevel is enabled. Otherwise, false
	 * @since 7.0.1
	 */
	private static final boolean logLevelIsEnabled( Logger log, LogLevel loglevel ) {
		// Return immediately if logging level isn't enabled
		switch( loglevel ) {
			case TRACE:
				if( log.isTraceEnabled() ) { return true; }
				return false;
			case DEBUG:
				if( log.isDebugEnabled() ) { return true; }
				return false;
			case INFO:
				if( log.isInfoEnabled() ) { return true; }
				return false;
			case WARN:
				if( log.isWarnEnabled() ) { return true; }
				return false;
			case ERROR:
				if( log.isErrorEnabled() ) { return true; }
				return false;
			default:
				throw new IllegalArgumentException("Unknown LogLevel: " + loglevel.toString());
		}
	}

	// TODO: javadoc
	/**
	 *
	 * @param log
	 * @param loglevel
	 * @param msg
	 * @since 7.0.1
	 */
	private static final void logMessageAtLevel( Logger log, LogLevel loglevel, String msg ) {
		switch( loglevel ) {
			case TRACE:
				log.trace(msg);
				return;
			case DEBUG:
				log.debug(msg);
				return;
			case INFO:
				log.info(msg);
				return;
			case WARN:
				log.warn(msg);
				return;
			case ERROR:
				log.error(msg);
				return;
		}
	}



	// ========== LOCAL CLASSES ==========

	private class SortableCtxListElement implements Comparable<SortableCtxListElement> {
		HashMap<String,String> child_elements = new HashMap<>();
		String[] sort_fields;

		public SortableCtxListElement( SvcLogicContext ctx, String root, String[] sort_fields ) {
			this.sort_fields = sort_fields;

			for( String key : ctx.getAttributeKeySet() ) {
				if( key.startsWith(root) ) {
					if( key.length() == root.length() ) {
						child_elements.put("", ctx.getAttribute(key));
						break;
					}
					else {
						child_elements.put(key.substring(root.length()+1), ctx.getAttribute(key));
					}
				}
			}
		}

		@Override
		public int compareTo(SortableCtxListElement arg0) {
			if( sort_fields == null ) {
				return this.child_elements.get("").compareTo(arg0.child_elements.get(""));
			}

			for( String field : this.sort_fields ) {
				int result = this.child_elements.get(field).compareTo(arg0.child_elements.get(field));
				if( result != 0 ) {
					return result;
				}
			}

			return 0;
		}

                @Override
		public boolean equals(Object object) {
			if (this == object) {
				return true;
			}
			if (!(object instanceof SortableCtxListElement)) {
				return false;
			}
			if (!super.equals(object)) {
				return false;
			}

			SortableCtxListElement that = (SortableCtxListElement) object;

			if (child_elements != null ? !child_elements.equals(that.child_elements)
					: that.child_elements != null) {
				return false;
			}
			// Probably incorrect - comparing Object[] arrays with Arrays.equals
			if (!Arrays.equals(sort_fields, that.sort_fields)) {
				return false;
			}

			return true;
		}

                @Override
		public int hashCode() {
			int result = super.hashCode();
			result = 31 * result + (child_elements != null ? child_elements.hashCode() : 0);
			result = 31 * result + Arrays.hashCode(sort_fields);
			return result;
		}
	}

	/**
     * Creates a file that contains the content of context memory.
     * @param parameters - must contain the parameter filename
     * @param ctx Reference to context memory
     * @throws SvcLogicException thrown if file cannot be created or if parameters are missing
     */
	public static void printContext(Map<String, String> parameters, SvcLogicContext ctx) throws SvcLogicException {
		if (parameters == null || parameters.isEmpty()) {
			throw new SvcLogicException("no parameters passed");
		}

		checkParameters(parameters, new String[]{"filename"}, LOG);

		String fileName = parameters.get("filename");


		try (FileOutputStream fstr = new FileOutputStream(new File(fileName));
			 PrintStream pstr = new PrintStream(fstr, true);)
		{
			pstr.println("#######################################");
			for (String attr : ctx.getAttributeKeySet()) {
				pstr.println(attr + " = " + ctx.getAttribute(attr));
			}
		} catch (Exception e) {
			throw new SvcLogicException("Cannot write context to file " + fileName, e);
		}


	}

	 /**
     * Checks context memory for a set of required parameters
     * Every parameter aside from prefix will be treated as mandatory
     * @param parameters HashMap<String,String> of parameters passed by the DG to this function
     * <table border="1">
     *  <thead><th>parameter</th><th>Mandatory/Optional</th><th>description</th></thead>
     *  <tbody>
     *      <tr><td>prefix</td><td>Optional</td><td>the prefix will be added to each parameter</td></tr>
     *  </tbody>
     * </table>
     * @param ctx Reference to context memory
     * @throws SvcLogicException
     * @since 11.0.2
     */
	public static void requiredParameters(Map<String, String> parameters, SvcLogicContext ctx) throws SvcLogicException {
		if (parameters == null || parameters.keySet().isEmpty()) {
            String errorMessage = "requiredParameters should not be called if the parameters hashmap is null or empty!";
            LOG.error(errorMessage);
            throw new SvcLogicException(errorMessage);
		}
		String prefixValue = null;
		String prefix = "prefix";
		if(parameters.containsKey(prefix)){
		    prefixValue = parameters.get(prefix);
		    parameters.remove(prefix);
		}
		checkParameters(prefixValue, ctx.getAttributeKeySet(), parameters.keySet(), LOG);
	}

    private static void checkParameters(String prefixValue, Set<String> ctx, Set<String> parameters, Logger log) throws SvcLogicException {
        for (String param : parameters) {
            if (prefixValue != null) {
                param = prefixValue + param;
            }
            if (!ctx.contains(param)) {
                String errorMessage = "This method requires the parameters [" + StringUtils.join(parameters, ",")
                        + "], but " + param + " was not passed in.";
                log.error(errorMessage);
                throw new SvcLogicException(errorMessage);
            }
        }
    }

    /**
     *  is in a different DG invocation just before/after we call NCS and set the state to InProgress
     */
    /**
    * setTime write the current date time to a string located at outputPath
    * @param parameters - requires outputPath to not be null
    * @param ctx Reference to context memory
    * @throws SvcLogicException if a required parameter is missing an exception is thrown
    */
    public static void setTime(Map<String, String> parameters, SvcLogicContext ctx) throws SvcLogicException
    {
        checkParameters(parameters, new String[] { "outputPath" }, LOG);

        // Set the DateFormat
        // "2015-03-16T12:18:35.138Z"
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        // Parse the date
        String ctxVariable = parameters.get("outputPath");
        try {
            String dateTime = format.format(new Date());
            ctx.setAttribute(ctxVariable, dateTime);
        } catch (Exception ex) {
            throw new SvcLogicException("problem with setTime", ex);
        }
    }

    /**
    * jsonStringToCtx takes a json string stored as a single property in context memory and breaks it into individual properties
    * @param parameters - requires source, outputPath and isEscaped to not be null.
    * @param ctx Reference to context memory
    * @throws SvcLogicException if a required parameter is missing an exception is thrown
    */
    public static void jsonStringToCtx(Map<String, String> parameters, SvcLogicContext ctx) throws SvcLogicException
    {
        checkParameters(parameters, new String[] { "source","outputPath","isEscaped" }, LOG);
        try {
            String source = ctx.getAttribute(parameters.get("source"));
            if("true".equals(parameters.get("isEscaped"))){
                source = StringEscapeUtils.unescapeJson(source);
            }
            writeJsonToCtx(source, ctx,parameters.get("outputPath"));
        } catch (Exception ex) {
            throw new SvcLogicException("problem with jsonStringToCtx", ex);
        }
    }

    protected static void writeJsonToCtx(String resp, SvcLogicContext ctx, String prefix){
        JsonParser jp = new JsonParser();
        JsonElement element = jp.parse(resp);
        String root = prefix + ".";
        if (element.isJsonObject()) {
            writeJsonObject(element.getAsJsonObject(), ctx, root);
        } else if (element.isJsonArray()) {
            handleJsonArray("", element.getAsJsonArray(), ctx, root);
        }
    }

    protected static void writeJsonObject(JsonObject obj, SvcLogicContext ctx, String root) {
        for (Entry<String, JsonElement> entry : obj.entrySet()) {
            String key = entry.getKey();
            if (entry.getValue().isJsonObject()) {
                writeJsonObject(entry.getValue().getAsJsonObject(), ctx, root + key + ".");
            } else if (entry.getValue().isJsonArray()) {
                JsonArray array = entry.getValue().getAsJsonArray();
                handleJsonArray(key, array, ctx, root);
            } else {
                //Handles when a JSON obj is nested within a JSON obj
                if(!root.endsWith(".")){
                    root = root + ".";
                }
                ctx.setAttribute(root + key, entry.getValue().getAsString());
            }
        }
    }

    protected static void handleJsonArray(String key, JsonArray array, SvcLogicContext ctx, String root) {
        ctx.setAttribute(root + key + LENGTH, String.valueOf(array.size()));
        Integer arrayIdx = 0;
        for (JsonElement element : array) {
            String prefix = root + key + "[" + arrayIdx + "]";

            if (element.isJsonArray()) {
                handleJsonArray(key, element.getAsJsonArray(), ctx, prefix);
            } else if (element.isJsonObject()) {
                writeJsonObject(element.getAsJsonObject(), ctx, prefix + ".");
            } else if (element.isJsonPrimitive()) {
                ctx.setAttribute(prefix, element.getAsString());
            }
            arrayIdx++;
        }
    }

    /**
     * getAttributeValue takes a ctx memory path as a string, gets the value stored at this path and set this value in context memory at
     * outputPath
     * @param parameters - requires source and outputPath
     * @param ctx Reference to context memory
     * @throws SvcLogicException if a required parameter is missing an exception is thrown
     */
     public static void getAttributeValue(Map<String, String> parameters, SvcLogicContext ctx) throws SvcLogicException {
         checkParameters(parameters, new String[] { "source", "outputPath" }, LOG);
         String source = ctx.getAttribute(parameters.get("source"));
         ctx.setAttribute(parameters.get("outputPath"), source);
     }

	/**
	 * ctxListContains provides a way to see if a context memory list contains a key value
	 * @param parameters - requires list, keyName, keyValue, outputPath to all not be null.
	 * @param ctx        Reference to context memory
	 * @throws SvcLogicException if a required parameter is missing an exception is thrown
	 */
	public static String ctxListContains(Map<String, String> parameters, SvcLogicContext ctx) throws SvcLogicException {
		checkParameters(parameters, new String[]{"list", "keyName", "keyValue"}, LOG);

		try {
			String ctxList = parameters.get("list");
			ctxList = (ctxList.endsWith(LENGTH)) ? ctxList : ctxList + LENGTH;
			int listLength = getArrayLength(ctx, ctxList);

			if (listLength == 0) {
				LOG.debug("List is not in context memory");
				return "false";
			} else {
				Set<String> keys = new HashSet<String>();

				String listPrefix = ctxList.substring(0, ctxList.lastIndexOf("_")) + "[";
				String listSuffix = "]." + parameters.get("keyName");

				for (int i = 0; i < listLength; i++) {
					String keyLocation = listPrefix + i + listSuffix;
					keys.add(ctx.getAttribute(keyLocation));
				}

				if (keys.contains(parameters.get("keyValue"))) {
					LOG.debug("List " + parameters.get("list") + " contains " + parameters.get("keyValue"));
					return "true";
				} else {
					LOG.debug("List " + parameters.get("list") + " do not contains " + parameters.get("keyValue"));
					return "false";
				}
			}
		} catch (Exception ex) {
			throw new SvcLogicException("ctxListContains failed", ex);
		}
	}

	/**
	 * set properties in context memory for a container </br>
	 * parameters with a null or empty key or value are ignored </br>
	 * required parameter root - root + "." + parameters.key
	 * is the key to set the value too value in context memory </br>
	 * optional parameter valueRoot - if set: valueRoot + "." + parameters.value
	 * is the key to get the value from context memory
	 *
	 * @param parameters - root (required), valueRoot (optional), properties names and values to be set
	 * @param ctx        Reference to context memory
	 * @return success or failure of operation
	 */
	public static String setPropertiesForRoot(Map<String, String> parameters, SvcLogicContext ctx) {
		LOG.debug("Execute Node \"setPropertiesForRoot\"");
		try {
			checkParameters(parameters, new String[]{"root"}, LOG);
		} catch (Exception ex) {
			return "failure";
		}

		String root = parameters.get("root");

		if (StringUtils.isEmpty(root)) {
			return "failure";
		}

		// set context memory to the the properties passed with root as prefix
		setParameterValuesToRoot(parameters, ctx, root);

		return "success";
	}

	private static boolean setParameterValuesToRoot(Map<String, String> parameters, SvcLogicContext ctx, String root) {
		boolean changeFlag = false;
		String valueRoot = parameters.get("valueRoot");

		for (Map.Entry<String, String> entry : parameters.entrySet()) {
			// ignore if it's the root parameter
			if (!entry.getKey().equals("root")) {
				String keyToBeSet = root + "." + entry.getKey();
				String valueToBeSet = "";

				if (StringUtils.isEmpty(valueRoot)) {
					valueToBeSet = entry.getValue();
				} else {
					valueToBeSet = ctx.getAttribute(valueRoot + "." + entry.getValue());
				}

				LOG.debug("Setting context memory: " + keyToBeSet + " = " + valueToBeSet);

				if (!StringUtils.isEmpty(entry.getKey()) && !StringUtils.isEmpty(valueToBeSet)) {
					ctxSetAttribute(ctx, keyToBeSet, valueToBeSet);
					changeFlag = true;
				}
			}
		}

		return changeFlag;
	}

	/**
	 * takes container list and set the properties with the value provided </br>
	 * parameters with a null or empty key or value are ignored </br>
	 * required parameters </br>
	 * prefixKey + "." + parameters.key is the key to set the value too value in context memory </br>
	 * prefixKey + "[index]." + keyName is the key of the entry in the list in context memory </br>
	 * keyValue is the value of the key of the list entry in context memory (must be actual value)</br>
	 * optional parameter valuePrefixKey - if set: valuePrefixKey + "." + parameters.value
	 * is the key to get the value from context memory
	 *
	 * @param parameters </br>
	 * 					 - prefixKey e.g "service-data.universal-cpe-ft.l2-switch-interfaces" </br>
	 *                   - keyName e.g "name" </br>
	 *                   - keyValue e.g "WAN1" (must be actual value and not use the prefixKey as root) </br>
	 *                   - valuePrefixKey (optional) e.g "input.universal-cpe-ft.l2-switch-interfaces[1] </br>
	 *                   - properties to be set, values for the properties </br>
	 * @param ctx        reference to context memory
	 * @return success or failure of operation
	 */
	public static String setPropertiesForList(Map<String, String> parameters, SvcLogicContext ctx) {
		LOG.debug("Execute Node \"setPropertiesForList\"");
		try {
			checkParameters(parameters, new String[]{"prefixKey", "keyName", "keyValue"}, LOG);
		} catch (Exception e) {
			LOG.error("a required parameter is missing");
			return "failure";
		}

		String prefixKey = parameters.get("prefixKey");
		String keyName = parameters.get("keyName");
		String keyValue = parameters.get("keyValue");

		if (StringUtils.isEmpty(keyName) || StringUtils.isEmpty(keyValue) || StringUtils.isEmpty(prefixKey)) {
			LOG.error("a required parameters value is empty or null");
			return "failure";
		}

		int listLength = getArrayLength(ctx, prefixKey);

		Map<String, String> containParams = new HashMap<>();
		containParams.put("list", prefixKey);
		containParams.put("keyName", keyName);
		containParams.put("keyValue", keyValue);

		String valuePrefixKey = parameters.get("valuePrefixKey");

		try {
			// create new list in context memory
			if (listLength == 0) {
				// since there's no length found make sure there's no current data at prefixKey in context memory
				Map<String, String> map = ctxGetBeginsWith(ctx, prefixKey);

				if (map.size() == 0) {
					setNewEntryInList(parameters, ctx, keyName, keyValue, prefixKey, valuePrefixKey, listLength);
				} else {
					LOG.error("there was no length for the list parameter set in context memory "
							+ "but " + map.size() + " entries were found in context memory "
							+ "where the key begins with: " + prefixKey);

					return "failure";
				}
			} else if (ctxListContains(containParams, ctx) == "false") {
				setNewEntryInList(parameters, ctx, keyName, keyValue, prefixKey, valuePrefixKey, listLength);
			} else if (ctxListContains(containParams, ctx) == "true") {
				// else update the context memory with the properties passed in at the right index level
				String listPrefix = prefixKey + "[";
				String listSuffix = "].";

				for (int i = 0; i < listLength; i++) {
					String listRootWithIndex = listPrefix + i + listSuffix;
					String listKeyName = listRootWithIndex + keyName;
					String valueAtListIndexKey = ctx.getAttribute(listKeyName);

					if (valueAtListIndexKey.equals(keyValue)) {
						setParametersToCtxList(parameters, ctx, listRootWithIndex, valuePrefixKey);
					}
				}
			}
		} catch (SvcLogicException e) {
			LOG.error("Call to ctxListContains failed: " + e.getMessage());

			return "failure";
		}

		return "success";
	}

	private static void setNewEntryInList(Map<String, String> parameters, SvcLogicContext ctx, String keyName,
										  String keyValue, String prefixKey, String valuePrefixKey, int listLength) {
		String prefixKeyWithIndex = prefixKey + "[" + listLength + "].";
		String listKeyName = prefixKeyWithIndex + keyName;

		// set list key
		LOG.debug("Setting context memory, new list entry with key:  " + listKeyName + " = " + keyValue);
		ctxSetAttribute(ctx, listKeyName, keyValue);

		// set the other parameters
		setParametersToCtxList(parameters, ctx, prefixKeyWithIndex, valuePrefixKey);

		// set length of list
		String ListLengthKeyName = prefixKey + LENGTH;

		ctxSetAttribute(ctx, prefixKey + LENGTH, listLength + 1);
		LOG.debug("Updated _length: " + prefixKey + "_length is now " + ctx.getAttribute(ListLengthKeyName));
	}

	/**
	 * helper function to set the parameter properties for list at the provided prefix key
	 *
	 * @param parameters
	 * @param ctx
	 * @param prefixKey
	 * @return true if any new context memory was added and or modified
	 */
	private static boolean setParametersToCtxList(Map<String, String> parameters, SvcLogicContext ctx, String prefixKeyWithIndex,
												  String valuePrefixKey) {
		boolean changeFlag = false;

		for (Map.Entry<String, String> entry : parameters.entrySet()) {
			if (! (entry.getKey().equals("prefixKey") ||
					entry.getKey().equals("keyName") ||
					entry.getKey().equals("keyValue")) ||
					entry.getKey().equals("valuePrefixKey")) {

				String keyToBeSet = prefixKeyWithIndex + entry.getKey();
				String valueToBeSet = "";

				if (StringUtils.isEmpty(valuePrefixKey)) {
					valueToBeSet = entry.getValue();
				} else {
					valueToBeSet = ctx.getAttribute(valuePrefixKey + "." + entry.getValue());
				}

				LOG.debug("Setting context memory: " + keyToBeSet + " = " + valueToBeSet);

				// only set context memory if properties key and value are not empty or null
				if (!StringUtils.isEmpty(entry.getKey()) && !StringUtils.isEmpty(valueToBeSet)) {
					ctxSetAttribute(ctx, keyToBeSet, valueToBeSet);
					changeFlag = true;
				}
			}
		}

		return changeFlag;
	}

    public static String containsKey(Map<String, String> parameters, SvcLogicContext ctx) throws SvcLogicException {
        String key = parameters.get("key");
        Boolean keyFound = ctx.getAttributeKeySet().contains(key);
        if (keyFound) {
            return "true";
        }
        return "false";
    }

}

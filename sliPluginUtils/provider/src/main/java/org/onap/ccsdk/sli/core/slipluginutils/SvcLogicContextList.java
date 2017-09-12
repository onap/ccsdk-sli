/*-
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 						reserved.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

/**
 * A utility class used to manage list manipulation in the context memory.
 * @see org.onap.ccsdk.sli.core.sli.SvcLogicContext
 */
public class SvcLogicContextList {
    /**
     * Internal flag indicating if list should be deleted from context memory
     * when it is copied into the SvcLogicContextList object.
     */
    private enum OperType {
        COPY, EXTRACT
    }

    // TODO: javadoc
    protected final String prefix;
    // TODO: javadoc
    protected final ArrayList<HashMap<String,String>> list;


    // TODO: javadoc
    public SvcLogicContextList( SvcLogicContext ctx, String list_prefix ) {
        this(ctx, list_prefix, OperType.COPY);
    }

    // TODO: javadoc
    private SvcLogicContextList( SvcLogicContext ctx, String list_prefix, OperType operation ) {
        this.prefix = list_prefix;

        // Initialize list
        int capacity = getCtxListLength(ctx, prefix);
        this.list = new ArrayList<>(capacity);
        for( int i = 0; i < capacity; i++ ) {
            this.list.add(i, new HashMap<String,String>());
        }

        // Populate "elements" in list
        String prefix_bracket = this.prefix + '[';
        for (String key : new HashSet<String>(ctx.getAttributeKeySet())) {
            if( key.startsWith(prefix_bracket) ) {
                // Extract the index of the list
                int index = getCtxListIndex(key, this.prefix, capacity);

                // Store the
                String suffix = key.substring((prefix_bracket + index + ']').length());
                suffix = suffix.isEmpty() ? suffix : suffix.substring(1);
                this.list.get(index).put( suffix, ctx.getAttribute(key));

                // If flag to extract set, remove data from context memory as
                // it is read into this list
                if( operation == OperType.EXTRACT ) {
                    ctx.setAttribute(key, null);
                }
            }
        }

        // If flag to extract set, remove list _length value from cxt mem
        if( operation == OperType.EXTRACT ) {
            ctx.setAttribute(this.prefix + "_length", null);
        }
    }

    // TODO: javadoc
    public static SvcLogicContextList extract( SvcLogicContext ctx, String list_prefix ) {
        return new SvcLogicContextList(ctx, list_prefix, OperType.EXTRACT);
    }


    // ========== PUBLIC FUNCTIONS ==========

    // TODO: javadoc
    public HashMap<String,String> get( int index ) {
        return this.list.get(index);
    }

    // TODO: javadoc
    public HashMap<String,String> remove( int index ) {
        return this.list.remove(index);
    }

    // TODO: javadoc
    public void remove( String value ) {
        remove( "", value );
    }

    // TODO: javadoc
    public void remove( String key, String value ) {
        if( value == null ) {
            throw new IllegalArgumentException("value cannot be null");
        }

        ListIterator<HashMap<String,String>> itr = this.list.listIterator();
        while( itr.hasNext() ) {
            if( value.equals(itr.next().get(key)) ) {
                itr.remove();
            }
        }
    }

    // TODO javadoc
    public void remove( Map<String,String> primary_key ) {
        ListIterator<HashMap<String,String>> itr = this.list.listIterator();
        while( itr.hasNext() ) {
            boolean found = true;
            HashMap<String,String> list_element = itr.next();
            for( Map.Entry<String,String> key : primary_key.entrySet() ) {
                if( !key.getValue().equals(list_element.get(key.getKey())) ) {
                    found = false;
                    break;
                }
            }

            if( found ) {
                itr.remove();
            }
        }
    }

    // TODO: javadoc
    public int size() {
        return list.size();
    }

    // TODO: javadoc
    public void writeToContext( SvcLogicContext ctx ) {
        ctx.setAttribute( prefix + "_length", Integer.toString(this.list.size()) );

        for( int i = 0; i < this.list.size(); i++ ) {
            for( Map.Entry<String,String> entry : this.list.get(i).entrySet() ) {
                if("".equals(entry.getKey())) {
                    ctx.setAttribute(prefix + '[' + i + ']', entry.getValue());
                } else {
                    ctx.setAttribute(prefix + '[' + i + "]." + entry.getKey(), entry.getValue());
                }
            }
        }
    }



    // ========== PRIVATE STATIC FUNCTIONS ==========

    // TODO: javadoc
    private static int getCtxListIndex( String key, String prefix, int list_size ) {
        int index = getCtxListIndex( key, prefix );
        if( index >= list_size ) {
            throw new IllegalArgumentException("Context memory list \"" + prefix + "[]\" contains an index >= the size of the list", new ArrayIndexOutOfBoundsException("index \"" + index + "\" is outside the bounds of the context memory list \"" + prefix + "[]. List Length = " + list_size));
        } else if (index < 0) {
            throw new IllegalArgumentException("Context memory list \"" + prefix + "[]\" contains a negative index", new NegativeArraySizeException("index \"" + index + "\" of context memory list is negative"));
        }

        return index;
    }

    // TODO: javadoc
    private static int getCtxListIndex( String key, String prefix ) {
        String ctx_index_str = StringUtils.substringBetween(key.substring(prefix.length()), "[", "]");
        try {
            return Integer.parseInt( ctx_index_str );
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Could not parse index value \"" + ctx_index_str + "\" in context memory key \"" + key + "\"", e);
        }
    }

    // TODO: javadoc
    private static int getCtxListLength( SvcLogicContext ctx, String prefix ) {
        String _length_key = prefix + "_length";
        String _length_val_str = ctx.getAttribute(_length_key);
        try {
            return Integer.parseInt(_length_val_str);
        } catch (NumberFormatException e) {
            if( _length_val_str == null ) {
                throw new IllegalStateException( "Could not find list length \"" + _length_key + "\" in context memory." );
            } else {
                throw new IllegalStateException( "Could not parse index value \"" + _length_val_str + "\" of context memory list length \"" + _length_key + "\"" , e );
            }
        }
    }
}

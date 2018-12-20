/*-
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 *                         reserved.
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

package org.onap.ccsdk.sli.core.sli;

import java.io.PrintStream;
import java.io.FileDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Arrays;
import java.util.ArrayList;
import java.io.*;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpPrefixBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Strings;


public class PrintYangToProp {

    private static final Logger LOG = LoggerFactory.getLogger(PrintYangToProp.class);
    public static final String PROPERTIES_FILE="";
    private static Properties properties;
    private static final String BUILDER="-builder";
    private static final String IMPL="-impl";
    private static final String TO_PROPERTIES_STRING="() to Properties entry";
    private static final String CAUGHT_EXCEPTION_MSG="Caught exception trying to convert value returned by ";
    public static Properties prop = new Properties();
    public static ArrayList<String> propList = new ArrayList<>();

    
    public static Properties toProperties(Properties props, Object fromObj) {
        Class fromClass = null;
        
        if (fromObj != null)
        {
            fromClass = fromObj.getClass();
        }
        return (toProperties(props, "", fromObj, fromClass));
    }
    
    public static Properties toProperties(Properties props, String pfx, Object fromObj)
    {
        Class fromClass = null;
        
        if (fromObj != null)
        {
            fromClass = fromObj.getClass();
        }
        
        return(toProperties(props, pfx, fromObj, fromClass));
    }

    public static Properties toProperties(Properties props, String pfx,
            Object fromObj, Class fromClass) {

        if (fromObj == null) {
            return (props);
        }
    
        
        String simpleName = fromClass.getSimpleName();

        if (fromObj instanceof List) {


            List fromList = (List) fromObj;

            for (int i = 0; i < fromList.size(); i++) {
                toProperties(props, pfx + "[" + i + "]", fromList.get(i), fromClass);
            }
            props.setProperty(pfx + "_length", "" + fromList.size());

        } else if (isYangGenerated(fromClass)) {

            String propNamePfx = null;

            // If called from a list (so prefix ends in ']'), don't
            // add class name again
            if (pfx.endsWith("]")) {
                propNamePfx = pfx;
            } else {
                if ((pfx != null) && (pfx.length() > 0)) {
                    propNamePfx = pfx ;
                } else {
                    propNamePfx = toLowerHyphen(fromClass.getSimpleName());
                }

                if (propNamePfx.endsWith(BUILDER)) {
                    propNamePfx = propNamePfx.substring(0, propNamePfx.length()
                            - BUILDER.length());
                }

                if (propNamePfx.endsWith(IMPL)) {
                    propNamePfx = propNamePfx.substring(0, propNamePfx.length()
                            - IMPL.length());
                }
            }
            
            // Iterate through getter methods to figure out values we need to
            // save from

            for (Method m : fromClass.getMethods()) {

                if (isGetter(m)) {

                    Class returnType = m.getReturnType();
                    String fieldName = toLowerHyphen(m.getName().substring(3));
                    if(m != null && m.getName().matches("^is[A-Z].*")){
                        fieldName = toLowerHyphen(m.getName().substring(2));
                    }

                if(Strings.isNullOrEmpty(fieldName)) fieldName = fieldName.substring(0, 1).toLowerCase()+ fieldName.substring(1);
                            

                    // Is the return type a yang generated class?
                    if (isYangGenerated(returnType)) {

                        // Is it an enum?
                        if (returnType.isEnum()) {
                            // Return type is a typedef. Save its value.
                            try {
                                boolean isAccessible = m.isAccessible();
                                if (!isAccessible) {
                                    m.setAccessible(true);
                                }

                                Object retValue = m.invoke(fromObj);

                                if (!isAccessible) {
                                    m.setAccessible(isAccessible);
                                }
                                if (retValue != null) {
                                    String propName = propNamePfx + "."
                                            + fieldName;
                                    String propVal = retValue.toString();
                                    String yangProp = "yang." + fieldName + "." + propVal;
                                    if ( properties.containsKey(yangProp)) {
                                        propVal = properties.getProperty(yangProp);
                                    }
                                    props.setProperty(propName, propVal);
                                }
                            } catch (Exception e) {
                                LOG.error(
                                        "Caught exception trying to convert Yang-generated enum returned by "
                                                + fromClass.getName() + "."
                                                + m.getName()
                                                + TO_PROPERTIES_STRING, e);
                            }
                        } else if (isIpv4Address(returnType)) {
                            // Save its value
                            try {
                                String propName = propNamePfx + "." + fieldName;
                                boolean isAccessible = m.isAccessible();
                                if (!isAccessible) {
                                    m.setAccessible(true);
                                }
                                Ipv4Address retValue = (Ipv4Address) m.invoke(fromObj);
                                if (!isAccessible) {
                                    m.setAccessible(isAccessible);
                                }

                                if (retValue != null) {
                                    String propVal = retValue.getValue();
                                    
                                    props.setProperty(propName, propVal);

                                }
                            } catch (Exception e) {
                                LOG.error(
                                        CAUGHT_EXCEPTION_MSG
                                                + fromClass.getName() + "."
                                                + m.getName()
                                                + TO_PROPERTIES_STRING, e);
                            }
                        } else if (isIpv6Address(returnType)) {
                            // Save its value
                            try {
                                String propName = propNamePfx + "." + fieldName;
                                boolean isAccessible = m.isAccessible();
                                if (!isAccessible) {
                                    m.setAccessible(true);
                                }
                                Ipv6Address retValue = (Ipv6Address) m.invoke(fromObj);
                                if (!isAccessible) {
                                    m.setAccessible(isAccessible);
                                }

                                if (retValue != null) {
                                    String propVal = retValue.getValue();
                                    
                                    props.setProperty(propName, propVal);

                                }
                            } catch (Exception e) {
                                LOG.error(
                                        CAUGHT_EXCEPTION_MSG
                                                + fromClass.getName() + "."
                                                + m.getName()
                                                + TO_PROPERTIES_STRING, e);
                            }
                        } else if (isIpv4Prefix(returnType)) {
                            
                            // Save its value
                            try {
                                String propName = propNamePfx + "." + fieldName;
                                boolean isAccessible = m.isAccessible();
                                if (!isAccessible) {
                                    m.setAccessible(true);
                                }
                                Ipv4Prefix retValue = (Ipv4Prefix) m.invoke(fromObj);
                                if (!isAccessible) {
                                    m.setAccessible(isAccessible);
                                }

                                if (retValue != null) {
                                    String propVal = retValue.getValue();
                                    
                                    props.setProperty(propName, propVal);

                                }
                            } catch (Exception e) {
                                LOG.error(
                                        CAUGHT_EXCEPTION_MSG
                                                + fromClass.getName() + "."
                                                + m.getName()
                                                + TO_PROPERTIES_STRING, e);
                            }
                        } else if (isIpv6Prefix(returnType)) {
                            //System.out.println("isIpv6Prefix");
                            // Save its value
                            try {
                                String propName = propNamePfx + "." + fieldName;
                                boolean isAccessible = m.isAccessible();
                                if (!isAccessible) {
                                    m.setAccessible(true);
                                }
                                Ipv6Prefix retValue = (Ipv6Prefix) m.invoke(fromObj);
                                if (!isAccessible) {
                                    m.setAccessible(isAccessible);
                                }

                                if (retValue != null) {
                                    String propVal = retValue.getValue().toString();
                                    //LOG.debug("Setting property " + propName
                                    //        + " to " + propVal);
                                    props.setProperty(propName, propVal);

                                }
                            } catch (Exception e) {
                                LOG.error(
                                        CAUGHT_EXCEPTION_MSG
                                                + fromClass.getName() + "."
                                                + m.getName()
                                                + TO_PROPERTIES_STRING, e);
                            }
                        } else {
                            try {
                                boolean isAccessible = m.isAccessible();
                                if (!isAccessible) {
                                    m.setAccessible(true);
                                }
                                Object retValue = m.invoke(fromObj);
                                if (!isAccessible) {
                                    m.setAccessible(isAccessible);
                                }
                                if (retValue != null) {
                                    toProperties(props, propNamePfx + "." + fieldName, retValue, returnType);
                                }
                            } catch (Exception e) {
                                LOG.error(
                                        "Caught exception trying to convert Yang-generated class returned by"
                                                + fromClass.getName() + "."
                                                + m.getName()
                                                + TO_PROPERTIES_STRING, e);
                            }
                        }
                    } else if (returnType.equals(Class.class)) {

                        //LOG.debug(m.getName()
                        //        + " returns a Class object - not interested");

                    } else if (List.class.isAssignableFrom(returnType)) {

                        // This getter method returns a list.
                        try {
                            boolean isAccessible = m.isAccessible();
                            if (!isAccessible) {
                                m.setAccessible(true);
                            }
                            Object retList = m.invoke(fromObj);
                            if (!isAccessible) {
                                m.setAccessible(isAccessible);
                            }
                            // Figure out what type of elements are stored in this array.
                            Type paramType = m.getGenericReturnType();
                            Type elementType = ((ParameterizedType) paramType)
                                    .getActualTypeArguments()[0];
                            toProperties(props, propNamePfx + "." + fieldName,
                                    retList, (Class)elementType);
                        } catch (Exception e) {
                            LOG.error(
                                    "Caught exception trying to convert List returned by "
                                            + fromClass.getName() + "."
                                            + m.getName()
                                            + TO_PROPERTIES_STRING, e);
                        }

                    } else {

                        // Method returns something that is not a List and not
                        // yang-generated.
                        // Save its value
                        try {
                            String propName = propNamePfx + "." + fieldName;
                            boolean isAccessible = m.isAccessible();
                            if (!isAccessible) {
                                m.setAccessible(true);
                            }
                            Object propValObj = m.invoke(fromObj);
                            if (!isAccessible) {
                                m.setAccessible(isAccessible);
                            }

                            if (propValObj != null) {
                                String propVal = propValObj.toString();
                                //LOG.debug("Setting property " + propName
                                //        + " to " + propVal);
                                props.setProperty(propName, propVal);

                            }
                        } catch (Exception e) {
                            LOG.error(
                                    CAUGHT_EXCEPTION_MSG
                                            + fromClass.getName() + "."
                                            + m.getName()
                                            + TO_PROPERTIES_STRING, e);
                        }
                    }

                }
            }

        } else {
            // Class is not yang generated and not a list
            // Do nothing.

        }

        return (props);
    }

    public static Object toBuilder(Properties props, Object toObj) {

        return (toBuilder(props, "", toObj));
    }

    public static List toList(Properties props, String pfx, List toObj,
            Class elemType) {

        int maxIdx = -1;
        boolean foundValue = false;

        //LOG.debug("Saving properties to List<" + elemType.getName()
        //        + ">  from " + pfx);

        // Figure out array size
        for (Object pNameObj : props.keySet()) {
            String key = (String) pNameObj;

            if (key.startsWith(pfx + "[")) {
                String idxStr = key.substring(pfx.length() + 1);
                int endloc = idxStr.indexOf("]");
                if (endloc != -1) {
                    idxStr = idxStr.substring(0, endloc);
                }

                try {
                    int curIdx = Integer.parseInt(idxStr);
                    if (curIdx > maxIdx) {
                        maxIdx = curIdx;
                    }
                } catch (Exception e) {
                    LOG.error("Illegal subscript in property " + key);
                }

            }
        }

        //LOG.debug(pfx + " has max index of " + maxIdx);
        for (int i = 0; i <= maxIdx; i++) {

            String curBase = pfx + "[" + i + "]";

            if (isYangGenerated(elemType)) {
                String builderName = elemType.getName() + "Builder";
                try {
                    Class builderClass = Class.forName(builderName);
                    Object builderObj = builderClass.newInstance();
                    Method buildMethod = builderClass.getMethod("build");
                    builderObj = toBuilder(props, curBase, builderObj, true);
                    if (builderObj != null) {
                        //LOG.debug("Calling " + builderObj.getClass().getName()
                        //        + "." + buildMethod.getName() + "()");
                        Object builtObj = buildMethod.invoke(builderObj);
                        toObj.add(builtObj);
                        foundValue = true;
                    }

                } catch (ClassNotFoundException e) {
                    LOG.warn("Could not find builder class " + builderName, e);
                } catch (Exception e) {
                    LOG.error("Caught exception trying to populate list from "
                            + pfx);
                }
            }

        }

        if (foundValue) {
            return (toObj);
        } else {
            return (null);
        }

    }
    
    public static Object toBuilder(Properties props, String pfx, Object toObj) {
        return(toBuilder(props, pfx, toObj, false));
    }

    public static Object toBuilder(Properties props, String pfx, Object toObj, boolean preservePfx) {
        Class toClass = toObj.getClass();
        boolean foundValue = false;

        //LOG.debug("Saving properties to " + toClass.getName() + " class from "
        //        + pfx);

        Ipv4Address addr;

        if (isYangGenerated(toClass)) {
            // Class is yang generated.
            //LOG.debug(toClass.getName() + " is a Yang-generated class");

            String propNamePfx = null;
            if (preservePfx) {
                propNamePfx = pfx;
            } else {

                if ((pfx != null) && (pfx.length() > 0)) {
                    propNamePfx = pfx + "."
                            + toLowerHyphen(toClass.getSimpleName());
                } else {
                    propNamePfx = toLowerHyphen(toClass.getSimpleName());
                }

                if (propNamePfx.endsWith(BUILDER)) {
                    propNamePfx = propNamePfx.substring(0, propNamePfx.length()
                            - BUILDER.length());
                }

                if (propNamePfx.endsWith(IMPL)) {
                    propNamePfx = propNamePfx.substring(0, propNamePfx.length()
                            - IMPL.length());
                }
            }

            if (toObj instanceof Identifier) {
                //LOG.debug(toClass.getName() + " is a Key - skipping");
                return (toObj);
            }

            // Iterate through getter methods to figure out values we need to
            // set

            for (Method m : toClass.getMethods()) {
                // LOG.debug("Is " + m.getName() + " method a setter?");
                if (isSetter(m)) {
                    // LOG.debug(m.getName() + " is a setter");
                    Class paramTypes[] = m.getParameterTypes();
                    Class paramClass = paramTypes[0];

                    String fieldName = toLowerHyphen(m.getName().substring(3));
                    fieldName = fieldName.substring(0, 1).toLowerCase()
                            + fieldName.substring(1);

                    String propName = propNamePfx + "." + fieldName;

                    String paramValue = props.getProperty(propName);
                    if (paramValue == null) {
                        //LOG.debug(propName + " is unset");
                    } else {
                        //LOG.debug(propName + " = " + paramValue);
                    }

                    // Is the return type a yang generated class?
                    if (isYangGenerated(paramClass)) {
                        // Is it an enum?
                        if (paramClass.isEnum()) {

                            //LOG.debug(m.getName() + " expects an Enum");
                            // Param type is a typedef.
                            if (paramValue != null) {
                                Object paramObj = null;

                                try {
                                    paramObj = Enum.valueOf(paramClass,
                                            toUpperCamelCase(paramValue));
                                } catch (Exception e) {
                                    LOG.error(
                                            "Caught exception trying to convert field "
                                                    + propName + " to enum "
                                                    + paramClass.getName(), e);
                                }

                                try {
                                    boolean isAccessible = m.isAccessible();
                                    if (!isAccessible) {
                                        m.setAccessible(true);
                                    }

                                    //LOG.debug("Calling "
                                    //        + toObj.getClass().getName() + "."
                                    //        + m.getName() + "(" + paramValue
                                    //        + ")");
                                    m.invoke(toObj, paramObj);

                                    if (!isAccessible) {
                                        m.setAccessible(isAccessible);
                                    }
                                    foundValue = true;

                                } catch (Exception e) {
                                    LOG.error(
                                            "Caught exception trying to create Yang-generated enum expected by"
                                                    + toClass.getName()
                                                    + "."
                                                    + m.getName()
                                                    + "() from Properties entry",
                                            e);
                                }
                            }
                        } else {

                            String simpleName = paramClass.getSimpleName();
                        LOG.info("simpleName:" + simpleName);

                            if ("Ipv4Address".equals(simpleName)
                                    || "Ipv6Address".equals(simpleName) || "Ipv4Prefix".equals(simpleName) || "Ipv6Prefix".equals(simpleName)) {
                            
                                if (paramValue != null) {
                            if("Ipv4Address".equals(simpleName) || "Ipv6Address".equals(simpleName)){
                                    try {
                                        IpAddress ipAddr = IpAddressBuilder
                                                .getDefaultInstance(paramValue);


                                        if ("Ipv4Address".equals(simpleName))
                                        {
                                            m.invoke(toObj, ipAddr.getIpv4Address());
                                        }
                                        else
                                        {
                                            m.invoke(toObj, ipAddr.getIpv6Address());

                                        }
                                        foundValue = true;
                                    } catch (Exception e) {
                                        LOG.error(
                                                "Caught exception calling "
                                                        + toClass.getName() + "."
                                                        + m.getName() + "("
                                                        + paramValue + ")", e);

                                    }
                            }else if("Ipv4Prefix".equals(simpleName)|| "Ipv6Prefix".equals(simpleName)){
                                    try {
                                        IpPrefix ipPrefix = IpPrefixBuilder
                                                .getDefaultInstance(paramValue);


                                        if ("Ipv4Prefix".equals(simpleName))
                                        {
                                            m.invoke(toObj, ipPrefix.getIpv4Prefix());
                                        }
                                        else
                                        {
                                            m.invoke(toObj, ipPrefix.getIpv6Prefix());

                                        }
                                        foundValue = true;
                                    } catch (Exception e) {
                                        LOG.error(
                                                "Caught exception calling "
                                                        + toClass.getName() + "."
                                                        + m.getName() + "("
                                                        + paramValue + ")", e);

                                }
                            }
                            }

                            } else {
                                // setter expects a yang-generated class. Need
                                // to
                                // create a builder to set it.

                                String builderName = paramClass.getName()
                                        + "Builder";
                                Class builderClass = null;
                                Object builderObj = null;
                                Object paramObj = null;

                                //LOG.debug(m.getName()
                                //        + " expects a yang-generated class - looking for builder "
                                //        + builderName);
                                try {
                                    builderClass = Class.forName(builderName);
                                    builderObj = builderClass.newInstance();
                                    paramObj = toBuilder(props, propNamePfx,
                                            builderObj);
                                } catch (ClassNotFoundException e) {
                                    Object constObj = null;
                                    try {
                                        // See if I can find a constructor I can
                                        // use
                                        Constructor[] constructors = paramClass
                                                .getConstructors();
                                        // Is there a String constructor?
                                        for (Constructor c : constructors) {
                                            Class[] cParms = c
                                                    .getParameterTypes();
                                            if ((cParms != null)
                                                    && (cParms.length == 1)) {
                                                if (String.class
                                                        .isAssignableFrom(cParms[0])) {
                                                    constObj = c
                                                            .newInstance(paramValue);
                                                }
                                            }
                                        }

                                        if (constObj == null) {
                                            // Is there a Long constructor?
                                            for (Constructor c : constructors) {
                                                Class[] cParms = c
                                                        .getParameterTypes();
                                                if ((cParms != null)
                                                        && (cParms.length == 1)) {
                                                    if (Long.class
                                                            .isAssignableFrom(cParms[0])) {
                                                        constObj = c
                                                                .newInstance(Long
                                                                        .parseLong(paramValue));
                                                    }
                                                }
                                            }

                                        }

                                        if (constObj != null) {
                                            try {
                                                m.invoke(toObj, constObj);
                                                foundValue = true;
                                            } catch (Exception e2) {
                                                LOG.error(
                                                        "Caught exception trying to call "
                                                                + m.getName(),
                                                        e2);
                                            }
                                        }
                                    } catch (Exception e1) {
                                        LOG.warn(
                                                "Could not find a suitable constructor for "
                                                        + paramClass.getName(),
                                                e1);
                                    }

                                    if (paramObj == null) {
                                        LOG.warn("Could not find builder class "
                                                + builderName
                                                + " and could not find a String or Long constructor - trying just to set passing paramValue");

                                    }

                                } catch (Exception e) {
                                    LOG.error(
                                            "Caught exception trying to create builder "
                                                    + builderName, e);
                                }

                                if (paramObj != null) {

                                    try {

                                        Method buildMethod = builderClass
                                                .getMethod("build");
                                        //LOG.debug("Calling "
                                        //        + paramObj.getClass().getName()
                                        //        + "." + buildMethod.getName()
                                        //        + "()");
                                        Object builtObj = buildMethod
                                                .invoke(paramObj);

                                        boolean isAccessible = m.isAccessible();
                                        if (!isAccessible) {
                                            m.setAccessible(true);
                                        }

                                        //LOG.debug("Calling "
                                        //        + toObj.getClass().getName()
                                        //        + "." + m.getName() + "()");
                                        m.invoke(toObj, builtObj);
                                        if (!isAccessible) {
                                            m.setAccessible(isAccessible);
                                        }
                                        foundValue = true;

                                    } catch (Exception e) {
                                        LOG.error(
                                                "Caught exception trying to set Yang-generated class expected by"
                                                        + toClass.getName()
                                                        + "."
                                                        + m.getName()
                                                        + "() from Properties entry",
                                                e);
                                    }
                                } else {
                                    try {
                                        boolean isAccessible = m.isAccessible();
                                        if (!isAccessible) {
                                            m.setAccessible(true);
                                        }
                                        //LOG.debug("Calling "
                                        //        + toObj.getClass().getName()
                                        //        + "." + m.getName() + "("
                                        //        + paramValue + ")");
                                        m.invoke(toObj, paramValue);
                                        if (!isAccessible) {
                                            m.setAccessible(isAccessible);
                                        }
                                        foundValue = true;

                                    } catch (Exception e) {
                                        LOG.error(
                                                "Caught exception trying to convert value returned by"
                                                        + toClass.getName()
                                                        + "."
                                                        + m.getName()
                                                        + TO_PROPERTIES_STRING,
                                                e);
                                    }
                                }
                            }
                            }
                        }else {

                        // Setter's argument is not a yang-generated class. See
                        // if it is a List.

                        if (List.class.isAssignableFrom(paramClass)) {

                            //LOG.debug("Parameter class " + paramClass.getName()
                            //        + " is a List");

                            // Figure out what type of args are in List and pass
                            // that to toList().

                            Type paramType = m.getGenericParameterTypes()[0];
                            Type elementType = ((ParameterizedType) paramType)
                                    .getActualTypeArguments()[0];
                            Object paramObj = new LinkedList();
                            try {
                                paramObj = toList(props, propName,
                                        (List) paramObj, (Class) elementType);
                            } catch (Exception e) {
                                LOG.error("Caught exception trying to create list expected as argument to "
                                        + toClass.getName() + "." + m.getName());
                            }

                            if (paramObj != null) {
                                try {
                                    boolean isAccessible = m.isAccessible();
                                    if (!isAccessible) {
                                        m.setAccessible(true);
                                    }
                                    //LOG.debug("Calling "
                                    //        + toObj.getClass().getName() + "."
                                    //        + m.getName() + "(" + paramValue
                                    //        + ")");
                                    m.invoke(toObj, paramObj);
                                    if (!isAccessible) {
                                        m.setAccessible(isAccessible);
                                    }
                                    foundValue = true;

                                } catch (Exception e) {
                                    LOG.error(
                                            "Caught exception trying to convert List returned by"
                                                    + toClass.getName() + "."
                                                    + m.getName()
                                                    + TO_PROPERTIES_STRING,
                                            e);
                                }
                            }
                        } else {

                            // Setter expects something that is not a List and
                            // not yang-generated. Just pass the parameter value

                            //LOG.debug("Parameter class "
                            //        + paramClass.getName()
                            //        + " is not a yang-generated class or a List");

                            if (paramValue != null) {

                                Object constObj = null;

                                try {
                                    // See if I can find a constructor I can use
                                    Constructor[] constructors = paramClass
                                            .getConstructors();
                                    // Is there a String constructor?
                                    for (Constructor c : constructors) {
                                        Class[] cParms = c.getParameterTypes();
                                        if ((cParms != null)
                                                && (cParms.length == 1)) {
                                            if (String.class
                                                    .isAssignableFrom(cParms[0])) {
                                                constObj = c
                                                        .newInstance(paramValue);
                                            }
                                        }
                                    }

                                    if (constObj == null) {
                                        // Is there a Long constructor?
                                        for (Constructor c : constructors) {
                                            Class[] cParms = c
                                                    .getParameterTypes();
                                            if ((cParms != null)
                                                    && (cParms.length == 1)) {
                                                if (Long.class
                                                        .isAssignableFrom(cParms[0])) {
                                                    constObj = c
                                                            .newInstance(Long
                                                                    .parseLong(paramValue));
                                                }
                                            }
                                        }

                                    }

                                    if (constObj != null) {
                                        try {
                                            //LOG.debug("Calling "
                                            //        + toObj.getClass()
                                            //                .getName() + "."
                                            //        + m.getName() + "("
                                            //        + constObj + ")");
                                            m.invoke(toObj, constObj);
                                            foundValue = true;
                                        } catch (Exception e2) {
                                            LOG.error(
                                                    "Caught exception trying to call "
                                                            + m.getName(), e2);
                                        }
                                    } else {
                                        try {
                                            boolean isAccessible = m
                                                    .isAccessible();
                                            if (!isAccessible) {
                                                m.setAccessible(true);
                                            }
                                            //LOG.debug("Calling "
                                            //        + toObj.getClass()
                                            //                .getName() + "."
                                            //        + m.getName() + "("
                                            //        + paramValue + ")");
                                            m.invoke(toObj, paramValue);
                                            if (!isAccessible) {
                                                m.setAccessible(isAccessible);
                                            }
                                            foundValue = true;

                                        } catch (Exception e) {
                                            LOG.error(
                                                    "Caught exception trying to convert value returned by"
                                                            + toClass.getName()
                                                            + "."
                                                            + m.getName()
                                                            + TO_PROPERTIES_STRING,
                                                    e);
                                        }
                                    }
                                } catch (Exception e1) {
                                    LOG.warn(
                                            "Could not find a suitable constructor for "
                                                    + paramClass.getName(), e1);
                                }

                                /*
                                 * try { boolean isAccessible =
                                 * m.isAccessible(); if (!isAccessible) {
                                 * m.setAccessible(true); } LOG.debug("Calling "
                                 * + toObj.getClass().getName() + "." +
                                 * m.getName() + "(" + paramValue + ")");
                                 * m.invoke(toObj, paramValue); if
                                 * (!isAccessible) {
                                 * m.setAccessible(isAccessible); } foundValue =
                                 * true;
                                 * 
                                 * } catch (Exception e) { LOG.error(
                                 * "Caught exception trying to convert value returned by"
                                 * + toClass.getName() + "." + m.getName() +
                                 * "() to Properties entry", e); }
                                 */
                            }
                        }
                    }
                } // End of section handling "setter" method
            } // End of loop through Methods
        } // End of section handling yang-generated class

        if (foundValue) {
            return (toObj);
        } else {
            return (null);
        }
    }

    public static Properties getProperties(PrintStream pstr, String pfx,
            Class toClass) {
        boolean foundValue = false;

        //LOG.debug("Analyzing " + toClass.getName() + " class : pfx " + pfx);

        if (isYangGenerated(toClass)
                && (!Identifier.class.isAssignableFrom(toClass))) {
            // Class is yang generated.
            //LOG.debug(toClass.getName() + " is a Yang-generated class");

            if (toClass.getName().endsWith("Key")) {
                if (Identifier.class.isAssignableFrom(toClass)) {
                    //LOG.debug(Identifier.class.getName()
                    //        + " is assignable from " + toClass.getName());
                } else {

                    //LOG.debug(Identifier.class.getName()
                    //        + " is NOT assignable from " + toClass.getName());
                }
            }

            String propNamePfx = null;
            if (pfx.endsWith("]")) {
                propNamePfx = pfx;
            }else if(pfx.indexOf(".CLASS_FOUND") != -1){
                pfx = pfx.replace(".CLASS_FOUND","");    
                propNamePfx = pfx + "."
                    + toLowerHyphen(toClass.getSimpleName());
            } else {

                if ((pfx != null) && (pfx.length() > 0)) {
                    propNamePfx = pfx + "."
                            + toLowerHyphen(toClass.getSimpleName());
                } else {
                    propNamePfx = toLowerHyphen(toClass.getSimpleName());
                }

                if (propNamePfx.endsWith(BUILDER)) {
                    propNamePfx = propNamePfx.substring(0, propNamePfx.length()
                            - BUILDER.length());
                }

                if (propNamePfx.endsWith(IMPL)) {
                    propNamePfx = propNamePfx.substring(0, propNamePfx.length()
                            - IMPL.length());
                }
            }

            // Iterate through getter methods to figure out values we need to
            // set

            for (Method m : toClass.getMethods()) {
                //LOG.debug("Is " + m.getName() + " method a getter?");
                if (isGetter(m)) {
                //    LOG.debug(m.getName() + " is a getter");
                    Class returnClass = m.getReturnType();

                    String fieldName = toLowerHyphen(m.getName().substring(3));
                    if(m != null && m.getName().matches("^is[A-Z].*")){
                        fieldName = toLowerHyphen(m.getName().substring(2));
                    }
                    fieldName = fieldName.substring(0, 1).toLowerCase()
                            + fieldName.substring(1);

                    String propName = propNamePfx + "." + fieldName;
                    //System.out.println("****" + propName);

                    // Is the return type a yang generated class?
                    if (isYangGenerated(returnClass)) {
                        // Is it an enum?
                        if (returnClass.isEnum()) {

                            //LOG.debug(m.getName() + " is an Enum");
                            //pstr.print("\n" + propName);
                            //pstr.print("\n" + propName + ":Enum:" + Arrays.asList(returnClass.getEnumConstants()) + "\n");
                            pstr.print("\"" + propName + ":Enum:" + Arrays.asList(returnClass.getEnumConstants()) + "\",");
                            prop.setProperty(propName,"");
                            propList.add(propName);

                        } else {
                            
                            String simpleName = returnClass.getSimpleName();
                            //System.out.println("simpleName:" + simpleName);
                            
                            if ("Ipv4Address".equals(simpleName) || "Ipv6Address".equals(simpleName) || "IpAddress".equals(simpleName) || "Ipv4Prefix".equals(simpleName) || "Ipv6Prefix".equals(simpleName) || "IpPrefix".equals(simpleName)) {
                                //LOG.debug(m.getName()+" is an "+simpleName);
                                //pstr.print("\n" + propName);
                                //pstr.print("\n" + propName + ":" + simpleName + "\n");
                                pstr.print("\"" + propName + ":" + simpleName + "\",");
                                prop.setProperty(propName,"");
                                propList.add(propName);
                            } else {
                                boolean isString = false;
                                boolean isNumber = false;
                                boolean isBoolean = false;
                                boolean isIdentifier = false;
                                //System.out.println("simpleName:" + simpleName);
                                //System.out.println("propName:" + propName);
                                for(Method mthd : returnClass.getMethods()){
                                    String methodName = mthd.getName();
                                    //System.out.println("methodName:" + methodName);
                                    if(methodName.equals("getValue")){
                                        Class retType = mthd.getReturnType();
                                        //System.out.println("retType:" + retType);
                                        isString = String.class.isAssignableFrom(retType);
                                        isNumber = Number.class.isAssignableFrom(retType);
                                        isBoolean = Boolean.class.isAssignableFrom(retType);
                                        isIdentifier = Identifier.class.isAssignableFrom(retType);
                                        //System.out.println("isString:" + isString);
                                        //System.out.println("isNumber:" + isNumber);
                                        //System.out.println("isNumber:" + isNumber);
                                        break;
                                    }
                                }

                                if(isString){
                                    pstr.print("\"" + propName + ":String\",");
                                    prop.setProperty(propName,"");
                                    propList.add(propName);
                                }else if(isNumber){
                                    pstr.print("\"" + propName + ":Number\",");
                                    prop.setProperty(propName,"");
                                    propList.add(propName);
                                }else if(isBoolean){
                                    pstr.print("\"" + propName + ":Boolean\",");
                                    prop.setProperty(propName,"");
                                    propList.add(propName);
                                }else if(isIdentifier){
                                    //System.out.println("isIdentifier");
                                    //isIdentifer so skipping 
                                    continue;
                                }else{
                                /*
                                System.out.println("fieldName:" + fieldName);
                                System.out.println("simpleName:" + simpleName);
                                System.out.println("returnClass:" + returnClass);
                                System.out.println("pstr:" + pstr);
                                System.out.println("propNamePfx:" + propNamePfx);
                                */
                                getProperties(pstr, propNamePfx + ".CLASS_FOUND", returnClass);
                                }
                            }

                        }
                    } else {

                        // Setter's argument is not a yang-generated class. See
                        // if it is a List.

                        if (List.class.isAssignableFrom(returnClass)) {

                            //LOG.debug("Parameter class "
                            //        + returnClass.getName() + " is a List");

                            // Figure out what type of args are in List and pass
                            // that to toList().

                            Type returnType = m.getGenericReturnType();
                            Type elementType = ((ParameterizedType) returnType)
                                    .getActualTypeArguments()[0];
                            Class elementClass = (Class) elementType;
                            //LOG.debug("Calling printPropertyList on list type ("
                                    //+ elementClass.getName()
                                //    + "), pfx is ("
                                //    + pfx
                                //    + "), toClass is ("
                                //    + toClass.getName() + ")");
                            //System.out.println("List propNamePfx:" + propNamePfx+ "." + toLowerHyphen(elementClass.getSimpleName()) + "[]");
                            if(String.class.isAssignableFrom(elementClass)){
                                pstr.print("\"" + propName + ":[String,String,...]\",");
                                prop.setProperty(propName,"");
                                propList.add(propName);
                            }else if(Number.class.isAssignableFrom(elementClass)){
                                pstr.print("\"" + propName + ":[Number,Number,...]\",");
                                prop.setProperty(propName,"");
                                propList.add(propName);
                            }else if(Boolean.class.isAssignableFrom(elementClass)){
                                pstr.print("\"" + propName + ":[Boolean,Boolean,...]\",");
                                prop.setProperty(propName,"");
                                propList.add(propName);
                            }else if(Identifier.class.isAssignableFrom(elementClass)){
                                continue;
                            }else{
                                getProperties(
                                    pstr,
                                    propNamePfx
                                            + "."
                                            + toLowerHyphen(elementClass
                                                    .getSimpleName()) + "[]",
                                    elementClass);
                            }

                        } else if (!returnClass.equals(Class.class)) {

                            // Setter expects something that is not a List and
                            // not yang-generated. Just pass the parameter value

                            //LOG.debug("Parameter class "
                            //        + returnClass.getName()
                            //        + " is not a yang-generated class or a List");

                            //pstr.print("\n" + propName);
                            String className=returnClass.getName();
                            int nClassNameIndex = className.lastIndexOf('.');
                            String nClassName = className;
                            if(nClassNameIndex != -1){
                                nClassName=className.substring(nClassNameIndex+1);
                            }
                            boolean isString = String.class.isAssignableFrom(returnClass);
                            boolean isNumber = Number.class.isAssignableFrom(returnClass);
                            boolean isBoolean = Boolean.class.isAssignableFrom(returnClass);
                            //pstr.print("\n" + propName +":" + nClassName +"\n");
                            boolean isIdentifier = Identifier.class.isAssignableFrom(returnClass);
                            if(!isIdentifier && !nClassName.equals("[C")){
                                if(isNumber){
                                    pstr.print("\"" + propName +":Number\",");
                                }else if(isBoolean){
                                    pstr.print("\"" + propName +":Boolean\",");
                                }else{
                                    if(nClassName.equals("[B")){
                                        pstr.print("\"" + propName +":Binary\",");
                                    }else{
                                        pstr.print("\"" + propName +":" + nClassName +"\",");
                                    }
                                }
                                prop.setProperty(propName,"");
                                propList.add(propName);
                            }

                        }
                    }
                } // End of section handling "setter" method
            } // End of loop through Methods
        } // End of section handling yang-generated class

        return prop;
    }

    public static boolean isYangGenerated(Class c) {
        if (c == null) {
            return (false);
        } else {
            //System.out.println(c.getName());
            return (c.getName().startsWith("org.opendaylight.yang.gen."));
        }
    }
    
    public static boolean isIpv4Address(Class c) {
        
        if (c == null ) {
            return (false);
        }
        String simpleName = c.getSimpleName();
        return ("Ipv4Address".equals(simpleName)) ;
    }
    
    public static boolean isIpv6Address(Class c) {
        
        if (c == null ) {
            return (false);
        } 
        String simpleName = c.getSimpleName();
        return ("Ipv6Address".equals(simpleName)) ;
    }
    public static boolean isIpv4Prefix(Class c) {
        
        if (c == null ) {
            return (false);
        }
        String simpleName = c.getSimpleName();
        //System.out.println("simpleName:" + simpleName);
        return ("Ipv4Prefix".equals(simpleName)) ;
    }

    public static boolean isIpv6Prefix(Class c) {
        
        if (c == null ) {
            return (false);
        }
        String simpleName = c.getSimpleName();
        //System.out.println("simpleName:" + simpleName);
        return ("Ipv6Prefix".equals(simpleName)) ;
    }

    public static String toLowerHyphen(String inStr) {
        if (inStr == null) {
            return (null);
        }

        String str = inStr.substring(0, 1).toLowerCase();
        if (inStr.length() > 1) {
            str = str + inStr.substring(1);
        }

        String regex = "(([a-z0-9])([A-Z]))";
        String replacement = "$2-$3";

        String retval = str.replaceAll(regex, replacement).toLowerCase();

        //LOG.debug("Converting " + inStr + " => " + str + " => " + retval);
        return (retval);
    }

    public static String toUpperCamelCase(String inStr) {
        if (inStr == null) {
            return (null);
        }

        String[] terms = inStr.split("-");
        StringBuffer sbuff = new StringBuffer();
        // Check if string begins with a digit
        if (Character.isDigit(inStr.charAt(0))) {
            sbuff.append('_');
        }
        for (String term : terms) {
            sbuff.append(term.substring(0, 1).toUpperCase());
            if (term.length() > 1) {
                sbuff.append(term.substring(1));
            }
        }
        return (sbuff.toString());

    }

    public static boolean isGetter(Method m) {
        //System.out.println(m);
        if (m == null) {
            return (false);
        }

        if (Modifier.isPublic(m.getModifiers())
                && (m.getParameterTypes().length == 0)) {
            if ((m.getName().matches("^is[A-Z].*") || m.getName().matches("^get[A-Z].*"))
                    && m.getReturnType().equals(Boolean.class)) {
                return (true);
            }
            if (m.getName().matches("^get[A-Z].*")
                    && !m.getReturnType().equals(void.class)) {
                return (true);
            }

        }

        return (false);
    }

    public static boolean isSetter(Method m) {
        if (m == null) {
            return (false);
        }

        if (Modifier.isPublic(m.getModifiers())
                && (m.getParameterTypes().length == 1)) {
            if (m.getName().matches("^set[A-Z].*")) {
                Class[] paramTypes = m.getParameterTypes();
                if (paramTypes[0].isAssignableFrom(Identifier.class)
                        || Identifier.class.isAssignableFrom(paramTypes[0])) {
                    return (false);
                } else {
                    return (true);
                }
            }

        }

        return (false);
    }

    public static void main(String[] args){
        
               try(PrintStream ps = new PrintStream(new FileOutputStream(FileDescriptor.out))){
            PrintYangToProp printYangToProp = new PrintYangToProp();
            String className = args[0];
            //ClassLoader classLoader = PrintYangToProp.class.getClassLoader();
            //Class aClass = classLoader.loadClass(className);
            Class cl = Class.forName(className);
            //printPropertyList(ps,"",cl);
            //JsonObject jsonObj = Json.createObjectBuilder().build();
            Properties p = getProperties(ps,"",cl);    
            //System.out.println(p);

        }catch(Exception e){
            e.printStackTrace();
        }
    }


}

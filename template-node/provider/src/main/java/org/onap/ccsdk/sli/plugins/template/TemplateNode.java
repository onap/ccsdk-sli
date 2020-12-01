/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 			reserved.
 * Modifications Copyright © 2018 IBM.
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

package org.onap.ccsdk.sli.plugins.template;

import java.io.FileInputStream;
import java.io.StringWriter;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TemplateNode implements SvcLogicJavaPlugin {
    private static final Logger logger = LoggerFactory.getLogger(TemplateNode.class);
    public static final String TEMPLATE_PATH = "templatePath";
    public static final String OUTPUT_PATH_KEY = "output";
    public static final String PREFIX_KEY = "prefix";
    public static final String REQUIRED_PARAMETERS_ERROR_MESSAGE = "templateName & outputPath are required fields";
    protected static final String TEMPLATE_PROPERTIES_FILE_NAME = "template-node.properties";
    protected static final String DEFAULT_PROPERTIES_DIR = "/opt/onap/ccsdk/data/properties";
    protected static final String PROPERTIES_DIR_KEY = "SDNC_CONFIG_DIR";

    protected VelocityEngine ve;

    public TemplateNode() {
        ve = new VelocityEngine();
        setProperties();
        ve.init();
    }

    protected void setProperties() {
        String configDir = System.getProperty(PROPERTIES_DIR_KEY, DEFAULT_PROPERTIES_DIR);
        Properties props = new Properties();

        try (FileInputStream in = new FileInputStream(configDir + "/" + TEMPLATE_PROPERTIES_FILE_NAME)) {
            props.load(in);
        } catch (Exception e) {
            logger.warn("Properties not loaded for template node, using sensible defaults. " + e.getMessage());
        }

        // give sensible defaults for required properties
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, props.getProperty("velocity.resource.loader", "file"));
        ve.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH,
                props.getProperty("velocity.file.resource.loader.path", "/opt/onap/sdnc/restapi/templates"));
        ve.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_CACHE,
                props.getProperty("velocity.file.resource.loader.cache", "false"));

        // allow flexible reading of additional velocity properties
        for (String propertyName : props.stringPropertyNames()) {
            if (propertyName.startsWith("velocity")) {
                logger.error("set " + propertyName.substring(9) + "=" + props.get(propertyName));
                ve.setProperty(propertyName.substring(9), props.get(propertyName));
            }
        }
    }

    public void evaluateTemplate(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
        String templateName = params.get(TEMPLATE_PATH);
        String outputPath = params.get(OUTPUT_PATH_KEY);
        String prefix = params.get(PREFIX_KEY);

        if (prefix != null && prefix.length() > 0) {
            outputPath = prefix + "." + outputPath;
        }

        if (templateName == null || outputPath == null) {
            throw new SvcLogicException(REQUIRED_PARAMETERS_ERROR_MESSAGE);
        } else {
            try {
                Template template = ve.getTemplate(templateName);
                VelocityContext context = new VelocityContext();
                context.put("ctx", ctx);
                context.put("params", params);
                //Adding these values directly to context makes working with the values cleaner
                for (Entry<String, String> entry : params.entrySet()) {
                    context.put(entry.getKey(), entry.getValue());
                }
                StringWriter sw = new StringWriter();
                template.merge(context, sw);
                ctx.setAttribute(outputPath, sw.toString());
            } catch (Exception e) {
                throw new SvcLogicException(e.getMessage());
            }
        }
    }

}

/*-
 * ============LICENSE_START=======================================================
 * ONAP - CCSDK
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.ccsdk.sli.core.sliapi.springboot.core;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.onap.ccsdk.sli.core.sli.ConfigurationException;
import org.onap.ccsdk.sli.core.sli.SvcLogicLoader;
import org.onap.ccsdk.sli.core.sli.SvcLogicStore;
import org.onap.ccsdk.sli.core.sli.SvcLogicStoreFactory;
import org.onap.ccsdk.sli.core.sli.provider.base.HashMapResolver;
import org.onap.ccsdk.sli.core.sli.provider.base.SvcLogicPropertiesProvider;
import org.onap.ccsdk.sli.core.sli.provider.base.SvcLogicResolver;
import org.onap.ccsdk.sli.core.sli.provider.base.SvcLogicServiceBase;
import org.onap.ccsdk.sli.core.sli.provider.base.SvcLogicServiceImplBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SvcLogicFactory {
  private static final Logger log = LoggerFactory.getLogger(SvcLogicFactory.class);

  @Bean
  public SvcLogicStore getStore() throws Exception {
    SvcLogicPropertiesProvider propProvider = new SvcLogicPropertiesProvider() {

      @Override
      public Properties getProperties() {
        Properties props = new Properties();
        String propPath = "src/main/resources/svclogic.properties";
        System.out.println(propPath);
        try (FileInputStream fileInputStream = new FileInputStream(propPath)) {
          props = new Properties();
          props.load(fileInputStream);
        } catch (final IOException e) {
          log.error("Failed to load properties for file: {}", propPath,
              new ConfigurationException("Failed to load properties for file: " + propPath, e));
        }
        return props;
      }
    };
    SvcLogicStore store = SvcLogicStoreFactory.getSvcLogicStore(propProvider.getProperties());
    return store;
  }

  @Bean
  public SvcLogicLoader createLoader() throws Exception {
    String serviceLogicDirectory = System.getProperty("serviceLogicDirectory");
    if (serviceLogicDirectory == null) {
      serviceLogicDirectory = "src/main/resources";
    }

    System.out.println("serviceLogicDirectory is " + serviceLogicDirectory);
    SvcLogicLoader loader = new SvcLogicLoader(serviceLogicDirectory, getStore());

    try {
      loader.loadAndActivate();
    } catch (IOException e) {
      log.error("Cannot load directed graphs", e);
    }
    return loader;
  }

  @Bean
  public SvcLogicServiceBase createService() throws Exception {
    SvcLogicResolver resolver = new HashMapResolver();
    return new SvcLogicServiceImplBase(getStore(), resolver);
  }

}

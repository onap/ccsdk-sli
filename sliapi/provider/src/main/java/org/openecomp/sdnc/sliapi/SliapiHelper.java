/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
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

package org.openecomp.sdnc.sliapi;

import org.opendaylight.yang.gen.v1.org.openecomp.sdnc.sliapi.rev161110.ExecuteGraphInputBuilder;
import org.opendaylight.yang.gen.v1.org.openecomp.sdnc.sliapi.rev161110.execute.graph.input.SliParameterBuilder;
import org.openecomp.sdnc.sli.provider.MdsalHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SliapiHelper extends MdsalHelper {

	private static final Logger LOG = LoggerFactory.getLogger(SliapiHelper.class);

	static {
		ExecuteGraphInputBuilder b1 = new ExecuteGraphInputBuilder();
		SliParameterBuilder b2 = new SliParameterBuilder();

	}

}

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

package org.onap.ccsdk.sli.core.sli.recording;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import org.onap.ccsdk.sli.core.sli.ConfigurationException;
import org.onap.ccsdk.sli.core.utils.PathValidator;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicRecorder;


public class FileRecorder implements SvcLogicRecorder {

	@Override
	public void record(Map<String, String> parmMap) throws SvcLogicException {
		
		String fileName = parmMap.get("file");
		if (fileName == null)
		{
			throw new ConfigurationException("No file parameter specified");
		}

		if (!PathValidator.isValidFilePath(fileName)) {
			throw new ConfigurationException("Invalid file name ("+fileName+")");
		}
		
		String record = parmMap.get("record");
		if (record == null)
		{
			String delimiter = parmMap.get("delimiter");
			if (delimiter == null)
			{
				delimiter = "|";
			}
			
			int idx = 1;
			boolean moreFields = true;
			while (moreFields)
			{
				String curField = parmMap.get("field"+idx++);
				if (curField == null)
				{
					moreFields = false;
				}
				else
				{
					if (record == null)
					{
						record = delimiter;
					}
					record = record + curField + delimiter;
				}
			}
		}
		
		if (record == null)
		{
			throw new ConfigurationException("No record/fields passed in record node");
		}
		
		File recordFile = new File(fileName);
		Date now = new Date();

		TimeZone tz = TimeZone.getTimeZone("UTC");
		DateFormat dateFmt = new SimpleDateFormat("yyy-MM-dd'T'HH:mm:ss:SS'+00:00'");
		dateFmt.setTimeZone(tz);
		if (record.indexOf("__TIMESTAMP__") != -1)
		{
			record = record.replaceFirst("__TIMESTAMP__", dateFmt.format(now));
		}
		
		try ( FileWriter fileWriter = new FileWriter(recordFile, true);
		        PrintWriter recPrinter = new PrintWriter(fileWriter))
		{
			recPrinter.println(record);
		}
		catch (Exception e)
		{
			throw new SvcLogicException("Cannot write record to file", e);
		}
	}

}


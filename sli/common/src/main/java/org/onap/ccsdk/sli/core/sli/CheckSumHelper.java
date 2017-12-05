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

package org.onap.ccsdk.sli.core.sli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.DatatypeConverter;

public class CheckSumHelper {

  public static String md5SumFromFile(String pathToFile) throws NoSuchAlgorithmException, IOException {
    byte[] b = Files.readAllBytes(Paths.get(pathToFile));
    return md5SumFromByteArray(b);
  }

  private static String md5SumFromByteArray(byte[] input) throws NoSuchAlgorithmException {
    byte[] hash = MessageDigest.getInstance("MD5").digest(input);
    String hexString = DatatypeConverter.printHexBinary(hash);
    return hexString.toLowerCase();
  }

}

package iot

import langhua.mdns.common.MdnsServiceThread

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
import langhua.mdns.common.MdnsServiceThread
import langhua.mdns.services.MdnsService
import org.apache.ofbiz.base.util.UtilValidate
import org.apache.ofbiz.base.util.Debug
import langhua.mdns.common.MdnsUtils

type = request.getParameter("type")
base64Name = request.getParameter("base64Name")
Debug.logInfo("type: " + type + " base64Name: " + base64Name, "getMdnsDetails.groovy")
decodedName = MdnsUtils.decodeBase64Name(base64Name)
if (!UtilValidate.isEmpty(type) || !UtilValidate.isEmpty(base64Name)) {
    MdnsService service = MdnsServiceThread.getMdnsService()
    if (service) {
        request.setAttribute("serviceInfo", service.serviceDetailsToJson(type, decodedName))
        return "success"
    }
    request.setAttribute('_ERROR_MESSAGE_', "No MDNS service available.")
}
request.setAttribute('_ERROR_MESSAGE_', "type or base64Name is empty.")
return "error"

<#--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->
<table id="mdnslist" name="mdnslist" class="table table-striped ms-table-primary" border="1">
    <thead>
        <th>${uiLabelMap.IotMdnsServiceType}</th>
        <th>${uiLabelMap.IotMdnsServiceName}</th>
        <th>${uiLabelMap.IotMdnsServiceInfo}</th>
    </thead>
    <tbody>
        <#if (serviceTypes?has_content)>
            <#list serviceTypes?keys?sort as serviceType>
                <#assign serviceNames = serviceTypes.get(serviceType)!/>
                <#if serviceNames?has_content>
                <tr name="${serviceType}">
                    <td<#if serviceNames?size gt 1> rowspan="${serviceNames?size}"</#if>>${serviceType}</td>
                    <#list 0..(serviceNames?size - 1) as i>
                    <#if (i gt 0)>
                <tr>
                    </#if>
                    <td>${serviceNames[i]!}</td>
                    <#assign serviceInfo = Static["langhua.mdns.common.MdnsServiceThread"].getMdnsService().getServiceInfo(serviceType, serviceNames[i])!/>
                    <td>
                        <#if serviceInfo?has_content>
                            <#if serviceInfo.instance?has_content>
                                Instance: ${serviceInfo.instance!}<br>
                            </#if>
                            <#if serviceInfo.subtype?has_content>
                                Subtype: ${serviceInfo.subtype!}<br>
                            </#if>
                            <#if serviceInfo.hostAddresses?has_content && serviceInfo.hostAddresses?size gt 1>
                                HostAddresses:<br>
                                <#list 0..(serviceInfo.hostAddresses?size - 1) as j>
                                &nbsp;&nbsp;&nbsp;&nbsp;${serviceInfo.hostAddresses[j]!}<br>
                                </#list>
                            <#elseif serviceInfo.hostAddress?has_content>
                                HostAddress: ${serviceInfo.hostAddress!}<br>
                            </#if>
                            <#if serviceInfo.port?has_content>
                                Port: ${serviceInfo.port!}<br>
                            </#if>
                            <#if serviceInfo.URLs?has_content && serviceInfo.URLs?size gt 1>
                                URLs:<br>
                                <#list 0..(serviceInfo.URLs?size - 1) as j>
                                &nbsp;&nbsp;&nbsp;&nbsp;${serviceInfo.URLs[j]!}<br>
                                </#list>
                            </#if>
                            <#if serviceInfo.textString?has_content>
                                Text: ${serviceInfo.textString!}<br>
                            </#if>
                            <#if serviceInfo.propertyNames?has_content && serviceInfo.propertyNames?size gt 1>
                                Properties:<br>
                                <#list serviceInfo.propertyNames as propertyName>
                                &nbsp;&nbsp;&nbsp;&nbsp;${propertyName!}: ${serviceInfo.getPropertyString(propertyName)!}<br>
                                </#list>
                            </#if>
                        </#if>
                    </td>
                </tr>
                    </#list>
                </#if>
            </#list>
        </#if>
    </tbody>
</table>
<script type="application/javascript">
    function viewExcelImportLog(sequenceNum) {
        document.location = "<@ofbizUrl>viewExcelImportLog</@ofbizUrl>?sequenceNum=" + sequenceNum;
    }

    function downloadCommentedExcel(sequenceNum) {
        document.location = "<@ofbizUrl>downloadCommentedExcel</@ofbizUrl>?sequenceNum=" + sequenceNum;
    }
</script>
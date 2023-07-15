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
<div class="row">
    <div class="col-sm-12 m-b-xs text-left p-xs">
        <label >${uiLabelMap.ExcelImportHistoryList}</label>
        <span class="tooltip pad-left30 top">${uiLabelMap.OnlyYourOwnImportHistoryDisplayed}</span>
    </div> 
</div>

<div id="loadBody" style="display:none">
</div>
<table id="mdnslist" name="mdnslist" class="table table-striped ms-table-primary">
    <thead>
        <th></th>
        <th>${uiLabelMap.SerialNumber}</th>
        <th>${uiLabelMap.Filename}</th>
        <th></th>
    </thead>
    <tbody>
        <#if (serviceTypes?has_content)>
            <#list serviceTypes?keys?sort as serviceType>
                <#assign serviceNames = serviceTypes.get(serviceType)!/>
                <#if serviceNames?has_content && serviceNames>
                <tr name="${serviceType}">
                    <td<#if serviceNames?size gt 1>colspan="${serviceNames?size}"</#if>>${serviceType}</td>
                    <#list serviceNames as serviceName>
                    <td>${serviceName}</td>
                    <#assign serviceInfo = Static["langhua.mdns.common.MdnsServiceThread"].getMdnsService().get>
                    <td></td>
                    </#list>
                </tr>
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
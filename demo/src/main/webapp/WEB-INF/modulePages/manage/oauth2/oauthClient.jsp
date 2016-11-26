<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>

<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    
    <%@ include file="/WEB-INF/commonPages/easyui-css.jsp"%>

    <title></title>

</head>

<body class="easyui-layout">

    <div data-options="region:'north'" style="overflow:hidden;">
        <form id="search-form">
            <table class="search-table">
                <tr>
                    <td>${euler:i18n('client.clientId')}</td>
                    <td><input class="easyui-textbox search-input" id="query_clientId" name="query.clientId"></td>
                    <td>${euler:i18n('client.description')}</td>
                    <td><input class="easyui-textbox search-input" id="query_description" name="query.description"></td>
                </tr>
                <tr>
                    <td>${euler:i18n('client.neverNeedApprove')}</td>
                    <td><input class="easyui-combobox search-input" id="query_neverNeedApprove" name="query.neverNeedApprove"
                            data-options="panelHeight:'auto',panelMaxHeight:'200px'"></td>
                    <td>${euler:i18n('client.enabled')}</td>
                    <td><input class="easyui-combobox search-input" id="query_enabled" name="query.enabled"
                            data-options="panelHeight:'auto',panelMaxHeight:'200px'"></td>
                </tr>
            </table>
            <table class="search-btn-table">
                <tr><td>
                <a class="easyui-linkbutton search-btn" data-options="iconCls:'icon-search'" id="search-btn" onclick="onSearch()">${euler:i18n('global.search')}</a>
                <a class="easyui-linkbutton search-btn" data-options="iconCls:'icon-reload'" id="reset-btn" onclick="onReset()">${euler:i18n('global.reset')}</a>
                </td></tr>
            </table>
        </form>
    </div>
    <div data-options="region:'center'" style="background:#eee;">
        
        <div id="toolbar">
            <a href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-add" plain="true" onclick="onAdd()">${euler:i18n('global.add')}</a>
            <a href="javascript:void(0)" class="easyui-linkbutton" id="editBtn" iconCls="icon-edit" plain="true" onclick="onEdit()">${euler:i18n('global.edit')}</a>
            <a href="javascript:void(0)" class="easyui-linkbutton" id="editBtn" iconCls="icon-ok" plain="true" onclick="onEnable()">${euler:i18n('global.enable')}</a>
            <a href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-cancel" plain="true" onclick="onDisable()">${euler:i18n('global.disable')}</a>
        </div>
        <table id="dg" class="easyui-datagrid" 
            data-options="
                fit:true,
                url:'findOauthClientByPage',
                toolbar:'#toolbar',
                fitColumns:false,
                rownumbers:true,
                remoteSort:false,
                pagination:true,
                singleSelect:false,
                onDblClickRow:onDblClickRow">
            <thead>
                <tr>
                    <th data-options="field:'ck', checkbox:true"></th>
                    <th data-options="field:'id',hidden:true">ID</th>
                    <th data-options="field:'clientId',align:'center',width:'200px'">${euler:i18n('client.clientId')}</th>
                    <th data-options="field:'accessTokenValiditySeconds',align:'center',width:'200px',formatter:tokenLifeFormatter">${euler:i18n('client.accessTokenValiditySeconds')}</th>
                    <th data-options="field:'refreshTokenValiditySeconds',align:'center',width:'200px',formatter:tokenLifeFormatter">${euler:i18n('client.refreshTokenValiditySeconds')}</th>
                    <th data-options="field:'neverNeedApprove',align:'center',width:'100px',formatter:yesOrNoFormatter">${euler:i18n('client.neverNeedApprove')}</th>
                    <th data-options="field:'enabled',align:'center',width:'100px',formatter:yesOrNoFormatter">${euler:i18n('client.enabled')}</th>
                    <th data-options="field:'description',align:'center',width:'200px'">${euler:i18n('client.description')}</th>
                </tr>
            </thead>
        </table>
        <div id="dlg" class="easyui-dialog" style="width:400px;height:357px;"
                data-options="
                    closed:true,
                    iconCls:'icon-save',
                    resizable:false,
                    modal:true,
                    constrain:true,
                    onClose:clearDlg,
                    buttons:[{text:'${euler:i18n('global.save')}', iconCls:'icon-ok', handler:onSave},{text:'${euler:i18n('global.cancel')}', iconCls:'icon-cancel', handler:onCancel}]">
            
            <div id="client-setup-tab" class="easyui-tabs" data-options="collapsible:false" style="width:100%;height:100%;">
                <div title="${euler:i18n('jsp.client.basic')}" data-options="closable:false">
                    <form id="fm" class="dlg-form" method="post">
                    <div class="dlg-body">
                        <input type="hidden" id="dlg_id" name="id">
                        <div class="dlg-line">
                            <span class="dlg-label-span">
                                <label class="dlg-label">${euler:i18n('client.clientId')}</label>
                            </span>
                            <span class="dlg-input-span">
                                <input class="easyui-textbox dlg-input" data-options="required:true,prompt:'${euler:i18n('global.lettersOrNumbers')}'" id="dlg_clientId" name="clientId">
                            </span>
                        </div>
                        <div class="dlg-line">
                            <span class="dlg-label-span">
                                <label class="dlg-label">${euler:i18n('client.accessTokenValiditySeconds')}</label>
                            </span>
                            <span class="dlg-input-span">
                                <input class="easyui-textbox dlg-input" data-options="required:true,prompt:'${euler:i18n('jsp.client.neverExpires')}'" id="dlg_accessTokenValiditySeconds" name="accessTokenValiditySeconds">
                            </span>
                        </div>
                        <div class="dlg-line">
                            <span class="dlg-label-span">
                                <label class="dlg-label">${euler:i18n('client.refreshTokenValiditySeconds')}</label>
                            </span>
                            <span class="dlg-input-span">
                                <input class="easyui-textbox dlg-input" data-options="required:true,prompt:'${euler:i18n('jsp.client.neverExpires')}'" id="dlg_refreshTokenValiditySeconds" name="refreshTokenValiditySeconds">
                            </span>
                        </div>
                        <div class="dlg-line">
                            <span class="dlg-label-span">
                                <label class="dlg-label">${euler:i18n('client.neverNeedApprove')}</label>
                            </span>
                            <span class="dlg-input-span">
                                <input class="easyui-combobox dlg-input" data-options="panelHeight:'auto',panelMaxHeight:'200px',required:true" id="dlg_neverNeedApprove" name="neverNeedApprove">
                            </span>
                        </div>
                        <div class="dlg-line">
                            <span class="dlg-label-span">
                                <label class="dlg-label">${euler:i18n('client.enabled')}</label>
                            </span>
                            <span class="dlg-input-span">
                                <input class="easyui-combobox dlg-input" data-options="panelHeight:'auto',panelMaxHeight:'200px',required:true" id="dlg_enabled" name="enabled">
                            </span>
                        </div>
                        <div class="dlg-line">
                            <span class="dlg-label-span">
                                <label class="dlg-label">${euler:i18n('client.grantType')}</label>
                            </span>
                            <span class="dlg-input-span"">
                                <label class=""><input name="grantType" id="ck_authorization_code" type="checkbox" value="authorization_code">authorization_code</label><br>
                                <label class=""><input name="grantType" id="ck_password" type="checkbox" value="password">password</label><br>
                                <label class=""><input name="grantType" id="ck_implicit" type="checkbox" value=implicit>implicit</label><br>
                                <label class=""><input name="grantType" id="ck_refresh_token" type="checkbox" value="refresh_token">refresh_token</label>
                            </span>
                        </div>
                        <div class="dlg-line"><span class="dlg-label-span"><label class="dlg-label">${euler:i18n('client.description')}</label></span><span class="dlg-input-span"><input class="easyui-textbox dlg-input" data-options="" id="dlg_description" name="description"></span></div>
                    </div>
                    </form>
                </div>
                <div title="${euler:i18n('jsp.client.resource')}" data-options="closable:false">
                    <div id="client-resource-toolbar">
                        <a href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-add" plain="true" onclick="openResourceSearchDlg()">${euler:i18n('global.add')}</a>
                        <a href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-remove" plain="true" onclick="onDeleteClientResource()">${euler:i18n('global.delete')}</a>
                    </div>
                    <div id="client-resource-list" class="easyui-datalist" style="width:100%;height:100%"
                        data-options="url:'',
                                      checkbox:true,
                                      valueField:'resourceId',
                                      textField:'name',
                                      toolbar:'#client-resource-toolbar',
                                      singleSelect:false,
                                      lines:true"></div>
                </div>
                <div title="${euler:i18n('jsp.client.scope')}" data-options="closable:false">
                    <div id="client-scope-toolbar">
                        <a href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-add" plain="true" onclick="openScopeSearchDlg()">${euler:i18n('global.add')}</a>
                        <a href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-remove" plain="true" onclick="onDeleteClientScope()">${euler:i18n('global.delete')}</a>
                    </div>
                    <div id="client-scope-list" class="easyui-datalist" style="width:100%;height:100%"
                        data-options="url:'',
                                      checkbox:true,
                                      valueField:'scope',
                                      textField:'name',
                                      toolbar:'#client-scope-toolbar',
                                      singleSelect:false,
                                      lines:true"></div>
                </div>
                <div title="${euler:i18n('jsp.client.redirectUri')}" data-options="closable:false">
                    <div id="client-redirect-uri-toolbar">
                        <a href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-add" plain="true" onclick="onAddClientRedirectURI()">${euler:i18n('global.add')}</a>
                        <a href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-remove" plain="true" onclick="onDeleteClientRedirectURI()">${euler:i18n('global.delete')}</a>
                    </div>
                    <div id="client-redirect-uri-list" class="easyui-datalist" style="width:100%;height:100%"
                        data-options="url:'',
                                      checkbox:true,
                                      valueField:'txt',
                                      textField:'txt',
                                      singleSelect:false,
                                      toolbar:'#client-redirect-uri-toolbar',
                                      lines:true"></div>
                </div>
            </div>
        </div>        
    </div>

    <%@ include file="/WEB-INF/modulePages/manage/oauth2/resourceSearchDlg.jsp"%>
    <%@ include file="/WEB-INF/modulePages/manage/oauth2/scopeSearchDlg.jsp"%>
    <div id="client-redirect-uri-dlg" class="easyui-dialog" style="width:600px;"
                data-options="
                    closed:true,
                    iconCls:'icon-save',
                    resizable:false,
                    modal:true,
                    onClose:clearAddRedirectURIDlg,
                    buttons:[{text:'${euler:i18n('global.confirm')}', iconCls:'icon-ok', handler:onConfirmAddRedirectURIDlg},{text:'${euler:i18n('global.cancel')}', iconCls:'icon-cancel', handler:onCancelAddRedirectURIDlg}]">
        
        <form id="client-redirect-uri-fm" class="dlg-form" method="post">
        <div class="dlg-body">
            <div class="dlg-line"><span class="dlg-label-span"><label class="dlg-label">${euler:i18n('jsp.client.redirectUri')}</label></span><span class="dlg-input-span"><input class="easyui-textbox dlg-input" style="width: 450px;" id="client-redirect-uri-dlg_newUri" name="newUri"></span></div>
        </div>
        </form>
    </div>
    
    <%@ include file="/WEB-INF/commonPages/easyui-js.jsp"%>

    <script>    
        $(function(){
            $('#query_neverNeedApprove').combobox({
                valueField:'key',
                textField:'value',
                editable:false,
                data:getDictData(yesOrNo, 'all')
            });
            
            $('#query_enabled').combobox({
                valueField:'key',
                textField:'value',
                editable:false,
                data:getDictData(yesOrNo, 'all')
            });
            
            $('#dlg_neverNeedApprove').combobox({
                valueField:'key',
                textField:'value',
                editable:false,
                data:getDictData(yesOrNo)
            });
            
            $('#dlg_enabled').combobox({
                valueField:'key',
                textField:'value',
                editable:false,
                data:getDictData(yesOrNo)
            });
        });
        
        function refreshDatagrid(){
            var jsonParam = $('#search-form').serializeJson();
            $('#dg').datagrid('reload', jsonParam);
        }
        
        function onSearch() {
            var jsonParam = $('#search-form').serializeJson();
            $('#dg').datagrid('load', jsonParam);            
        }
        
        function onReset() {
            $('#search-form').form('clear');
        }
        
        function onAdd() {
            $('#fm').form('clear');
            $('#dlg').dialog('open').dialog('setTitle', "${euler:i18n('jsp.client.addClient')}");
            $('#client-setup-tab').tabs('select', 0);
        }
        
        function onEdit() {
            var row = $('#dg').datagrid('getSelections');
            
            if(row == null || row.length < 1){
                $.messager.alert("${euler:i18n('global.remind')}", "${euler:i18n('global.pleaseSelectRowsToEdit')}");
            } else if(row){
                $('#fm').form('load', row[0]);
                var grantType = row[0].authorizedGrantTypes;
                for(var x in grantType) {
                    if("authorization_code" == grantType[x]){
                        $('#ck_authorization_code').prop("checked","checked");
                    }
                    if("password" == grantType[x]){
                        $('#ck_password').prop("checked","checked");
                    }
                    if("implicit" == grantType[x]){
                        $('#ck_implicit').prop("checked","checked");
                    }
                    if("refresh_token" == grantType[x]){
                        $('#ck_refresh_token').prop("checked","checked");
                    }
                }
                var registeredRedirectUri = [];
                for(var x in row[0].registeredRedirectUri){
                    var obj = {};
                    obj.txt = row[0].registeredRedirectUri[x];
                    registeredRedirectUri.push(obj);
                }
                $('#client-redirect-uri-list').datalist('loadData', registeredRedirectUri);
                $('#client-resource-list').datalist('loadData', row[0].resources);
                $('#client-scope-list').datalist('loadData', row[0].scopes);

                $('#dlg').dialog('open').dialog('setTitle', "${euler:i18n('jsp.client.editClient')}");
                
                $('#client-setup-tab').tabs('select', 0);
            }
        }
        
        function onSave() {
            var scopes = $('#client-scope-list').datalist('getData').rows;
            var resources = $('#client-resource-list').datalist('getData').rows;
            var redirectUri = $('#client-redirect-uri-list').datalist('getData').rows;
            
            var scopeIds = "";
            for(var x in scopes){
                scopeIds += scopes[x].id+";"
            }
            var resourceIds = "";
            for(var x in resources){
                resourceIds += resources[x].id+";"
            }
            var redirectUris = "";
            for(var x in redirectUri){
                redirectUris += redirectUri[x].txt+";"
            }
            $('#fm').form({
                queryParams:{"scopesIds" : scopeIds,
                             "resourceIds" : resourceIds,
                             "redirectUris" :redirectUris}
            });
            $('#fm').form('submit', {
                url:'saveOauthClient',
                onSumit:function(){
                    return $(this).form('validate');
                },
                success:function(data) {
                    if(data) {
                        $.messager.alert("${euler:i18n('global.error')}", data);
                        return;
                    }
                    clearDlg();
                    onClose();
                    refreshDatagrid();
                }
            }); 
        }
        
        function onEnable() {
            var row = $('#dg').datagrid('getSelections');

            if(row == null || row.length < 1){
                $.messager.alert("${euler:i18n('global.remind')}", "${euler:i18n('global.pleaseSelectRowsToModify')}");
            } else if(row){
                $.messager.confirm("${euler:i18n('global.warn')}", "${euler:i18n('global.sureToEnable')}", function(r) {
                    if(r) {
                        var ids = "";
                        for(var i = 0; i < row.length; i++){
                            ids += row[i].id + ';';
                        }
                        $.ajax({
                            url:'enableClients',
                            type:'POST',
                            async:true,
                            data: "ids=" + ids,
                            error:function(XMLHttpRequest, textStatus, errorThrown) {
                                $.messager.alert("${euler:i18n('global.error')}", XMLHttpRequest.responseText);
                            },
                            success:function(data, textStatus) {
                                refreshDatagrid();
                            }
                        });
                    }
                });
            }
        }

        function onDisable() {
            var row = $('#dg').datagrid('getSelections');

            if(row == null || row.length < 1){
                $.messager.alert("${euler:i18n('global.remind')}", "${euler:i18n('global.pleaseSelectRowsToModify')}");
            } else if(row){
                $.messager.confirm("${euler:i18n('global.warn')}", "${euler:i18n('global.sureToDisable')}", function(r) {
                    if(r) {
                        var ids = "";
                        for(var i = 0; i < row.length; i++){
                            ids += row[i].id + ';';
                        }
                        $.ajax({
                            url:'disableClients',
                            type:'POST',
                            async:true,
                            data: "ids=" + ids,
                            error:function(XMLHttpRequest, textStatus, errorThrown) {
                                $.messager.alert("${euler:i18n('global.error')}", XMLHttpRequest.responseText);
                            },
                            success:function(data, textStatus) {
                                refreshDatagrid();
                            }
                        });
                    }
                });
            }
        }
        
        function onCancel() {
            clearDlg();
            onClose();
        }
        
        function clearDlg(){
            $('#fm').form('clear');
            $('#client-resource-list').datalist('loadData', []);
            $('#client-scope-list').datalist('loadData', []);
            $('#client-redirect-uri-list').datalist('loadData', []);
        }
        
        function onClose(){
            $('#dlg').dialog('close');
        }
        
        function onDblClickRow(rowIndex, rowData){
            var row = $('#dg').datagrid('clearSelections');
            var row = $('#dg').datagrid('selectRow', rowIndex);
            onEdit();
        }
        
        function resourceSearchDlgCallback(data) {
            var existRows = $('#client-resource-list').datalist('getRows');
            var resultRows = [];
            resultRows = resultRows.concat(existRows);
            for(var x in data) {
                var existed = false;
                for(var y in existRows){
                    if(existRows[y].resourceId == data[x].resourceId) {
                        existed = true;
                        break;
                    }
                }
                if(!existed) {
                    resultRows.push(data[x]);
                }
            }
            $('#client-resource-list').datalist('loadData', resultRows);
        }
        
        function onDeleteClientResource() {
            var deleteRows = $('#client-resource-list').datalist('getSelections');
            if(deleteRows == null || deleteRows.length < 1){
                $.messager.alert("${euler:i18n('global.remind')}", "${euler:i18n('global.pleaseSelectRowsToModify')}");
            } else if(deleteRows){
                $.messager.confirm("${euler:i18n('global.warn')}", "${euler:i18n('global.sureToDelete')}", function(r) {
                    if(r) {
                        var existRows = $('#client-resource-list').datalist('getRows');
                        var resultRows = [];
                        for(var x in existRows) {
                            var deleted = false;
                            for(var y in deleteRows){
                                if(existRows[x].resourceId == deleteRows[y].resourceId){
                                    deleted = true;
                                    break;
                                }
                            }
                            if(!deleted){
                                resultRows = resultRows.concat(existRows[x]);                     
                            } 
                        }
                        $('#client-resource-list').datalist('loadData', resultRows); 
                    }
                });
            }
        }
        
        function scopeSearchDlgCallback(data) {
            var existRows = $('#client-scope-list').datalist('getRows');
            var resultRows = [];
            resultRows = resultRows.concat(existRows);
            for(var x in data) {
                var existed = false;
                for(var y in existRows){
                    if(existRows[y].scope == data[x].scope) {
                        existed = true;
                        break;
                    }
                }
                if(!existed) {
                    resultRows.push(data[x]);
                }
            }
            $('#client-scope-list').datalist('loadData', resultRows);
        }
        
        function onDeleteClientScope() {
            var deleteRows = $('#client-scope-list').datalist('getSelections');
            if(deleteRows == null || deleteRows.length < 1){
                $.messager.alert("${euler:i18n('global.remind')}", "${euler:i18n('global.pleaseSelectRowsToModify')}");
            } else if(deleteRows){
                $.messager.confirm("${euler:i18n('global.warn')}", "${euler:i18n('global.sureToDelete')}", function(r) {
                    if(r) {
                        var existRows = $('#client-scope-list').datalist('getRows');
                        var resultRows = [];
                        for(var x in existRows) {
                            var deleted = false;
                            for(var y in deleteRows){
                                if(existRows[x].scope == deleteRows[y].scope){
                                    deleted = true;
                                    break;
                                }
                            }
                            if(!deleted){
                                resultRows = resultRows.concat(existRows[x]);                     
                            } 
                        }
                        $('#client-scope-list').datalist('loadData', resultRows); 
                    }
                });
            }
            
        }
        
        function onAddClientRedirectURI(){
            $('#client-redirect-uri-dlg').dialog('open').dialog('setTitle', "${euler:i18n('jsp.client.addRedirectUri')}");
        }
        
        function onDeleteClientRedirectURI() {
            var deleteRows = $('#client-redirect-uri-list').datalist('getSelections');
            if(deleteRows == null || deleteRows.length < 1){
                $.messager.alert("${euler:i18n('global.remind')}", "${euler:i18n('global.pleaseSelectRowsToModify')}");
            } else if(deleteRows){
                $.messager.confirm("${euler:i18n('global.warn')}", "${euler:i18n('global.sureToDelete')}", function(r) {
                    if(r) {
                        var existRows = $('#client-redirect-uri-list').datalist('getRows');
                        
                        var resultRows = [];
                        
                        for(var x in existRows){
                            var deleted = false;
                            for(var y in deleteRows) {
                                if(existRows[x].txt == deleteRows[y].txt){
                                    deleted = true;
                                    break;
                                }
                            }
                            if(!deleted){
                                resultRows = resultRows.concat(existRows[x]);                     
                            } 
                        }
                    
                        $('#client-redirect-uri-list').datalist('loadData', resultRows);
                    }
                });
            }            
        }
        
        function onConfirmAddRedirectURIDlg(){
            var newUri = $('#client-redirect-uri-dlg_newUri').textbox('getValue');
            
            var existRows = $('#client-redirect-uri-list').datalist('getRows');
            for(var x in existRows){
                if(existRows[x].txt == newUri) {
                    return;
                }
            }
            
            var resultRows = [];
            resultRows = resultRows.concat(existRows);
            var newRow = {};
            newRow.txt = newUri;
            resultRows.push(newRow);
            $('#client-redirect-uri-list').datalist('loadData', resultRows);   
            clearAddRedirectURIDlg();
            onCloseAddRedirectURIDlg();
        }
        
        function onCancelAddRedirectURIDlg(){
            clearAddRedirectURIDlg();
            onCloseAddRedirectURIDlg();
        }
        
        function clearAddRedirectURIDlg(){
            $('#client-redirect-uri-fm').form('clear');      
        }
        
        function onCloseAddRedirectURIDlg(){
            $('#client-redirect-uri-dlg').dialog('close');
        }
        
        function tokenLifeFormatter(value, row, index){
            if(-1 == value) {
                return "${euler:i18n('global.neverExpires')}";
            } else {
                return value;
            }
        }
    </script>
</body>

</html>
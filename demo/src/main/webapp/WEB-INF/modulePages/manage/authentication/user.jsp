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
                    <td>${euler:i18n('user.username')}</td>
                    <td><input class="easyui-textbox search-input" id="query_username" name="query.username"></td>
                    <td>${euler:i18n('user.fullName')}</td>
                    <td><input class="easyui-textbox search-input" id="query_fullName" name="query.fullName"></td>
                    <td>${euler:i18n('user.enabled')}</td>
                    <td><input class="easyui-combobox search-input" id="query_enabled" name="query.enabled"
                            data-options="panelHeight:'auto',panelMaxHeight:'200px'"></td>
                </tr>
                <tr>
                    <td>${euler:i18n('user.accountNonExpired')}</td>
                    <td><input class="easyui-combobox search-input" id="query_accountNonExpired" name="query.accountNonExpired"
                            data-options="panelHeight:'auto',panelMaxHeight:'200px'"></td>
                    <td>${euler:i18n('user.accountNonLocked')}</td>
                    <td><input class="easyui-combobox search-input" id="query_accountNonLocked" name="query.accountNonLocked"
                            data-options="panelHeight:'auto',panelMaxHeight:'200px'"></td>
                    <td>${euler:i18n('user.credentialsNonExpired')}</td>
                    <td><input class="easyui-combobox search-input" id="query_credentialsNonExpired" name="query.credentialsNonExpired"
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
            <a href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-edit" plain="true" onclick="onEdit()">${euler:i18n('global.edit')}</a>
            <a href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-reload" plain="true" onclick="onResetPasswd()">${euler:i18n('jsp.user.resetPassword')}</a>
            <a href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-ok" plain="true" onclick="onEnable()">${euler:i18n('global.enable')}</a>
            <a href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-cancel" plain="true" onclick="onDisable()">${euler:i18n('global.disable')}</a>
            <a href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-remove" plain="true" onclick="onDelete()">${euler:i18n('global.delete')}</a>
        </div>
        <table id="dg" class="easyui-datagrid"
            data-options="
                fit:true,
                url:'findUserByPage',
                toolbar:'#toolbar',
                fitColumns:false,
                rownumbers:true,
                remoteSort:false,
                pagination:true,
                singleSelect:true,
                onClickRow:onClickRow">
            <thead>
                <tr>
                    <th data-options="field:'id',hidden:true">ID</th>
                    <th data-options="field:'username',align:'center',width:'120px'">${euler:i18n('user.username')}</th>
                    <th data-options="field:'email',align:'center',width:'120px'">${euler:i18n('user.email')}</th>
                    <th data-options="field:'mobile',align:'center',width:'120px'">${euler:i18n('user.mobile')}</th>
                    <th data-options="field:'fullName',align:'center',width:'120px'">${euler:i18n('user.fullName')}</th>
                    <th data-options="field:'enabled',align:'center',width:'100px',formatter:yesOrNoFormatter">${euler:i18n('user.enabled')}</th>
                    <th data-options="field:'accountNonExpired',align:'center',width:'100px',formatter:yesOrNoFormatter">${euler:i18n('user.accountNonExpired')}</th>
                    <th data-options="field:'accountNonLocked',align:'center',width:'100px',formatter:yesOrNoFormatter">${euler:i18n('user.accountNonLocked')}</th>
                    <th data-options="field:'credentialsNonExpired',align:'center',width:'100px',formatter:yesOrNoFormatter">${euler:i18n('user.credentialsNonExpired')}</th>
                </tr>
            </thead>
        </table>
        <div id="dlg" class="easyui-dialog dlg-window" style="width:400px;"
                data-options="
                    closed:true,
                    iconCls:'icon-save',
                    resizable:false,
                    modal:true,
                    constrain:true,
                    onClose:clearDlg,
                    buttons:[{text:'${euler:i18n('global.save')}', iconCls:'icon-ok', handler:onSave},{text:'${euler:i18n('global.cancel')}', iconCls:'icon-cancel', handler:onCancel}]">
            <form id="fm" class="dlg-form" method="post">
                <div class="dlg-body">
                <input type="hidden" id="dlg_id" name="id">
                <div class="dlg-line"><span class="dlg-label-span"><label class="dlg-label">${euler:i18n('user.username')}</label></span><span class="dlg-input-span"><input class="easyui-textbox dlg-input" data-options="required:true" id="dlg_username" name="username"></span></div>
                <div class="dlg-line"><span class="dlg-label-span"><label class="dlg-label">${euler:i18n('user.email')}</label></span><span class="dlg-input-span"><input class="easyui-textbox dlg-input" data-options="required:true" id="dlg_email" name="email"></span></div>
                <div class="dlg-line"><span class="dlg-label-span"><label class="dlg-label">${euler:i18n('user.mobile')}</label></span><span class="dlg-input-span"><input class="easyui-textbox dlg-input" data-options="" id="dlg_mobile" name="mobile"></span></div>
                <div class="dlg-line"><span class="dlg-label-span"><label class="dlg-label">${euler:i18n('user.fullName')}</label></span><span class="dlg-input-span"><input class="easyui-textbox dlg-input" data-options="required:true" id="dlg_fullName" name="fullName"></span></div>
                <div class="dlg-line"><span class="dlg-label-span"><label class="dlg-label">${euler:i18n('user.enabled')}</label></span><span class="dlg-input-span"><input class="easyui-combobox dlg-input" data-options="panelHeight:'auto',panelMaxHeight:'200px',required:true" id="dlg_enabled" name="enabled"></span></div>
                <div class="dlg-line"><span class="dlg-label-span"><label class="dlg-label">${euler:i18n('user.accountNonExpired')}</label></span><span class="dlg-input-span"><input class="easyui-combobox dlg-input" data-options="panelHeight:'auto',panelMaxHeight:'200px',required:true" id="dlg_accountNonExpired" name="accountNonExpired"></span></div>
                <div class="dlg-line"><span class="dlg-label-span"><label class="dlg-label">${euler:i18n('user.accountNonLocked')}</label></span><span class="dlg-input-span"><input class="easyui-combobox dlg-input" data-options="panelHeight:'auto',panelMaxHeight:'200px',required:true" id="dlg_accountNonLocked" name="accountNonLocked"></span></div>
                <div class="dlg-line"><span class="dlg-label-span"><label class="dlg-label">${euler:i18n('user.credentialsNonExpired')}</label></span><span class="dlg-input-span"><input class="easyui-combobox dlg-input" data-options="panelHeight:'auto',panelMaxHeight:'200px',required:true" id="dlg_credentialsNonExpired" name="credentialsNonExpired"></span></div>
                </div>
            </form>
        </div>
        <div id="reset-passwd-dlg" class="easyui-dialog dlg-window" style="width:400px;"
                data-options="
                    closed:true,
                    iconCls:'icon-reload',
                    resizable:false,
                    modal:true,
                    onClose:clearResetPasswdDlg,
                    buttons:[{text:'${euler:i18n('global.reset')}', iconCls:'icon-ok', handler:onResetPasswdDlgSave},{text:'${euler:i18n('global.cancel')}', iconCls:'icon-cancel', handler:onResetPasswdDlgCancel}]">
            <form id="reset-passwd-fm" class="dlg-form" method="post">
                <div class="dlg-body">
                <input type="hidden" id="reset-passwd-dlg-id" name="id">
                <div class="dlg-line"><span class="dlg-label-span"><label class="dlg-label">${euler:i18n('global.newPassword')}</label></span><span class="dlg-input-span"><input class="easyui-textbox dlg-input" data-options="required:true,type:'password'" id="reset-passwd-dlg-newpassword" name="newpassword"></span></div>
                <div class="dlg-line"><span class="dlg-label-span"><label class="dlg-label">${euler:i18n('global.confirmPassword')}</label></span><span class="dlg-input-span"><input class="easyui-textbox dlg-input" data-options="required:true,type:'password'" id="reset-passwd-dlg-confirmpassword" name="confirmpassword"></span></div>
                </div>
            </form>
        </div>
    </div>
    <div data-options="region:'east',title:'${euler:i18n('user.group')}',split:true" style="width:300px;">
        <div id="user-group-toolbar">
            <a href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-save" plain="true" onclick="onSaveUserGroup()">${euler:i18n('global.saveChange')}</a>
        </div>
        <div id="user-group-list" class="easyui-datalist" style="width:100%;height:100%"
            data-options="url:'findAllGroups',
                          checkbox:true,
                          valueField:'id',
                          textField:'name',
                          singleSelect:false,
                          toolbar:'#user-group-toolbar',
                          lines:true"></div>
    </div>
    <%@ include file="/WEB-INF/commonPages/easyui-js.jsp"%>

    <script>

        $(function(){

            $('#query_enabled').combobox({
                valueField:'key',
                textField:'value',
                editable:false,
                data:getDictData(yesOrNo, 'all')
            });
            $('#query_accountNonExpired').combobox({
                valueField:'key',
                textField:'value',
                editable:false,
                data:getDictData(yesOrNo, 'all')
            });
            $('#query_accountNonLocked').combobox({
                valueField:'key',
                textField:'value',
                editable:false,
                data:getDictData(yesOrNo, 'all')
            });
            $('#query_credentialsNonExpired').combobox({
                valueField:'key',
                textField:'value',
                editable:false,
                data:getDictData(yesOrNo, 'all')
            });

            $('#dlg_enabled').combobox({
                valueField:'key',
                textField:'value',
                editable:false,
                data:getDictData(yesOrNo)
            });
            $('#dlg_accountNonExpired').combobox({
                valueField:'key',
                textField:'value',
                editable:false,
                data:getDictData(yesOrNo)
            });
            $('#dlg_accountNonLocked').combobox({
                valueField:'key',
                textField:'value',
                editable:false,
                data:getDictData(yesOrNo)
            });
            $('#dlg_credentialsNonExpired').combobox({
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
            $('#dlg_enabled').combobox('select', 'true');
            $('#dlg_accountNonExpired').combobox('select', 'true');
            $('#dlg_accountNonLocked').combobox('select', 'true');
            $('#dlg_credentialsNonExpired').combobox('select', 'true');
            $('#dlg').dialog('open').dialog('setTitle', "${euler:i18n('jsp.user.addUser')}");
        }

        function onEdit() {
            var row = $('#dg').datagrid('getSelections');

            if(row == null || row.length < 1){
                $.messager.alert("${euler:i18n('global.remind')}", "${euler:i18n('global.pleaseSelectRowsToEdit')}");
            } else if(row){
                $('#fm').form('load', row[0]);
                
                $('#dlg_enabled').combobox('clear');
                $('#dlg_accountNonExpired').combobox('clear');
                $('#dlg_accountNonLocked').combobox('clear');
                $('#dlg_credentialsNonExpired').combobox('clear');
                
                $('#dlg_enabled').combobox('select', row[0].enabled+"");
                $('#dlg_accountNonExpired').combobox('select', row[0].accountNonExpired+"");
                $('#dlg_accountNonLocked').combobox('select', row[0].accountNonLocked+"");
                $('#dlg_credentialsNonExpired').combobox('select', row[0].credentialsNonExpired+"");
                $('#dlg').dialog('open').dialog('setTitle', "${euler:i18n('jsp.user.editUser')}");

            }
        }

        function onSave() {

            $('#fm').form('submit', {
                url:'saveUser',
                onSumit:function(){
                    return false;//$(this).form('validate');
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
                            url:'enableUsers',
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
                            url:'disableUsers',
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

        function onDelete() {
            var row = $('#dg').datagrid('getSelections');

            if(row == null || row.length < 1){
                $.messager.alert("${euler:i18n('global.remind')}", "${euler:i18n('global.pleaseSelectRowsToDelete')}");
            } else if(row){
                $.messager.confirm("${euler:i18n('global.warn')}", "${euler:i18n('jsp.user.sureToDeleteUser')}", function(r) {
                    if(r) {
                        var ids = "";
                        for(var i = 0; i < row.length; i++){
                            ids += row[i].id + ';';
                        }
                        $.ajax({
                            url:'deleteUsers',
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
        }

        function onClose(){
            $('#dlg').dialog('close');
        }
        
        function onResetPasswd() {
            var row = $('#dg').datagrid('getSelections');

            if(row == null || row.length < 1){
                $.messager.alert("${euler:i18n('global.remind')}", "${euler:i18n('global.pleaseSelectRows')}");
            } else if(row){
                $('#reset-passwd-fm').form('clear');
                $('#reset-passwd-fm').form('load', row[0]);
                $('#reset-passwd-dlg').dialog('open').dialog('setTitle', "${euler:i18n('jsp.user.resetPassword')}");
            }            
        }
        
        function onResetPasswdDlgSave() {
            var newPassword = $('#reset-passwd-dlg-newpassword').textbox('getValue');
            var confirmPassword = $('#reset-passwd-dlg-confirmpassword').textbox('getValue');
            if(newPassword === confirmPassword) {
                var id = $('#reset-passwd-dlg-id').val();
                var data = {};
                data.id = id;
                data.password = newPassword;
                $.ajax({
                    url:'resetUserPassword',
                    type:'POST',
                    async:true,
                    data: data,
                    error:function(XMLHttpRequest, textStatus, errorThrown) {
                        $.messager.alert("${euler:i18n('global.error')}", XMLHttpRequest.responseText);
                    },
                    success:function(data, textStatus) {
                        clearResetPasswdDlg();
                        onResetPasswdDlgClose();
                    }
                });
            } else {
                $.messager.alert("${euler:i18n('global.error')}", "${euler:i18n('global.confirmPasswordWrong')}");
            }
        }

        function onResetPasswdDlgCancel() {
            clearResetPasswdDlg();
            onResetPasswdDlgClose();
        }

        function clearResetPasswdDlg(){
            $('#reset-passwd-fm').form('clear');
        }

        function onResetPasswdDlgClose(){
            $('#reset-passwd-dlg').dialog('close');
        }

        function onClickRow(rowIndex, rowData){
            $('#user-group-list').datalist('unselectAll');
            var groupData = [];
            for(var x in rowData.groups){
                var groupId = rowData.groups[x].id;
                var rows = $('#user-group-list').datalist('getRows');
                for(var y in rows){
                    if(rows[y].id === groupId) {
                        $('#user-group-list').datalist('selectRow', y);
                    }
                }
            }
        }

        function onSaveUserGroup() {
            var userRows = $('#dg').datalist('getSelections');

            if(userRows == null || userRows.length != 1) {
                return;
            }
            var rows = $('#user-group-list').datalist('getSelections');
            var userId = userRows[0].id;
            var groupIds = "";
            for(var x in rows){
                groupIds += rows[x].id+";";
            }

            var data = {};
            data.userId = userId;
            data.groupIds = groupIds;
            $.ajax({
                url:'saveUserGroups',
                type:'POST',
                async:true,
                data: data,
                error:function(XMLHttpRequest, textStatus, errorThrown) {
                    $.messager.alert("${euler:i18n('global.error')}", XMLHttpRequest.responseText);
                },
                success:function(data, textStatus) {
                    refreshDatagrid();
                }
            });
        }

        /* function onDblClickRow(rowIndex, rowData){
            var row = $('#dg').datagrid('clearSelections');
            var row = $('#dg').datagrid('selectRow', rowIndex);
            onEdit();
        } */
    </script>
</body>

</html>
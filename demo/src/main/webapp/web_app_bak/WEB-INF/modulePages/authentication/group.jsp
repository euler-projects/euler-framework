<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>

<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    
    <link rel="stylesheet" href="${contextPath}/resources/css/lib/easyui/themes/metro/easyui.css">
    <link rel="stylesheet" href="${contextPath}/resources/css/lib/easyui/themes/icon.css">
    <link rel="stylesheet" href="${contextPath}/resources/css/lib/global.css">
    <link rel="stylesheet" href="${contextPath}/resources/css/lib/icon.css">

    <title></title>

</head>

<body class="easyui-layout">

    <div data-options="region:'north'" style="overflow:hidden;">
        <form id="search-form">
            <table class="search-table">
                <tr>
                    <td>${euler:i18n('group.name')}</td>
                    <td><input class="easyui-textbox search-input" id="query_name" name="query.name"></td>
                    <td>${euler:i18n('group.description')}</td>
                    <td><input class="easyui-textbox search-input" id="query_description" name="query.description"></td>
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
            <a href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-remove" plain="true" onclick="onDelete()">${euler:i18n('global.delete')}</a>
        </div>
        <table id="dg" class="easyui-datagrid" 
            data-options="
                fit:true,
                url:'findGroupByPage',
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
                    <th data-options="field:'name',align:'center',width:'200px'">${euler:i18n('group.name')}</th>
                    <th data-options="field:'description',align:'center',width:'200px'">${euler:i18n('group.description')}</th>
                </tr>
            </thead>
        </table>
        <div id="dlg" class="easyui-dialog dlg-window" style="width:400px;"
                data-options="
                    closed:true,
                    iconCls:'icon-save',
                    resizable:false,
                    modal:true,
                    onClose:clearDlg,
                    buttons:[{text:'${euler:i18n('global.save')}', iconCls:'icon-ok', handler:onSave},{text:'${euler:i18n('global.cancel')}', iconCls:'icon-cancel', handler:onCancel}]">
            <form id="fm" class="dlg-form" method="post">
                <div class="dlg-body">
                <input type="hidden" id="dlg_id" name="id">
                <div class="dlg-line"><span class="dlg-label-span"><label class="dlg-label">${euler:i18n('group.name')}</label></span><span class="dlg-input-span"><input class="easyui-textbox dlg-input" data-options="required:true" id="dlg_name" name="name"></span></div>
                <div class="dlg-line"><span class="dlg-label-span"><label class="dlg-label">${euler:i18n('group.description')}</label></span><span class="dlg-input-span"><input class="easyui-textbox dlg-input" data-options="" id="dlg_description" name="description"></span></div>
                </div>
            </form>
        </div>        
    </div>    
    <div data-options="region:'east',title:'${euler:i18n('group.authority')}',split:true" style="width:300px;">
        <div id="group-auth-toolbar">
            <a href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-save" plain="true" onclick="onSaveGroupAuth()">${euler:i18n('global.saveChange')}</a>
        </div>
        <div id="group-auth-list" class="easyui-datalist" style="width:100%;height:100%"
            data-options="url:'findAllAuthorities',
                          checkbox:true,
                          valueField:'id',
                          textField:'name',
                          singleSelect:false,
                          toolbar:'#group-auth-toolbar',
                          lines:true"></div>
    </div>
    <script src="${contextPath}/resources/scripts/lib/easyui/jquery.min.js"></script>
    <script src="${contextPath}/resources/scripts/lib/easyui/jquery.easyui.min.js"></script>
    <script src="${contextPath}/resources/scripts/lib/easyui/easyui-lang-zh_CN.js"></script>
    <script src="${contextPath}/resources/scripts/lib/common-dict.js"></script>
    <script src="${contextPath}/resources/scripts/lib/util.js"></script>

    <script>    
        $(function(){
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
            $('#dlg').dialog('open').dialog('setTitle', "${euler:i18n('jsp.group.addGroup')}");
        }
        
        function onEdit() {
            var row = $('#dg').datagrid('getSelections');
            
            if(row == null || row.length < 1){
                $.messager.alert("${euler:i18n('global.remind')}", "${euler:i18n('global.pleaseSelectRowsToEdit')}");
            } else if(row){
                $('#fm').form('load', row[0]);
                $('#dlg').dialog('open').dialog('setTitle', "${euler:i18n('jsp.group.editGroup')}");
                
            }
        }
        
        function onSave() {
            $('#fm').form('submit', {
                url:'saveGroup',
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
        
        function onDelete() {
            var row = $('#dg').datagrid('getSelections');
            
            if(row == null || row.length < 1){
                $.messager.alert("${euler:i18n('global.remind')}", "${euler:i18n('global.pleaseSelectRowsToDelete')}");
            } else if(row){
                $.messager.confirm("${euler:i18n('global.warn')}", "${euler:i18n('global.sureToDelete')}", function(r) {
                    if(r) {
                        var ids = "";
                        for(var i = 0; i < row.length; i++){
                            ids += row[i].id + ';';
                        }
                        $.ajax({
                            url:'deleteGroups',
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
        
        function onClickRow(rowIndex, rowData){
            $('#group-auth-list').datalist('unselectAll'); 
            var groupData = [];
            for(var x in rowData.authorities){
                var groupId = rowData.authorities[x].id;
                var rows = $('#group-auth-list').datalist('getRows');
                for(var y in rows){
                    if(rows[y].id === groupId) {
                        $('#group-auth-list').datalist('selectRow', y);                        
                    }
                }
            }
        }
        
        function onSaveGroupAuth() {
            var authRows = $('#dg').datalist('getSelections');
            
            if(authRows == null || authRows.length != 1) {
                return;
            }
            var rows = $('#group-auth-list').datalist('getSelections');
            var groupId = authRows[0].id;
            var authIds = "";
            for(var x in rows){
                authIds += rows[x].id+";";
            }
            
            var data = {};
            data.groupId = groupId;
            data.athorityIds = authIds;
            $.ajax({
                url:'saveGroupAuthorities',
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
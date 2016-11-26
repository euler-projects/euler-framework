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
                    <td>${euler:i18n('codeTable.name')}</td>
                    <td><input class="easyui-textbox search-input" id="query_name" name="query.name"></td>
                    <td>${euler:i18n('codeTable.codeType')}</td>
                    <td><input class="easyui-combobox search-input" id="query_codeType" name="query.codeType"
                            data-options="panelHeight:'auto',panelMaxHeight:'200px'"></td>
                    <td>${euler:i18n('codeTable.description')}</td>
                    <td><input class="easyui-textbox search-input" id="query_description" name="query.description"></td>
                </tr>
                <tr>
                    <td>${euler:i18n('codeTable.value')}</td>
                    <td><input class="easyui-textbox search-input" id="query_value" name="query.value"></td>
                    <td>${euler:i18n('codeTable.valueZhCn')}</td>
                    <td><input class="easyui-textbox search-input" id="query_valueZhCn" name="query.valueZhCn"></td>
                    <td>${euler:i18n('codeTable.valueEnUs')}</td>
                    <td><input class="easyui-textbox search-input" id="query_valueEnUs" name="query.valueEnUs"></td>
                </tr>
                <%-- <tr>
                    <td>${euler:i18n('createPerson')}</td>
                    <td><input class="easyui-textbox search-input" id="query_createBy" name="query.createBy"></td>
                    <td>${euler:i18n('lastModifyPerson')}</td>
                    <td><input class="easyui-textbox search-input" id="query_modifyBy" name="query.modifyBy"></td>
                    <td>${euler:i18n('lastModifyDate')}</td>
                    <td><input class="easyui-datebox search-input" id="query_modifyDate" name="query.modifyDate"></td>
                </tr> --%>
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
                url:'findCodeTableByPage',
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
                    <th data-options="field:'name',align:'center',width:'90px'">${euler:i18n('codeTable.name')}</th>
                    <th data-options="field:'codeType',align:'center',width:'120px',formatter:codeTypeFormatter">${euler:i18n('codeTable.codeType')}</th>
                    <th data-options="field:'key',align:'center',width:'60px'">${euler:i18n('codeTable.key')}</th>
                    <th data-options="field:'value',align:'center',width:'160px'">${euler:i18n('codeTable.value')}</th>
                    <th data-options="field:'valueZhCn',align:'center',width:'160px'">${euler:i18n('codeTable.valueZhCn')}</th>
                    <th data-options="field:'valueEnUs',align:'center',width:'160px'">${euler:i18n('codeTable.valueEnUs')}</th>
                    <th data-options="field:'showOrder',align:'center',width:'100px'">${euler:i18n('codeTable.showOrder')}</th>
                    <th data-options="field:'description',align:'center',width:'160px'">${euler:i18n('codeTable.description')}</th>
                    <%-- <th data-options="field:'createByName',align:'center'">${euler:i18n('createPerson')}</th>
                    <th data-options="field:'modifyByName',align:'center'">${euler:i18n('lastModifyPerson')}</th>
                    <th data-options="field:'modifyDate',align:'center',formatter:unixDatetimeFormatter">${euler:i18n('lastModifyDate')}</th> --%>
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
                <div class="dlg-line"><span class="dlg-label-span"><label class="dlg-label">${euler:i18n('codeTable.name')}</label></span><span class="dlg-input-span"><input class="easyui-textbox dlg-input" data-options="required:true,prompt:'${euler:i18n('global.lettersOrNumbers')}'" id="dlg_name" name="name"></span></div>
                <div class="dlg-line"><span class="dlg-label-span"><label class="dlg-label">${euler:i18n('codeTable.codeType')}</label></span><span class="dlg-input-span"><input class="easyui-combobox dlg-input" data-options="required:true,panelHeight:'auto',panelMaxHeight:'200px'" id="dlg_codeType" name="codeType"></span></div>
                <div class="dlg-line"><span class="dlg-label-span"><label class="dlg-label">${euler:i18n('codeTable.key')}</label></span><span class="dlg-input-span"><input class="easyui-textbox dlg-input" data-options="required:true" id="dlg_key" name="key"></span></div>
                <div class="dlg-line"><span class="dlg-label-span"><label class="dlg-label">${euler:i18n('codeTable.value')}</label></span><span class="dlg-input-span"><input class="easyui-textbox dlg-input" data-options="required:true" id="dlg_value" name="value"></span></div>
                <div class="dlg-line"><span class="dlg-label-span"><label class="dlg-label">${euler:i18n('codeTable.valueZhCn')}</label></span><span class="dlg-input-span"><input class="easyui-textbox dlg-input" data-options="required:false" id="dlg_value" name="valueZhCn"></span></div>
                <div class="dlg-line"><span class="dlg-label-span"><label class="dlg-label">${euler:i18n('codeTable.valueEnUs')}</label></span><span class="dlg-input-span"><input class="easyui-textbox dlg-input" data-options="required:false" id="dlg_value" name="valueEnUs"></span></div>
                <div class="dlg-line"><span class="dlg-label-span"><label class="dlg-label">${euler:i18n('codeTable.showOrder')}</label></span><span class="dlg-input-span"><input class="easyui-textbox dlg-input" data-options="required:false" id="dlg_showOrder" name="showOrder"></span></div>
                <div class="dlg-line"><span class="dlg-label-span"><label class="dlg-label">${euler:i18n('codeTable.description')}</label></span><span class="dlg-input-span"><input class="easyui-textbox dlg-input" data-options="required:false" id="dlg_description" name="description"></span></div>
                </div>
            </form>
        </div>
    </div>
    
    <%@ include file="/WEB-INF/commonPages/easyui-js.jsp"%>

    <script>
        $(function(){ 
            
            $('#query_codeType').combobox({
                valueField:'key',
                textField:'value',
                editable:false,
                data:getDictData(codeType, 'all')
            });
            
            $('#dlg_codeType').combobox({
                valueField:'key',
                textField:'value',
                editable:false,
                data:getDictData(codeType)
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
            $('#dlg').dialog('open').dialog('setTitle', "${euler:i18n('jsp.codeTable.addCodeTable')}");
        }
        
        function onEdit() {
            var row = $('#dg').datagrid('getSelections');
            
            if(row == null || row.length < 1){
                $.messager.alert("${euler:i18n('global.remind')}", "${euler:i18n('global.pleaseSelectRowsToEdit')}");
            } else if(row){
                $('#fm').form('clear');
                $('#fm').form('load', row[0]);
                $('#dlg').dialog('open').dialog('setTitle', "${euler:i18n('jsp.codeTable.editCodeTable')}");
                
            }
        }
        
        function onSave() {
            $('#fm').form('submit', {
                url:'saveCodeTable',
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
                            url:'deleteCodeTables',
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
        
        function onDblClickRow(rowIndex, rowData){
            var row = $('#dg').datagrid('clearSelections');
            var row = $('#dg').datagrid('selectRow', rowIndex);
            onEdit();
        }
        
        function codeTypeFormatter(value, row, index) {
            for(i in codeType){
                if(codeType[i].key == value)
                    return codeType[i].value;
            }
        }
    </script>
</body>

</html>
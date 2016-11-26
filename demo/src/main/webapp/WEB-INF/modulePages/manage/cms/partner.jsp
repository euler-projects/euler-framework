<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>

<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    
    <%@ include file="/WEB-INF/commonPages/easyui-css.jsp"%>

    <title></title>
    
    <style>
        .dlg-input{
            width: 260px;
        }
    </style>

</head>

<body class="easyui-layout">

    <div data-options="region:'north'" style="overflow:hidden;">
        <form id="search-form">
            <table class="search-table">
                <tr>
                    <td>${euler:i18n('partner.name')}</td>
                    <td><input class="easyui-textbox search-input" id="query_name" name="query.name"></td>
                    <td>${euler:i18n('partner.summary')}</td>
                    <td><input class="easyui-textbox search-input" id="query_summary" name="query.summary"></td>
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
                url:'findPartnerByPage',
                toolbar:'#toolbar',
                fitColumns:false,
                rownumbers:false,
                remoteSort:false,
                pagination:true,
                singleSelect:false,
                onDblClickRow:onDblClickRow">
            <thead>
                <tr>
                    <th data-options="field:'ck', checkbox:true"></th>
                    <th data-options="field:'id',hidden:true">ID</th>
                    <th data-options="field:'name',align:'center',width:'200px'">${euler:i18n('partner.name')}</th>
                    <th data-options="field:'summary',align:'center',width:'200px'">${euler:i18n('partner.summary')}</th>
                    <th data-options="field:'logoFileName',align:'center',width:'210px',formatter:imgFormatter">${euler:i18n('partner.logo')}</th>
                    <th data-options="field:'url',align:'center',width:'200px',formatter:urlFormatter">${euler:i18n('partner.url')}</th>
                    <th data-options="field:'show',align:'center',width:'60px',formatter:yesOrNoFormatter">${euler:i18n('partner.show')}</th>
                    <th data-options="field:'order',align:'center',width:'60px'">${euler:i18n('partner.order')}</th>
                </tr>
            </thead>
        </table>
        <div id="dlg" class="easyui-dialog dlg-window" style="width:450px;"
                data-options="
                    closed:true,
                    iconCls:'icon-save',
                    resizable:false,
                    modal:true,
                    onClose:clearDlg,
                    constrain:true,
                    buttons:[{text:'${euler:i18n('global.save')}', iconCls:'icon-ok', handler:onSave},{text:'${euler:i18n('global.cancel')}', iconCls:'icon-cancel', handler:onCancel}]">
            <form id="fm" class="dlg-form" enctype="multipart/form-data" method="post">
                <div class="dlg-body">
                <input type="hidden" id="dlg_id" name="id">
                <div class="dlg-line">
                    <span class="dlg-label-span">
                        <label class="dlg-label">${euler:i18n('partner.name')}</label>
                    </span>
                    <span class="dlg-input-span">
                        <input class="easyui-textbox dlg-input" data-options="required:true" id="dlg_name" name="name">
                    </span>
                </div>
                <div class="dlg-line"><span class="dlg-label-span"><label class="dlg-label">${euler:i18n('partner.logo')}</label></span><span class="dlg-input-span"><input class="easyui-filebox dlg-input" data-options="prompt:'${euler:i18n('jsp.partner.maxSize')}',buttonText:'${euler:i18n('global.chooseFile')}'" id="dlg_logo" name="logo"></span></div>
                <div class="dlg-line">
                    <span class="dlg-label-span">
                        <label class="dlg-label">
                        <label class="">${euler:i18n('partner.show')}<input id="ck_show" type="checkbox" value="true" name="show"></label></label>
                    </span>
                    <span class="dlg-input-span"><input class="easyui-textbox dlg-input" data-options="prompt:'${euler:i18n('jsp.partner.order')}'" id="dlg_order" name="order"></span></div>
                <div class="dlg-line"><span class="dlg-label-span"><label class="dlg-label">${euler:i18n('partner.url')}</label></span><span class="dlg-input-span"><input class="easyui-textbox dlg-input" data-options="" id="dlg_url" name="url"></span></div>
                <div class="dlg-line" style="height:93px;">
                    <span class="dlg-label-span">
                        <label class="dlg-label">${euler:i18n('partner.summary')}</label>
                    </span>
                    <span class="dlg-input-span">
                        <input class="easyui-textbox dlg-input" style="height:87px;" data-options="multiline:true" id="dlg_summary" name="summary">
                    </span>
                </div>
                </div>
            </form>
        </div>        
    </div>
    <%@ include file="/WEB-INF/commonPages/easyui-js.jsp"%>

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
            $('#ck_show').attr('checked', 'checked');
            $('#dlg').dialog('open').dialog('setTitle', "${euler:i18n('jsp.partner.addPartner')}");
        }
        
        function onEdit() {
            var row = $('#dg').datagrid('getSelections');
            
            if(row == null || row.length < 1){
                $.messager.alert("${euler:i18n('global.remind')}", "${euler:i18n('global.pleaseSelectRowsToEdit')}");
            } else if(row){
                $('#fm').form('load', row[0]);
                $('#dlg').dialog('open').dialog('setTitle', "${euler:i18n('jsp.partner.editPartner')}");
                
            }
        }
        
        function onSave() {
            $('#fm').form('submit', {
                url:'savePartner',
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
                            url:'deletePartners',
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
    </script>
</body>

</html>
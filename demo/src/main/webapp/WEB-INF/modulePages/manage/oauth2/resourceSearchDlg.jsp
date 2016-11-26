<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%-- OAUTH资源服务器搜索页面组件,包含此页面后通过openResourceSearchDlg()打开搜索对话框,需自行实现resourceSearchDlgCallback(resources)接收搜索到的OAUTH资源服务器信息 --%>

<div id="resource-search-dlg" class="easyui-dialog" title="${euler:i18n('jsp.resource.searchResource')}" style="width:460px;height:400px;"
        data-options="
            closed:true,
            iconCls:'icon-search',
            resizable:false,
            modal:true,
            constrain:true,
            onClose:clearResourceSearchDlg,
            buttons:[{text:'${euler:i18n('global.confirm')}', iconCls:'icon-ok', handler:confirmResourceSearchDlg},{text:'${euler:i18n('global.cancel')}', iconCls:'icon-cancel', handler:closeResourceSearchDlg}]">
    <div id="resource-search-dlg-toolbar">
        <input class="easyui-searchbox search-input" id="resource-search-dlg-searchbox">
    </div>
    <table id="resource-search-dlg-dg" class="easyui-datagrid" 
        data-options="
            fit:true,
            url:'findOauthResourceByPage',
            toolbar:'#resource-search-dlg-toolbar',
            fitColumns:false,
            rownumbers:true,
            remoteSort:false,
            pagination:true,
            singleSelect:false,
            onDblClickRow:onDblClickResourceSearchDlgRow">
        <thead>
            <tr>
                <th data-options="field:'ck', checkbox:true"></th>
                <th data-options="field:'id',hidden:true">ID</th>
                <th data-options="field:'resourceId',align:'center',width:'100px'">${euler:i18n('resource.resourceId')}</th>
                <th data-options="field:'name',align:'center',width:'100px'">${euler:i18n('resource.name')}</th>
                <th data-options="field:'description',align:'center',width:'150px'">${euler:i18n('resource.description')}</th>
            </tr>
        </thead>
    </table>
</div>
<script>
    $(function(){
        $('#resource-search-dlg-searchbox').searchbox({
            searcher:function(value,name){
                doResourceSearchDlgSearch(value);
            },
            prompt:"${euler:i18n('jsp.resource.searchResourceCodeOrName')}"
        });
    });
    
    function doResourceSearchDlgSearch(value){
        var serializeObj={};
        serializeObj['query.resourceNameOrId']=value;
        $('#resource-search-dlg-dg').datagrid('load', serializeObj);
    }
    
    function clearResourceSearchDlg() {
        $('#resource-search-dlg-searchbox').textbox('clear');        
    }
    
    function openResourceSearchDlg(){
        clearResourceSearchDlg();
        $('#resource-search-dlg').dialog('open');
        $('#resource-search-dlg-dg').datagrid('reload'); 
    }

    function onDblClickResourceSearchDlgRow(rowIndex, rowData){
        var rows = [];
        rows.push(rowData);
        resourceSearchDlgCallback(rows);
        closeResourceSearchDlg();
    }
    
    function confirmResourceSearchDlg(){
        var rows = $('#resource-search-dlg-dg').datagrid('getSelections');
        
        if(rows == null || rows.length < 1){
            $.messager.alert("${euler:i18n('global.remind')}", "${euler:i18n('global.pleaseSelectRowsToAdd')}");
        } else if(rows){
            resourceSearchDlgCallback(rows);
            closeResourceSearchDlg();            
        }
    }
    
    function closeResourceSearchDlg(){
        clearResourceSearchDlg();
        $('#resource-search-dlg').dialog('close');
    }
</script>

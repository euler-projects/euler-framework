<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%-- SCOPE搜索页面组件,包含此页面后通过openScopeSearchDlg()打开搜索对话框,需自行实现scopeSearchDlgCallback(scopes)接收搜索到的SCOPE信息 --%>

    <div id="scope-search-dlg" class="easyui-dialog" title="${euler:i18n('jsp.scope.searchScope')}" style="width:460px;height:400px;"
        data-options="
            closed:true,
            iconCls:'icon-search',
            resizable:false,
            modal:true,
            constrain:true,
            onClose:clearScopeSearchDlg,
            buttons:[{text:'${euler:i18n('global.confirm')}', iconCls:'icon-ok', handler:confirmScopeSearchDlg},{text:'${euler:i18n('global.cancel')}', iconCls:'icon-cancel', handler:closeScopeSearchDlg}]">
    <div id="scope-search-dlg-toolbar">
        <input class="easyui-searchbox search-input" id="scope-search-dlg-searchbox">
    </div>
    <table id="scope-search-dlg-dg" class="easyui-datagrid" 
        data-options="
            fit:true,
            url:'findOauthScopeByPage',
            toolbar:'#scope-search-dlg-toolbar',
            fitColumns:false,
            rownumbers:true,
            remoteSort:false,
            pagination:true,
            singleSelect:false,
            onDblClickRow:onDblClickScopeSearchDlgRow">
        <thead>
            <tr>
                <th data-options="field:'ck', checkbox:true"></th>
                <th data-options="field:'id',hidden:true">ID</th>
                <th data-options="field:'scope',align:'center',width:'100px'">${euler:i18n('scope.scope')}</th>
                <th data-options="field:'name',align:'center',width:'100px'">${euler:i18n('scope.name')}</th>
                <th data-options="field:'description',align:'center',width:'150px'">${euler:i18n('scope.description')}</th>
            </tr>
        </thead>
    </table>
</div>
<script>
    $(function(){
        $('#scope-search-dlg-searchbox').searchbox({
            searcher:function(value,name){
                doScopeSearchDlgSearch(value);
            },
            prompt:"${euler:i18n('jsp.scope.searchScopeCodeOrName')}"
        });
    });
    
    function doScopeSearchDlgSearch(value){
        var serializeObj={};
        serializeObj['query.scopeNameOrCode']=value;
        $('#scope-search-dlg-dg').datagrid('load', serializeObj);
    }
    
    function clearScopeSearchDlg() {
        $('#scope-search-dlg-searchbox').textbox('clear');        
    }
    
    function openScopeSearchDlg(){
        clearScopeSearchDlg();
        $('#scope-search-dlg').dialog('open');
        $('#scope-search-dlg-dg').datagrid('reload'); 
    }

    function onDblClickScopeSearchDlgRow(rowIndex, rowData){
        var rows = [];
        rows.push(rowData);
        scopeSearchDlgCallback(rows);
        closeScopeSearchDlg();
    }
    
    function confirmScopeSearchDlg(){
        var rows = $('#scope-search-dlg-dg').datagrid('getSelections');
        
        if(rows == null || rows.length < 1){
            $.messager.alert("${euler:i18n('global.remind')}", "${euler:i18n('global.pleaseSelectRowsToAdd')}");
        } else if(rows){
            scopeSearchDlgCallback(rows);
            closeScopeSearchDlg();            
        }
    }
    
    function closeScopeSearchDlg(){
        clearScopeSearchDlg();
        $('#scope-search-dlg').dialog('close');
    }
</script>

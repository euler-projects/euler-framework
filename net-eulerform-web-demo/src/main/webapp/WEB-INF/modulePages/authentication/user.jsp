<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.text.SimpleDateFormat,java.util.Date" %>
<!DOCTYPE html>
<html lang="zh-CN">

<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <link rel="stylesheet" type="text/css" href="../resources/themes/metro/easyui.css">
    <link rel="stylesheet" type="text/css" href="../resources/themes/icon.css">
    <link rel="stylesheet" type="text/css" href="../resources/css/root/index.css">
    <link rel="stylesheet" type="text/css" href="../resources/css/global.css">

    <title></title>


</head>

<body class="easyui-layout">

    <div data-options="region:'north'" style="overflow:hidden;">
        <form id="search-form">
            <table class="search-table">
                <tr>
                    <td>${euler:i18n('codeTable.name')}</td>
                    <td><input class="easyui-textbox" style="width: 150px" id="query_name" name="query.name" /></td>
                    <td>${euler:i18n('codeTable.codeType')}</td>
                    <td><input class="easyui-combobox" style="width: 150px" id="query_codeType" name="query.codeType"
                            data-options="panelHeight:'auto',panelMaxHeight:'200px'" /></td>
                    <td>${euler:i18n('codeTable.description')}</td>
                    <td><input class="easyui-textbox" style="width: 150px" id="query_description" name="query.description" /></td>
                </tr>
                <tr>
                    <td>${euler:i18n('codeTable.value')}</td>
                    <td><input class="easyui-textbox" style="width: 150px" id="query_value" name="query.value" /></td>
                    <td>${euler:i18n('codeTable.valueZhCn')}</td>
                    <td><input class="easyui-textbox" style="width: 150px" id="query_valueZhCn" name="query.valueZhCn" /></td>
                    <td>${euler:i18n('codeTable.valueEnUs')}</td>
                    <td><input class="easyui-textbox" style="width: 150px" id="query_valueEnUs" name="query.valueEnUs" /></td>
                </tr>
                <tr>
                    <td>${euler:i18n('createPerson')}</td>
                    <td><input class="easyui-textbox" style="width: 150px" id="query_createBy" name="query.createBy" /></td>
                    <td>${euler:i18n('lastModifyPerson')}</td>
                    <td><input class="easyui-textbox" style="width: 150px" id="query_modifyBy" name="query.modifyBy" /></td>
                    <td>${euler:i18n('lastModifyDate')}</td>
                    <td><input class="easyui-datebox" style="width: 150px" id="query_modifyDate" name="query.modifyDate" /></td>
                </tr>
            </table>
            <table style="display: inline-block;">
                <tr><td>
                <a class="easyui-linkbutton" style="width: 90px;" data-options="iconCls:'icon-search'" id="search-btn" onclick="doSearch()">搜索</a>
                <a class="easyui-linkbutton" style="width: 90px;" data-options="iconCls:'icon-reload'" id="reset-btn" onclick="doReset()">重置</a>
                </td></tr>
            </table>
        </form>
    </div>
    <div data-options="region:'center'" style="background:#eee;">
        
        <div id="toolbar">
            <a href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-add" plain="true" onclick="onAdd()">创建</a>
            <a href="javascript:void(0)" class="easyui-linkbutton" id="editBtn" iconCls="icon-edit" plain="true" onclick="onEdit()">编辑</a>
            <a href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-remove" plain="true" onclick="onDelete()">删除</a>
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
                    <th data-options="field:'name',align:'center'">${euler:i18n('codeTable.name')}</th>
                    <th data-options="field:'codeType',align:'center',formatter:codeTypeFormatter">${euler:i18n('codeTable.codeType')}</th>
                    <th data-options="field:'key',align:'center'">${euler:i18n('codeTable.key')}</th>
                    <th data-options="field:'value',align:'center'">${euler:i18n('codeTable.value')}</th>
                    <th data-options="field:'valueZhCn',align:'center'">${euler:i18n('codeTable.valueZhCn')}</th>
                    <th data-options="field:'valueEnUs',align:'center'">${euler:i18n('codeTable.valueEnUs')}</th>
                    <th data-options="field:'showOrder',align:'center'">${euler:i18n('codeTable.showOrder')}</th>
                    <th data-options="field:'description',align:'center'">${euler:i18n('codeTable.description')}</th>
                    <th data-options="field:'createByName',align:'center'">${euler:i18n('createPerson')}</th>
                    <th data-options="field:'modifyByName',align:'center'">${euler:i18n('lastModifyPerson')}</th>
                    <th data-options="field:'modifyDate',align:'center',formatter:unixDateFormatter">${euler:i18n('lastModifyDate')}</th>
                </tr>
            </thead>
        </table>
        <div id="dlg" class="easyui-dialog" title="My Dialog" style="width:400px;"
                data-options="
                    closed:true,
                    iconCls:'icon-save',
                    resizable:false,
                    modal:true,
                    buttons:[{text:'保存', iconCls:'icon-ok', handler:onSave},{text:'取消', iconCls:'icon-cancel', handler:onCancel}]">
            <form id="fm" method="post">
                <input type="hidden" id="dlg_id" name="id">
                <div class="dlg_line"><label class="dlg_label">${euler:i18n('codeTable.name')}</label><input class="easyui-textbox" style="width: 150px" id="dlg_name" name="name"></div>
                <div class="dlg_line"><label class="dlg_label">${euler:i18n('codeTable.codeType')}</label><input class="easyui-textbox" style="width: 150px" id="dlg_codeType" name="codeType"></div>
                <div class="dlg_line"><label class="dlg_label">${euler:i18n('codeTable.key')}</label><input class="easyui-textbox" style="width: 150px" id="dlg_key" name="key"></div>
                <div class="dlg_line"><label class="dlg_label">${euler:i18n('codeTable.value')}</label><input class="easyui-textbox" style="width: 150px" id="dlg_value" name="value"></div>
                <div class="dlg_line"><label class="dlg_label">${euler:i18n('codeTable.valueZhCn')}</label><input class="easyui-textbox" style="width: 150px" id="dlg_value" name="valueZhCn"></div>
                <div class="dlg_line"><label class="dlg_label">${euler:i18n('codeTable.valueEnUs')}</label><input class="easyui-textbox" style="width: 150px" id="dlg_value" name="valueEnUs"></div>
                <div class="dlg_line"><label class="dlg_label">${euler:i18n('codeTable.showOrder')}</label><input class="easyui-textbox" style="width: 150px" id="dlg_showOrder" name="showOrder"></div>
                <div class="dlg_line"><label class="dlg_label">${euler:i18n('codeTable.description')}</label><input class="easyui-textbox" style="width: 150px" id="dlg_description" name="description"></div>
            </form>
        </div>
    </div>
    <script type="text/javascript" src="../resources/scripts/lib/easyui/jquery.min.js"></script>
    <script type="text/javascript" src="../resources/scripts/lib/easyui/jquery.easyui.min.js"></script>
    <script type="text/javascript" src="../resources/scripts/lib/easyui/easyui-lang-zh_CN.js"></script>
    <script type="text/javascript" src="../resources/scripts/lib/common-dict.js"></script>
    <script type="text/javascript" src="../resources/scripts/lib/common-dict-render.js"></script>

    <script>
        $(function(){ 
            
            $('#query_codeType').combobox({
                valueField:'key',
                textField:'value',
                editable:false,
                data:getDictData(codeType, 'all')
            });
        });
        
        $.fn.serializeJson=function(){ 
            var serializeObj={};
            var array=this.serializeArray();
            $(array).each(function(){
                    if(serializeObj[this.name]){
                          if($.isArray(serializeObj[this.name])){ 
                              serializeObj[this.name].push(this.value); 
                          }else{
                              serializeObj[this.name]=[serializeObj[this.name],this.value]; 
                          } 
                    }else{ 
                        serializeObj[this.name]=this.value;
                    } 
            }); 
            return serializeObj; 
        };
        
        function refreshDatagrid(){
            var jsonParam = $('#search-form').serializeJson();
            $('#dg').datagrid('reload', jsonParam);
        }
        
        function doSearch() {
            var jsonParam = $('#search-form').serializeJson();
            $('#dg').datagrid('load', jsonParam);            
        }
        
        function doReset() {
            $('#search-form').form('clear');
        }
        
        function onAdd() {
            $('#dlg').dialog('open').dialog('setTitle', '创建');
        }
        
        function onEdit() {
            var row = $('#dg').datagrid('getSelections');
            
            if(row == null || row.length < 1){
                $.messager.alert('提示', '请选择需要删除的记录');
            } else if(row){
                $('#fm').form('clear');
                $('#fm').form('load', row[0]);
                $('#dlg').dialog('open').dialog('setTitle', '编辑');
                
            }
        }
        
        function onSave() {
            $('#fm').form('submit', {
                url:'saveCodeTable',
                onSumit:function(){
                    return $(this).form('validate');
                },
                success:function(result) {
                    onClose();
                    refreshDatagrid();
                }
            });
        }
        
        function onDelete() {
            var row = $('#dg').datagrid('getSelections');
            
            if(row == null || row.length < 1){
                $.messager.alert('提示', '请选择需要删除的记录');
            } else if(row){
                $.messager.confirm('提示', '确定删除所选记录吗?', function(r) {
                    if(r) {
                        var ids = "";
                        for(var i = 0; i < row.length; i++){
                            ids += row[i].id + ',';
                        }
                        $.ajax({
                            url:'delCodeTablesByIds',
                            type:'POST',
                            async:true,
                            data: "ids=" + ids,
                            error:function() {
                                $.messager.alert('提示', '删除失败');
                            },
                            success:function() {
                                refreshDatagrid();                                    
                            }
                        });
                    }
                });
            }
        }
        
        function onCancel() {
            onClose();
        }
        
        function onClose(){
            $('#fm').form('clear');
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

        function unixDateFormatter(value, row, index) {
            return new Date(value).Format('yyyy-MM-dd hh:mm:ss');
            /* return date.getFullYear() + '-' + (date.getMonth()+1) +'-' + date.getDate() + ' '
            +date.getHours() + ':' +date.getMinutes() + ':' +date.getSeconds(); */
        }
        
        Date.prototype.Format = function (fmt) { //author: meizz 
            var o = {
                "M+": this.getMonth() + 1, //月份 
                "d+": this.getDate(), //日 
                "h+": this.getHours(), //小时 
                "m+": this.getMinutes(), //分 
                "s+": this.getSeconds(), //秒 
                "q+": Math.floor((this.getMonth() + 3) / 3), //季度 
                "S": this.getMilliseconds() //毫秒 
            };
            if (/(y+)/.test(fmt)) fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
            for (var k in o)
            if (new RegExp("(" + k + ")").test(fmt)) fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
            return fmt;
        }
    </script>
</body>

</html>
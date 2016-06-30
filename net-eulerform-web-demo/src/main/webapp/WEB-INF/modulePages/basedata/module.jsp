<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.text.SimpleDateFormat,java.util.Date" %>
<!DOCTYPE html>

<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <link rel="stylesheet" type="text/css" href="../resources/css/lib/easyui/themes/metro/easyui.css">
    <link rel="stylesheet" type="text/css" href="../resources/css/lib/easyui/themes/icon.css">
    <link rel="stylesheet" type="text/css" href="../resources/css/lib/easyui/global.css">

    <title></title>


</head>

<body class="easyui-layout">
    <div data-options="region:'center',title:'${euler:i18n('jsp.module.properties')}'" style="">
        <div id="toolbar">
            <a href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-save" plain="true" onclick="onSaveProperties()">${euler:i18n('global.saveChange')}</a>
        </div>
        <table id="pg" class="easyui-propertygrid"
        data-options="url:'',showGroup:false,fit:true,scrollbarSize:0,toolbar:'#toolbar'"></table>
    </div>
    <div data-options="region:'west',title:'${euler:i18n('jsp.module.list')}',split:true" style="width:200px;">
        <ul id="module-tree"></ul>
        <div id="module-m" class="easyui-menu" style="width:150px;">
            <div onclick="onAddModule()" data-options="iconCls:'icon-add'">${euler:i18n('jsp.module.createModule')}</div>
            <div onclick="onAddPage()" data-options="iconCls:'icon-add'">${euler:i18n('jsp.page.createPage')}</div>
            <div onclick="onRemoveModule()" data-options="iconCls:'icon-remove'">${euler:i18n('jsp.module.removeModuleAndChildPage')}</div>
        </div>
        <div id="page-m" class="easyui-menu" style="width:150px;">
            <div onclick="onAddPage()" data-options="iconCls:'icon-add'">${euler:i18n('jsp.page.createPage')}</div>
            <div onclick="onRemovePage()" data-options="iconCls:'icon-remove'">${euler:i18n('jsp.page.removepage')}</div>
        </div>
        <div id="module-dlg" class="easyui-dialog" title="${euler:i18n('jsp.module.createModule')}" style="width:400px;"
                data-options="
                    closed:true,
                    iconCls:'icon-save',
                    resizable:false,
                    modal:true,
                    onClose:clearModuleDlg,
                    buttons:[{text:'${euler:i18n('global.save')}', iconCls:'icon-ok', handler:onSaveModuleDlg},{text:'${euler:i18n('global.cancel')}', iconCls:'icon-cancel', handler:onCancelModuleDlg}]">
            <form id="module-fm" method="post">
                <div class="dlg_line"><label class="dlg_label">${euler:i18n('module.name')}</label><input class="easyui-textbox" style="width: 150px" id="dlg_module_name" name="name"></div>
                <div class="dlg_line"><label class="dlg_label">${euler:i18n('module.requireAuthority')}</label><input class="easyui-textbox" style="width: 150px" id="dlg_module_requireAuthority" name="requireAuthority"></div>
                <div class="dlg_line"><label class="dlg_label">${euler:i18n('module.showOrder')}</label><input class="easyui-textbox" style="width: 150px" id="dlg_module_showOrder" name="showOrder"></div>
                <div class="dlg_line"><label class="dlg_label">${euler:i18n('module.description')}</label><input class="easyui-textbox" style="width: 150px" id="dlg_module_description" name="description"></div>
            </form>
        </div>
        <div id="page-dlg" class="easyui-dialog" title="${euler:i18n('jsp.page.createPage')}" style="width:400px;"
                data-options="
                    closed:true,
                    iconCls:'icon-save',
                    resizable:false,
                    modal:true,
                    onClose:clearPageDlg,
                    buttons:[{text:'${euler:i18n('global.save')}', iconCls:'icon-ok', handler:onSavePageDlg},{text:'${euler:i18n('global.cancel')}', iconCls:'icon-cancel', handler:onCancelPageDlg}]">
            <form id="page-fm" method="post">
                <div class="dlg_line"><label class="dlg_label">${euler:i18n('module.name')}</label><input class="easyui-textbox" style="width: 150px" id="dlg_page_module_id" data-options="editable:false" name="moduleId"></div>
                <div class="dlg_line"><label class="dlg_label">${euler:i18n('page.name')}</label><input class="easyui-textbox" style="width: 150px" id="dlg_page_name" name="name"></div>
                <div class="dlg_line"><label class="dlg_label">${euler:i18n('page.url')}</label><input class="easyui-textbox" style="width: 150px" id="dlg_page_url" name="url"></div>
                <div class="dlg_line"><label class="dlg_label">${euler:i18n('page.requireAuthority')}</label><input class="easyui-textbox" style="width: 150px" id="dlg_page_requireAuthority" name="requireAuthority"></div>
                <div class="dlg_line"><label class="dlg_label">${euler:i18n('page.showOrder')}</label><input class="easyui-textbox" style="width: 150px" id="dlg_page_showOrder" name="showOrder"></div>
                <div class="dlg_line"><label class="dlg_label">${euler:i18n('page.description')}</label><input class="easyui-textbox" style="width: 150px" id="dlg_page_description" name="description"></div>
            </form>
        </div>
    </div>
    <script type="text/javascript" src="../resources/scripts/lib/easyui/jquery.min.js"></script>
    <script type="text/javascript" src="../resources/scripts/lib/easyui/jquery.easyui.min.js"></script>
    <script type="text/javascript" src="../resources/scripts/lib/easyui/easyui-lang-zh_CN.js"></script>
    <script type="text/javascript" src="../resources/scripts/lib/common-dict.js"></script>
    <script type="text/javascript" src="../resources/scripts/lib/util.js"></script>

    <script>
        $(function(){
            $('#module-tree').tree({
                dnd: false,
                url:'findAllModules',
                loadFilter: function(data){
                    if (data){
                        var r = [];
                        for(var x in data){
                            var obj = {};
                            obj.id = data[x].id;
                            obj.text = data[x].name;
                            obj.type = 'module';
                            obj.children = [];
                            for(var c in data[x].pages){
                                var child = {};
                                child.id = data[x].pages[c].id;
                                child.text = data[x].pages[c].name;
                                child.type = 'page';
                                obj.children.push(child);
                            }
                            r.push(obj);
                        }
                        return r;
                    } else {
                        return {};
                    }
                },
                onClick: function(node){

                    var nan = {"total":0,"rows":[]};
                    $('#pg').propertygrid('loadData', nan);
                    if("module" == node.type) {
                        $.ajax({
                            type : "GET",
                            async : true,
                            url : 'findModuleProperties/'+node.id,
                            dataType: 'json',
                            success : function(data) {
                                var result = [];
                                var obj = {};
                                obj.name="${euler:i18n('module.name')}";
                                obj.editor="text";
                                obj.value=data.name;
                                obj.propertyName='name';
                                result.push(obj);
                                obj = {}
                                obj.name="${euler:i18n('module.requireAuthority')}";
                                obj.editor="text";
                                obj.value=data.requireAuthority;
                                obj.propertyName='requireAuthority';
                                result.push(obj);
                                obj = {}
                                obj.name="${euler:i18n('module.showOrder')}";
                                obj.editor="text";
                                obj.value=data.showOrder;
                                obj.propertyName='showOrder';
                                result.push(obj);
                                obj = {}
                                obj.name="${euler:i18n('module.description')}";
                                obj.editor="text";
                                obj.value=data.description;
                                obj.propertyName='description';
                                result.push(obj);
                                var pgData = {};
                                pgData.total = 4 ;
                                pgData.rows = result;
                                pgData.type="module";
                                pgData.id=data.id;
                                $('#pg').propertygrid('loadData',pgData);
                            },
                            error : function() {}
                        });
                    } else if("page" == node.type) {
                        $.ajax({
                            type : "GET",
                            async : true,
                            url : 'findPageProperties/'+node.id,
                            dataType: 'json',
                            success : function(data) {
                                var result = [];
                                var obj = {};
                                obj.name="${euler:i18n('page.name')}";
                                obj.editor="text";
                                obj.value=data.name;
                                obj.propertyName='name';
                                result.push(obj);
                                obj = {}
                                obj.name="${euler:i18n('page.url')}";
                                obj.editor="text";
                                obj.value=data.url;
                                obj.propertyName='url';
                                result.push(obj);
                                obj = {}
                                obj.name="${euler:i18n('page.requireAuthority')}";
                                obj.editor="text";
                                obj.value=data.requireAuthority;
                                obj.propertyName='requireAuthority';
                                result.push(obj);
                                obj = {}
                                obj.name="${euler:i18n('page.showOrder')}";
                                obj.editor="text";
                                obj.value=data.showOrder;
                                obj.propertyName='showOrder';
                                result.push(obj);
                                obj = {}
                                obj.name="${euler:i18n('page.description')}";
                                obj.editor="text";
                                obj.value=data.description;
                                obj.propertyName='description';
                                result.push(obj);
                                var pgData = {};
                                pgData.total = 5 ;
                                pgData.rows = result;
                                pgData.type="page";
                                pgData.id=data.id;
                                pgData.moduleId = data.moduleId;
                                $('#pg').propertygrid('loadData',pgData);
                            },
                            error : function() {}
                        });
                    }
                },
                onContextMenu: function(e, node){
                    e.preventDefault();
                    // select the node
                    $('#module-tree').tree('select', node.target);
                    // display context menu
                    if("module" == node.type) {
                        $('#module-m').menu('show', {
                            left: e.pageX,
                            top: e.pageY
                        });
                    } else if("page" == node.type) {

                        $('#page-m').menu('show', {
                            left: e.pageX,
                            top: e.pageY
                        });
                    }
                }
            });
        });
        
        function onAddModule(){
            $('#module-dlg').dialog('open');
        }
        
        function onSaveModuleDlg(){
            $('#module-fm').form('submit', {
                url:'saveModule',
                onSumit:function(){
                    return $(this).form('validate');
                },
                success:function(result) {
                    $('#module-tree').tree('reload');
                    clearModuleDlg();
                    onCloseModuleDlg();
                }
            });            
        }
        
        function onCancelModuleDlg() {
            clearModuleDlg();
            onCloseModuleDlg();
        }
        
        function clearModuleDlg(){
            $('#module-fm').form('clear');         
        }
        
        function onCloseModuleDlg(){
            $('#module-dlg').dialog('close');
        }
        
        function onRemoveModule(){
            var node = $('#module-tree').tree('getSelected');
            
            if(node){
                $.messager.confirm("${euler:i18n('global.warn')}", "${euler:i18n('global.sureToDelete')}", function(r) {
                    if(r) {
                        $.ajax({
                            url:'deleteModule',
                            type:'POST',
                            async:true,
                            data: "id=" + node.id,
                            error:function() {
                                $.messager.alert("${euler:i18n('global.warn')}", "${euler:i18n('global.operateFailed')}");
                            },
                            success:function() {
                                $('#module-tree').tree('reload');                                  
                            }
                        });
                    }
                });
            }
        }
        
        function onAddPage(){
            var node = $('#module-tree').tree('getSelected');
            var module = {};
            if("page" == node.type) {
                module = $('#module-tree').tree('getParent', node.target);
            } else {
                module = node;
            }
            $('#page-dlg').dialog('open');
            $('#dlg_page_module_id').textbox('setValue', module.id);
            $('#dlg_page_module_id').textbox('setText', module.text);
        }
        
        function onSavePageDlg(){
            $('#page-fm').form('submit', {
                url:'savePage',
                onSumit:function(){
                    return $(this).form('validate');
                },
                success:function(result) {
                    $('#module-tree').tree('reload');
                    clearPageDlg();
                    onClosePageDlg();
                }
            });            
        }
        
        function onCancelPageDlg() {
            clearPageDlg();
            onClosePageDlg();
        }
        
        function clearPageDlg(){
            $('#page-fm').form('clear');      
        }
        
        function onClosePageDlg(){
            $('#page-dlg').dialog('close');
        }
        
        function onRemovePage(){
            var node = $('#module-tree').tree('getSelected');
            
            if(node){
                $.messager.confirm("${euler:i18n('global.warn')}", "${euler:i18n('global.sureToDelete')}", function(r) {
                    if(r) {
                        $.ajax({
                            url:'deletePage',
                            type:'POST',
                            async:true,
                            data: "id=" + node.id,
                            error:function() {
                                $.messager.alert("${euler:i18n('global.warn')}", "${euler:i18n('global.operateFailed')}");
                            },
                            success:function() {
                                $('#module-tree').tree('reload');                                  
                            }
                        });
                    }
                });
            }
        }
        
        function onSaveProperties() {
            var data = $('#pg').propertygrid('getData');
            console.log(data);
            if('module' == data.type) {
                saveModuleChange(data);
            } else if('page' == data.type) {
                savePageChange(data);
            }
        }
        
        function saveModuleChange(data) {
            var a = {};
            a.id = data.id;
            for(var i = 0 ; i < data.total; i++){
                a[data.rows[i].propertyName] = data.rows[i].value;
            }
            $.ajax({
                url:'saveModule',
                type:'POST',
                async:true,
                data: a,
                error:function() {
                    $.messager.alert("${euler:i18n('global.warn')}", "${euler:i18n('global.operateFailed')}");
                },
                success:function() {
                    $('#module-tree').tree('reload');
                }
            });
        }
        
        function savePageChange(data) {
            var a = {};
            a.id = data.id;
            a.moduleId = data.moduleId;
            for(var i = 0 ; i < data.total; i++){
                a[data.rows[i].propertyName] = data.rows[i].value;
            }
            $.ajax({
                url:'savePage',
                type:'POST',
                async:true,
                data: a,
                error:function() {
                    $.messager.alert("${euler:i18n('global.warn')}", "${euler:i18n('global.operateFailed')}");
                },
                success:function() {
                    $('#module-tree').tree('reload');
                }
            });
        }
    </script>
</body>

</html>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.text.SimpleDateFormat,java.util.Date" %>
<!DOCTYPE html>
<html lang="zh-CN">

<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <link rel="stylesheet" type="text/css" href="../resources/css/lib/easyui/themes/metro/easyui.css">
    <link rel="stylesheet" type="text/css" href="../resources/css/lib/easyui/themes/icon.css">
    <link rel="stylesheet" type="text/css" href="../resources/css/lib/easyui/global.css">

    <title></title>


</head>

<body class="easyui-layout">
    <div data-options="region:'center',title:'center title'" style="">
        <table id="pg" class="easyui-propertygrid"
        data-options="url:'',showGroup:false,fit:true,scrollbarSize:0"></table>
    </div>
    <div data-options="region:'west',title:'West',split:true" style="width:200px;">
    <%-- <ul id="tt" class="easyui-tree">
        <c:forEach items="${menu}" var="module">     
            <li><span>${module.name}</span><ul>
                <c:forEach items="${module.pages}" var="page"> 
                    <li><span>${page.name}</span></li>
                </c:forEach>
            </ul></li>
        </c:forEach>
    </ul> --%>
        <ul id="module-tree"></ul>
        <div id="module-m" class="easyui-menu" style="width:120px;">
            <div onclick="addModule()" data-options="iconCls:'icon-add'">添加模块</div>
            <div onclick="addPage()" data-options="iconCls:'icon-add'">新建页面</div>
            <div onclick="remove()" data-options="iconCls:'icon-remove'">删除模块</div>
        </div>
        <div id="page-m" class="easyui-menu" style="width:120px;">
            <div onclick="addPage()" data-options="iconCls:'icon-add'">新建页面</div>
            <div onclick="remove()" data-options="iconCls:'icon-remove'">删除页面</div>
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
                    if (data.rows){
                        var r = [];
                        for(var x in data.rows){
                            var obj = {};
                            obj.id = data.rows[x].id;
                            obj.text = data.rows[x].name;
                            obj.type = 'module';
                            obj.children = [];
                            for(var c in data.rows[x].pages){
                                var child = {};
                                child.id = data.rows[x].pages[c].id;
                                child.text = data.rows[x].pages[c].name;
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
                                obj.name="模块名称";
                                obj.editor="text";
                                obj.value=data.name;
                                result.push(obj);
                                obj = {}
                                obj.name="URL";
                                obj.editor="text";
                                obj.value=data.url;
                                result.push(obj);
                                obj = {}
                                obj.name="模块权限";
                                obj.editor="text";
                                obj.value=data.requireAuthority;
                                result.push(obj);
                                obj = {}
                                obj.name="显示顺序";
                                obj.editor="text";
                                obj.value=data.showOrder;
                                result.push(obj);
                                var rows = {};
                                rows.total = 3 ;
                                rows.rows = result;
                                $('#pg').propertygrid('loadData',rows);
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
                                obj.name="页面名称";
                                obj.editor="text";
                                obj.value=data.name;
                                result.push(obj);
                                obj = {}
                                obj.name="URL";
                                obj.editor="text";
                                obj.value=data.url;
                                result.push(obj);
                                obj = {}
                                obj.name="页面权限";
                                obj.editor="text";
                                obj.value=data.requireAuthority;
                                result.push(obj);
                                obj = {}
                                obj.name="显示顺序";
                                obj.editor="text";
                                obj.value=data.showOrder;
                                result.push(obj);
                                var rows = {};
                                rows.total = 3 ;
                                rows.rows = result;
                                $('#pg').propertygrid('loadData',rows);
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
        
        function addModule(){
            var node = $('#module-tree').tree('getSelected');
            alert(node.text);
        }
        
        function addPage(){
            var node = $('#module-tree').tree('getSelected');
            if("page" == node.type){
                var a = $('#module-tree').tree('getParent', node.target);
                alert(a.text);
            }
            alert(node.text);
        }
    </script>
</body>

</html>
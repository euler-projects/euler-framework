<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>

<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    
    <%@ include file="/WEB-INF/commonPages/easyui-css.jsp"%>
    <script src="${contextPath}/resources/scripts/lib/ueditor/ueditor.config.js"></script>
    <script src="${contextPath}/resources/scripts/lib/ueditor/ueditor.all.min.js"> </script>
    <script src="${contextPath}/resources/scripts/lib/ueditor/lang/zh-cn/zh-cn.js"></script>

    <title></title>
    
    <style>
    
        .dlg-form {
            padding:12px;
        }
        .dlg-label-span {
            border:1px solid #e0e0e0;
            text-align:center;
        }
        
        .dlg-input-span {
            border:1px solid #e0e0e0;
        }
        
        .dlg-label {
            width:50px;
        }        
        
        .dlg-input{
            width:350px;
        }
        
        .dlg-input-x3 {
            width:788px;            
        }
    </style>

</head>

<body class="easyui-layout">

    <div data-options="region:'north'" style="overflow:hidden;">
        <form id="search-form">
            <table class="search-table">
                <tr>
                    <td>${euler:i18n('blog.title')}</td>
                    <td><input class="easyui-textbox search-input" id="query_title" name="query.title"></td>
                    <td>${euler:i18n('blog.author')}</td>
                    <td><input class="easyui-textbox search-input" id="query_author" name="query.author"></td>
                    <td>${euler:i18n('blog.pubDate')}</td>
                    <td><input class="easyui-datebox search-input" id="query_pubDate" name="query.pubDate"></td>
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
                url:'findBlogByPage',
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
                    <th data-options="field:'title',align:'center',width:'500px'">${euler:i18n('blog.title')}</th>
                    <th data-options="field:'author',align:'center',width:'80px'">${euler:i18n('blog.author')}</th>
                    <th data-options="field:'pubDate',align:'center',width:'130px',formatter:unixDatetimeFormatter">${euler:i18n('blog.pubDate')}</th>
                    <th data-options="field:'top',align:'center',width:'60px',formatter:yesOrNoFormatter">${euler:i18n('blog.top')}</th>
                    <th data-options="field:'id',align:'center',width:'130px',formatter:blogPriviewFormatter">${euler:i18n('global.operate')}</th>
                </tr>
            </thead>
        </table>
        <div id="dlg" class="easyui-dialog dlg-window" style="width:96%;height:96%;"
                data-options="
                    closed:true,
                    iconCls:'icon-save',
                    resizable:false,
                    modal:false,
                    onClose:clearDlg,
                    constrain:true,
                    buttons:[{text:'${euler:i18n('global.save')}', iconCls:'icon-ok', handler:onSave},{text:'${euler:i18n('global.cancel')}', iconCls:'icon-cancel', handler:onCancel}]">
            <form id="fm" class="dlg-form" enctype="multipart/form-data" method="post">
                <table class="dlg-body" style="table-layout: fixed;">
                    <input type="hidden" id="dlg_id" name="id">
                    <tr class="dlg-line">
                        <td class="dlg-label-span" style="">
                            <label class="dlg-label">${euler:i18n('blog.title')}</label>
                        </td>
                        <td class="dlg-input-span" colspan="3">
                            <input class="easyui-textbox dlg-input dlg-input-x3" data-options="required:true" id="dlg_title" name="title">
                        </td>
                    </tr>
                    <tr class="dlg-line">
                        <td class="dlg-label-span">
                            <label class="dlg-label">${euler:i18n('blog.summary')}</label>
                        </td>
                        <td class="dlg-input-span" style="height:93px;">
                            <input class="easyui-textbox dlg-input" style="height:87px;" data-options="multiline:true,required:true" id="dlg_summary" name="summary">
                        </td>
                        <td class="dlg-label-span" rowspan="3">
                            <label class="dlg-label">${euler:i18n('blog.img')}</label>
                        </td>
                        <td class="dlg-input-span" style="height:155px;" rowspan="3">
                            <img class="dlg-input img-box" style="height:149px;width:350px;" id="dlg_img-show" src="" alt="${euler:i18n('jsp.blog.noImg')}">
                        </td>
                    </tr>
                    <tr class="dlg-line">
                        <td class="dlg-label-span">
                            <label class="dlg-label">${euler:i18n('blog.author')}</label>
                        </td>
                        <td class="dlg-input-span">
                            <input class="easyui-textbox dlg-input" style="" data-options="required:true" id="dlg_author" name="author">
                        </td>
                    </tr>
                    <tr class="dlg-line">
                        <td class="dlg-label-span">
                            <label class="dlg-label">${euler:i18n('blog.pubDate')}</label>
                        </td>
                        <td class="dlg-input-span">
                            <input class="easyui-datetimebox dlg-input" style="width:160px;" data-options="required:true"  id="dlg_pubDateStr" name="pubDateStr">
                            &nbsp;&nbsp;&nbsp;<label class=""><input id="ck_top" type="checkbox" value="true" name="top">${euler:i18n('blog.top')}</label>
                        </td>
                    </tr>
                    <tr class="dlg-line">
                        <td class="dlg-label-span">
                        </td>
                        <td class="dlg-input-span">
                        </td>
                        <td class="dlg-label-span">
                            <label class="dlg-label">${euler:i18n('jsp.blog.uploadImg')}</label>
                        </td>
                        <td class="dlg-input-span">
                            <input class="easyui-filebox dlg-input" style="" data-options="buttonText:'${euler:i18n('global.chooseFile')}'" id="dlg_img" name="img">
                        </td>
                    </tr>
                    <tr class="dlg-line" style="height:496px;">
                        <td class="dlg-label-span">
                            <label class="dlg-label">${euler:i18n('blog.text')}</label>
                        </td>
                        <td class="dlg-input-span" colspan="3" style="padding-top:3px; padding-bottom:3px;">
                            <script class="dlg-input dlg-input-x3" id="editor" name="text" type="text/plain" style="height:380px;width:794px;"></script>
                        </td>
                    </tr>
                </table>
            </form>
        </div>        
    </div>
    <%@ include file="/WEB-INF/commonPages/easyui-js.jsp"%>
    
    <script>
    var ue = UE.getEditor(
            'editor',
            {
                toolbars: [
                    [
                        'source', //源代码
                        //'preview', //预览
                        'print', //打印
                        '|',
                        'undo', //撤销
                        'redo', //重做
                        '|',
                        'formatmatch', //格式刷
                        'horizontal', //分隔线
                        'searchreplace', //查询替换
                        '|',
                        'forecolor', //字体颜色
                        'backcolor', //背景色
                        '|',
                        'justifyleft', //居左对齐
                        'justifyright', //居右对齐
                        'justifycenter', //居中对齐
                        'justifyjustify', //两端对齐
                        '|',
                        'link', //超链接
                        'unlink', //取消链接
                        'simpleupload', //单图上传
                        'insertimage', //多图上传
                        'map', //Baidu地图
                        'attachment', //附件
                    ],
                    [
                        'paragraph', //段落格式
                        'fontfamily', //字体
                        'fontsize', //字号
                        '|',
                        'bold', //加粗
                        'italic', //斜体
                        'underline', //下划线
                        'strikethrough', //删除线
                        'subscript', //下标
                        'superscript', //上标
                        'fontborder', //字符边框
                        '|',
                        'forecolor', //字体颜色
                        'backcolor' //背景色
                    ],
                    [
                        'inserttable', //插入表格
                        'insertrow', //前插入行
                        'insertcol', //前插入列
                        'deleterow', //删除行
                        'deletecol', //删除列
                        'mergeright', //右合并单元格
                        'mergedown', //下合并单元格
                        'mergecells', //合并多个单元格
                        'splittorows', //拆分成行
                        'splittocols', //拆分成列
                        'splittocells', //完全拆分单元格
                        'inserttitle', //插入标题
                        'deletecaption', //删除表格标题
                        'deletetable', //删除表格
                        'insertparagraphbeforetable',//"表格前插入行"
                        'edittable', //表格属性
                        'edittd' //单元格属性
                    ]
                ]
            });
    
    
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
            $('#dlg_pubDateStr').datetimebox('setValue', unixDatetimeFormatter((new Date()).getTime()));
            $('#dlg').dialog('open').dialog('setTitle', "${euler:i18n('jsp.blog.addBlog')}");
        }
        
        function onEdit() {
            var upload = '${contextPath}/upload/';
            var row = $('#dg').datagrid('getSelections');
            
            if(row == null || row.length < 1){
                $.messager.alert("${euler:i18n('global.remind')}", "${euler:i18n('global.pleaseSelectRowsToEdit')}");
            } else if(row){
                $('#fm').form('load', row[0]);
                $('#dlg_pubDateStr').datetimebox('setValue', unixDatetimeFormatter(row[0].pubDate));
                setImgSrc('#dlg_img-show', 356, 149, upload + row[0].imageFileName);
                UE.getEditor('editor').setContent(row[0].text);
                $('#dlg').dialog('open').dialog('setTitle', "${euler:i18n('jsp.blog.editBlog')}");
                
            }
        }
        
        function onSave() {
            $('#fm').form('submit', {
                url:'saveBlog',
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
                            url:'deleteBlog',
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
            alert($('#dlg_pubDateBox').datetimebox('getValue'));
            clearDlg();
            onClose();
        }
        
        function clearDlg(){
            $('#fm').form('clear');
            UE.getEditor('editor').setContent("");  
            setImgSrc('#dlg_img-show', 356, 149, null);        
        }
        
        function onClose(){
            $('#dlg').dialog('close');
        }
        
        function onDblClickRow(rowIndex, rowData){
            var row = $('#dg').datagrid('clearSelections');
            var row = $('#dg').datagrid('selectRow', rowIndex);
            onEdit();
        }
        
        function blogPriviewFormatter(value, row, index) {
            return '<a href="javascript:void(0)" onClick="onPriview(\''+value+'\')">${euler:i18n('global.priview')}</a>'
                 + '&nbsp;｜&nbsp;'
                 + '<a href="javascript:void(0)" onClick="onDblClickRow(\''+index+'\')">${euler:i18n('global.edit')}</a>'
                 + '&nbsp;｜&nbsp;'
                 + '<a href="javascript:void(0)" onClick="clickDelete(\''+value+'\')">${euler:i18n('global.delete')}</a>';
        }
        
        function onPriview(id) {
            alert(id);
        }
        
        function clickDelete(id) {
            $.messager.confirm("${euler:i18n('global.warn')}", "${euler:i18n('global.sureToDelete')}", function(r) {
                if(r) {
                    $.ajax({
                        url:'deleteBlog',
                        type:'POST',
                        async:true,
                        data: "ids=" + id,
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
    </script>
</body>

</html>
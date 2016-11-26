<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>

<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    
    <%@ include file="/WEB-INF/commonPages/easyui-css.jsp"%>

    <title></title>
    
    <style>
        .dlg-window {
            background-color:#E0ECFF;
            padding:6px; 
        }
        
        .dlg-body {
            border-collapse:collapse;
        }
        
        .dlg-body-title {
            border-width:1px 1px 0 1px;
            border-style:solid;
            border-color:#aaa;
        }
        
        .dlg-line {
        }
        
        .dlg-label-span {
            border:1px solid #aaa;
            text-align:center;
        }
        
        .dlg-input-span {
            border:1px solid #aaa;
        }
        
        .dlg-label {
            width: 120px;
        }
        
        .dlg-input {
            width: 600px;
        }
        
        .img-line {
            height:310px;
        }
        
        .img-box {
            height:304px;
            line-height:304px;
            font-size:30px;
            color:#aaa;
        }
    </style>

</head>

<body">
    <div id="dlg" class="easyui-dialog dlg-window" style="width:100%;"
                    data-options="
                        closed:false,
                        iconCls:'icon-save',
                        resizable:false,
                        modal:true,
                        constrain:true,
                        top:0,
                        maximized:true,
                        noheader:true,
                        border:false,
                        buttons:[{text:'${euler:i18n('global.save')}', iconCls:'icon-ok', handler:onSave},{text:'${euler:i18n('global.cancel')}', iconCls:'icon-cancel', handler:onCancel}]">
        <form id="fm" class="dlg-form" enctype="multipart/form-data" method="post">
            <div class="dlg-body">
            <div class="dlg-body-title">${euler:i18n('email.config')}</div>
            <div class="dlg-line"><span class="dlg-label-span"><label class="dlg-label">${euler:i18n('email.username')}</label></span><span class="dlg-input-span"><input class="easyui-textbox dlg-input" data-options="required:true" id="dlg_username" name="username"></span></div>
            <div class="dlg-line"><span class="dlg-label-span"><label class="dlg-label">${euler:i18n('email.password')}</label></span><span class="dlg-input-span"><input class="easyui-textbox dlg-input" data-options="type:'password',required:true" id="dlg_password" name="password"></span></div>
            <div class="dlg-line"><span class="dlg-label-span"><label class="dlg-label">${euler:i18n('email.smtp')}</label></span><span class="dlg-input-span"><input class="easyui-textbox dlg-input" data-options="required:true" id="dlg_smtp" name="smtp"></span></div>
            <div class="dlg-line"><span class="dlg-label-span"><label class="dlg-label">${euler:i18n('email.sender')}</label></span><span class="dlg-input-span"><input class="easyui-textbox dlg-input" data-options="required:true" id="dlg_sender" name="sender"></span></div>
            <div class="dlg-line"><span class="dlg-label-span"><label class="dlg-label">${euler:i18n('email.defaultReceiver')}</label></span><span class="dlg-input-span"><input class="easyui-textbox dlg-input" data-options="required:true" id="dlg_defaultReceiver" name="defaultReceiver"></span></div>
            </div>
        </form>
    </div>
    <%@ include file="/WEB-INF/commonPages/easyui-js.jsp"%>
    <script>    
        $(function(){
            loadEmailConfig();
        });
        
        function onSave() {
            $('#fm').form('submit', {
                url:'saveEmailConfig',
                onSumit:function(){
                    return $(this).form('validate');
                },
                success:function(data) {
                    if(data) {
                        $.messager.alert("${euler:i18n('global.error')}", data);
                        return;
                    }
                    loadEmailConfig();
                }
            });
        }
        
        function loadEmailConfig() {
            $.ajax({
                url:'loadEmailConfig',
                type:'GET',
                async:true,
                error:function(XMLHttpRequest, textStatus, errorThrown) {
                    $.messager.alert("${euler:i18n('global.error')}", XMLHttpRequest.responseText);
                },
                success:function(data, textStatus) {
                    $('#fm').form('load', data);
                }
            });
        }
        
        function onCancel() {
            loadEmailConfig();
        }
    </script>
    
</body>

</html>
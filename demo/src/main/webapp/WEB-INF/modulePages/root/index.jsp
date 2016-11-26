<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.text.SimpleDateFormat,java.util.Date" %>
<!DOCTYPE html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    
    <%@ include file="/WEB-INF/commonPages/easyui-css.jsp"%>
    <link rel="stylesheet" href="${contextPath}/resources/css/root/index.css">

    <title>${euler:i18n('global.websiteTitle')}</title>

</head>

<body class="easyui-layout">
    <div id="header-zone" data-options="region:'north',split:false,collapsible:false">
        <div id="header">
            <span id="site-title"><a href="${contextPath}/"><span class="demo-brand-200-20"></span></a></span>
        </div>
    </div>
    <div id="footer-zone" data-options="region:'south',split:false,collapsible:false">
        <div id="footer">&copy;<%=new SimpleDateFormat("yyyy").format(new Date()) %>&nbsp;cFrost&nbsp;<a id="icp"
                href="http://www.miitbeian.gov.cn" target="_Blank">粤ICP备15054669号</a></div>
    </div>
    <div id="content-zone"  data-options="region:'center',split:false">
        <div id="main-content" class="easyui-tabs" data-options="collapsible:false" style="width:100%;height:100%;">
            <div id="index-tab" title="${euler:i18n('global.welcome')}" data-options="closable:true">
                <h3 style="color:#0099FF;">${euler:i18n('global.welcomeTitle')}</h3>
                <p>${euler:i18n('global.welcomeInfo')}</p>
                <a href="${contextPath}/manage">后台管理</a>
            </div>
        </div>
    </div>
    <script src="${contextPath}/resources/scripts/lib/easyui/jquery.min.js"></script>
    <script src="${contextPath}/resources/scripts/lib/easyui/jquery.easyui.min.js"></script>
</body>
</html>
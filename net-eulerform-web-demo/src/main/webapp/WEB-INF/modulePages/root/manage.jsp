<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.text.SimpleDateFormat,java.util.Date" %>
<!DOCTYPE html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    
    <link rel="stylesheet" href="${contextPath}/resources/css/lib/easyui/themes/metro/easyui.css">
    <link rel="stylesheet" href="${contextPath}/resources/css/lib/easyui/themes/icon.css">
    <link rel="stylesheet" href="${contextPath}/resources/css/lib/global.css">
    <link rel="stylesheet" href="${contextPath}/resources/css/lib/icon.css">
    <link rel="stylesheet" href="${contextPath}/resources/css/root/index.css">

    <title>${euler:i18n('global.websiteTitle')}</title>

</head>

<body class="easyui-layout">
    <div id="header-zone" data-options="region:'north',split:false,collapsible:false">
        <div id="header">
                        <span id="site-title"><a href="${contextPath}/"><span class="euler-formula"></span></a></span>
            <span id="user-info" style="position:absolute;right:3px;">
                <span>${currentUser.empName}(${currentUser.username})</span>&nbsp;&nbsp;<span><a href="${contextPath}/logout">${euler:i18n('global.logout')}</a></span>
            </span>
        </div>
    </div>
    <div id="footer-zone" data-options="region:'south',split:false,collapsible:false">
        <div id="footer">Version 0.0.2 &copy;<%=new SimpleDateFormat("yyyy").format(new Date()) %>&nbsp;cFrost&nbsp;<a id="icp"
                href="http://www.miitbeian.gov.cn" target="_Blank">粤ICP备15054669号</a></div>
    </div>
    <div id="menu-zone" data-options="region:'west',title:'${euler:i18n('global.menu')}',split:false">
        <div id="sys-menu" class="easyui-accordion" style="width:100%;height:100%;border:0">
            <c:forEach items="${menu}" var="module">    
                <security:authorize access="hasAnyAuthority('${module.requireAuthority},ADMIN') ">           
                    <div title="${module.name}" data-options="iconCls:'icon-module-menu'">
                        <c:forEach items="${module.pages}" var="page"> 
                            <security:authorize access="hasAnyAuthority('${page.requireAuthority},ADMIN') ">  
                                <a href="#" onclick="addTab('${page.url}', '${page.name}')"><li>${page.name}</li></a>
                            </security:authorize>
                        </c:forEach>
                    </div>
                </security:authorize>
            </c:forEach>
        </div>
    </div>
    <div id="content-zone"  data-options="region:'center',split:false">
        <div id="main-content" class="easyui-tabs" data-options="collapsible:false" style="width:100%;height:100%;">
            <div id="index-tab" title="${euler:i18n('global.welcome')}" data-options="closable:true">
                <h3 style="color:#0099FF;">${euler:i18n('global.welcomeTitle')}</h3>
                <p>${euler:i18n('global.welcomeInfo')}</p>
            </div>
        </div>
    </div>
    <script src="${contextPath}/resources/scripts/lib/easyui/jquery.min.js"></script>
    <script src="${contextPath}/resources/scripts/lib/easyui/jquery.easyui.min.js"></script>
    
    <script>
        
        function addTab(url, title) {
            
            var html = '<iframe width="100%" height="100%" frameborder="no" border="0" marginwidth="0" marginheight="0" allowtransparency="yes" src="'+url+'"></iframe>';
            var exists = $('#main-content').tabs('exists', title);
            if(exists){
                $('#main-content').tabs('select', title);
                var tab = $('#main-content').tabs('getSelected');
                $('#main-content').tabs('update', {
                    tab: tab,
                    options: {
                        title: title,
                        content:html,
                    }
                });
                return;
            }
            
            $('#main-content').tabs('add',{
                title:title,
                content:html,
                closable:true
            });
            
        }
    </script>
</body>
</html>
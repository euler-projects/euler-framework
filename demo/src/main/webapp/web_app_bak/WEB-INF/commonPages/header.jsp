<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page isELIgnored="false" %>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta http-equiv="X-Frame-Options" content="SAMEORIGIN"> 

<link rel="stylesheet" type="text/css"
    href="${contextPath}/resources/css/frame.css">
<link rel="stylesheet" type="text/css"
    href="${contextPath}/resources/css/global.css">
<link rel="stylesheet" type="text/css"
    href="${contextPath}/resources/css/header.css">
<link rel="stylesheet" type="text/css"
    href="${contextPath}/resources/css/footer.css">

</head>
<body>
    <div id="__page__">
        <header>
            <div id="siteName">
                <a href="${contextPath}/"><div class="euler-formula"></div></a>
            </div>
            <nav>
                <a href="${contextPath}/blog/">BLOG</a>
                &nbsp;&nbsp;&nbsp;&nbsp;<a
                    href="${contextPath}/code/">CODE</a>
                &nbsp;&nbsp;&nbsp;&nbsp;<a
                    href="${contextPath}/disk/">DISK</a>
                &nbsp;&nbsp;&nbsp;&nbsp;<a
                    href="http://nexus.cfrost.net" target="_Blank">NEXUS</a>
                &nbsp;&nbsp;&nbsp;&nbsp;<a
                    href="http://mail.cfrost.net" target="_Blank">E-MAIL</a>
                &nbsp;&nbsp;&nbsp;&nbsp;<a
                    href="${contextPath}/about/">ABOUT</a>
            </nav>
            <div id="sFms">
                <form id="searchForm" method="get"
                    action="${contextPath}/doSearch.action">
                    <span id="sboxs"><input id="sBox"
                        class="searchBox" type="text" name="search"
                        value="Input to search..."
                        onfocus="searchBoxClick(this.id); setStyle(this.id, 'inline', 'color:#000000')"
                        onblur="searchBoxClick(this.id); setStyle(this.id, 'clear')">
                    </span><span id="sbtns"><input id="sBtn"
                        class="searchBtn" type="submit" value="搜 索">
                    </span>
                </form>
            </div>
            <div id="userInfo">
                <li><a href="javascript:void(0);"
                    onclick="userInfoClicked();">${currentUser.username}</a>&nbsp;<a
                    href="javascript:void(0);"
                    onclick="userInfoClicked();"><div id="kik"
                            class="img_triangle"></div></a>
                    <ul id="userMenu">
                        <li class="child_li"><a
                            href="javascript:void(0);">个人信息</a></li>
                        <li class="child_li"><a
                            href="javascript:void(0);">修改密码</a></li>
                        <li class="child_li"><a
                            href="javascript:void(0);">设置</a></li>
                        <li class="last_child_li"><a
                            href="${contextPath}/logout">注销</a></li>
                    </ul>
                </li>
            </div>
        </header>
        <div id="__body__">
            <div id="__view__">
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
<title>cFrost.net</title>
<%@ include file="/WEB-INF/commonPages/header.jsp"%>
<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/resources/css/blog/blog.css" />

<div id="blog_sidebar">
    <div class="blog_sidebar_title">主题</div>
    <div id="blog_sidebar_tag"></div>
    <div class="blog_sidebar_title">发表时间</div>
    <div id="blog_sidebar_time"></div>
</div>
<div id="blog">
</div>
<script src="<%=request.getContextPath()%>/resources/scripts/blog/blog.js"></script>
<%@ include file="/WEB-INF/commonPages/footer.jsp"%>
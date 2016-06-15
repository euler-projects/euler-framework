<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
<title>cFrost.net</title>
<%@ include file="/WEB-INF/commonPages/header.jsp"%>
<div id="progress"
    style="margin-top: -1px; padding: 10px;">
    <p>This site is being built...</p>
    ${currentUser.username}<br>
    ${currentUser.password}<br>
    ${euler:i18n('test')}<br>
    ${euler:i18n('eulerform')}<br>
    <spring:message code="test"/>
</div>

<%@ include file="/WEB-INF/commonPages/footer.jsp"%>
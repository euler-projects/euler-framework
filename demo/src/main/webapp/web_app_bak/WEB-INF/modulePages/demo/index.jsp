<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
<title>cFrost.net</title>
<%@ include file="/WEB-INF/commonPages/header.jsp"%>
<div id="progress"
    style="margin-top: -1px; padding: 10px;">
    <p>${filename}</p>
    ${user.username}<br>
    ${user.password}<br>
	<form action="upload" method="post" enctype="multipart/form-data">  
	<input type="file" name="file"> <input type="submit" value="Submit"></form> 
</div>

<%@ include file="/WEB-INF/commonPages/footer.jsp"%>
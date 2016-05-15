<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
<title>cFrost.net</title>
<%@ include file="/WEB-INF/commonPages/header.jsp"%>
<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/disk.css" />
<script type="text/javascript" src="<%=request.getContextPath()%>/scripts/disk.js"></script>
<div id="disk_fileTableDiv">
    <table id="disk_fileTable" cellspacing="0px">
        <tr>
            <th id="disk_fileName">File Name</th>
            <th id="disk_uploadDate">Upload Date</th>
            <th id="disk_fileSize">File Size</th>
            <th id="disk_download">Download</th></tr>
    </table>
</div>
<%@ include file="/WEB-INF/commonPages/footer.jsp"%>
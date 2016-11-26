<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="zh-CN">

<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Reset Password</title>
</head>

<body>
<form method="post" action="${contextPath}/authentication/resetPasswd">
    <input type="hidden" name="userId" value="${userId}">
    <input type="hidden" name="resetToken" value="${resetToken}">
    新密码:<input type="password" id="pwd" name="pwd">
    确认密码:<input type="password" id="confirm_pwd">
    <input type="submit">
</form>
</body>

</html>

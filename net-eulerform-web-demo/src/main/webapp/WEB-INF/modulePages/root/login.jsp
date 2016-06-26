<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="zh-CN">

<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Bootstrap 101 Template</title>

    <!-- Bootstrap -->
    <link href="htmls/bootstrap-3.3.5/css/bootstrap.min.css" rel="stylesheet">
    <link href="htmls/bootstrap-3.3.5/css/global.css" rel="stylesheet">
    <link href="htmls/bootstrap-3.3.5/css/login.css" rel="stylesheet">
</head>

<body>
    <div class="site-wrapper">

        <div class="site-wrapper-inner">
            <div class="web-title">
                <img alt="Brand" src="htmls/Euler-Formula.png" width="300px" height="75px">
            </div>

            <div class="login-form">
                <form method="post" action="<c:url value="/login" />">
                    <div class="form-group">
                        <input type="username" name="username" class="form-control" id="exampleInputEmail1" placeholder="Username">
                    </div>
                    <div class="form-group">
                        <input type="password" name="password" class="form-control" id="exampleInputPassword1" placeholder="Password">
                    </div>
                    <div class="form-group button-group">
                        <span><button 
                                type="submit" class="btn btn-success">Sign in</button></span><span><button 
                                type="button" class="btn btn-info">Sign up</button></span>
                    </div>
                </form>
            </div>
        </div>


        <footer class="navbar navbar-inverse navbar-fixed-bottom footer">
            Version 0.0.2 &copy;2016&nbsp;cFrost&nbsp;<a id="icp" href="http://www.miitbeian.gov.cn" target="_Blank">粤ICP备15054669号</a>
        </footer>
    </div>

    <!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
    <script src="htmls/bootstrap-3.3.5/js/jquery-3.0.0.min.js"></script>
    <!-- Include all compiled plugins (below), or include individual files as needed -->
    <script src="htmls/bootstrap-3.3.5/js/bootstrap.min.js"></script>
</body>

</html>



<%-- <!DOCTYPE html>
<html>
    <head>
        <title>Log In</title>
    </head>
    <body>
        <h2>Log In</h2>
        <c:if test="${param.containsKey('error')}">
            <b>Login failed. Please try again.</b><br /><br />
        </c:if>
        <c:if test="${param.containsKey('loggedOut')}">
            <b>You are now logged out.</b><br /><br />
        </c:if>
        <form method="post" action="<c:url value="/login" />">
            Username<br />
            <input type="text" name="username" /><br /><br />

            Password<br />
            <input type="password" name="password" /><br /><br />

            <input type="submit" name="Log In" />
        </form>
        <a href="<c:url value="/logout"/>">logout</a>
    </body>
</html> --%>

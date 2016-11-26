<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.text.SimpleDateFormat,java.util.Date" %>
<!DOCTYPE html>
<html lang="zh-CN">

<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${global.singin}</title>

    <link href="${contextPath}/resources/bootstrap-3.3.5/css/bootstrap.min.css" rel="stylesheet">
    <%-- <link href="${contextPath}/resources/bootstrap-3.3.5/css/bootstrap-theme.min.css" rel="stylesheet"> --%>
    <link href="${contextPath}/resources/css/lib/icon.css" rel="stylesheet">
    <link href="${contextPath}/resources/bootstrap-3.3.5/local/global.css" rel="stylesheet">
    <link href="${contextPath}/resources/bootstrap-3.3.5/local/center-from.css" rel="stylesheet">
</head>

<body>
    <div class="wrapper">

        <div class="wrapper-inner">
            <div class="title-wrapper">
                <span class="demo-brand-200-50-fff"></span>
                <%-- <img alt="Brand" src="${contextPath}/resources/images/Euler-Formula-800_200-fff.png" width="300px" height="75px"> --%>
            </div>

            <div class="main-form-wrapper">
                <form method="post" id="signin-form" class="main-form" action="signin">
                    <div class="form-group">
                        <input type="text" name="username" class="form-control" id="username" placeholder="Username">
                    </div>
                    <div class="form-group">
                        <input type="password" name="password" class="form-control" id="password" placeholder="Password">
                    </div>
                    <div class="form-group signin-group" style="display:none;">
                        <input type="password" name="" class="form-control" id="confirm-pass" placeholder="Confirm Password">
                    </div>
                    <div class="form-group signin-group" style="display:none;">
                        <input type="text" name="fullName" class="form-control" id="fullName" placeholder="Your Name">
                    </div>
                    <div class="form-group signin-group" style="display:none;">
                        <input type="email" name="email" class="form-control" id="email" placeholder="E-Mail Address">
                    </div>
                    <div class="form-group signin-group" style="display:none;">
                        <input type="tel" name="mobile" class="form-control" id="mobile" placeholder="Cellphone Number">
                    </div>
                    <div id="forgotpasswd" class="checkbox">
                        <span><a href="forgotpasswd">Forgot password?</a></span><span><label>
                            <input type="checkbox"> Remember me
                        </label></span>
                    </div>
                    <div class="form-group button-group">
                        <span><button 
                                type="submit" class="btn btn-success">Sign in</button></span><span><button 
                                type="button" id="singup-btn" class="btn btn-default" onClick="changeToSignupForm()">Sign Up</button></span>
                    </div>
                </form>
            </div>
            
            <div class="info-wrapper">
                <c:if test="${param.containsKey('error')}">
                    <b>Login failed. Please try again.</b>
                </c:if>
                <c:if test="${param.containsKey('loggedOut')}">
                    <b>You are now logged out.</b>
                </c:if>
            </div>
        </div>

        <footer class="navbar navbar-inverse navbar-fixed-bottom footer-wrapper">Powered&nbsp;by&nbsp;eulerframework&nbsp;${eulerframeworkVersion}&nbsp;&copy;<%=new SimpleDateFormat("yyyy").format(new Date()) %>&nbsp;cFrost&nbsp;<a id="icp" href="http://www.miitbeian.gov.cn" target="_Blank">粤ICP备15054669号</a></footer>
    </div>

    <script src="${contextPath}/resources/bootstrap-3.3.5/js/jquery-3.0.0.min.js"></script>
    <script src="${contextPath}/resources/bootstrap-3.3.5/js/bootstrap.min.js"></script>
    
    <script>
    
        function changeToSignupForm() {
            $('.signin-group').show();
            $('#forgotpasswd').hide();
            $('#singup-btn').attr("onClick", "signUp()");
            return;
            
        }
    
        function signUp(){
            
            var username = $('#username').val();
            var password = $('#password').val();
            var fullName = $('#fullName').val();
            var email = $('#email').val();
            var mobile = $('#mobile').val();
            var confirmPass = $('#confirm-pass').val();
            if(password != confirmPass) {
                alert("Passwords must match");
                return;
            }
            
            var data = {};
            data.username = username;
            data.password = password;
            data.fullName = fullName;
            data.email = email;
            data.mobile = mobile;
            $.ajax({
                url:'signup',
                type:'POST',
                async:true,
                data: data,
                error:function(XMLHttpRequest, textStatus, errorThrown) {
                    alert(XMLHttpRequest.responseText);
                },
                success:function(data, textStatus) {
                    $('#signin-form').submit();
                }
            });
        }
    
    </script>
</body>

</html>

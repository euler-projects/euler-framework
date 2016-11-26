<%--@elvariable id="_csrf" type="org.springframework.security.web.csrf.CsrfToken"--%>
<%--@elvariable id="authorizationRequest" type="org.springframework.security.oauth2.provider.AuthorizationRequest"--%>
<spring:message code="title.oauthAuthorize" var="oauthAuthorize"/>
<template:basic htmlTitle="${oauthAuthorize}" bodyTitle="${oauthAuthorize}">
    <spring:message code="oauth.authorizeThirdParty">
        <spring:argument value="${authorizationRequest.clientId}"/>
    </spring:message><br>
    <br>
    <form method="post" action="${contextPath}/oauth/authorize">
        <input type="hidden" name="${_csrf.parameterName}"
               value="${_csrf.token}">
        <input type="hidden" name="user_oauth_approval" value="false">
        <input type="button" value="${euler:i18n('oauth.authorizeApprove')}"
               onclick="this.form.user_oauth_approval.value = 'true'; this.form.submit();">
        <input type="button" value="${euler:i18n('oauth.authorizeDeny')}"
               onclick="this.form.user_oauth_approval.value = 'false'; this.form.submit();">
    </form>
</template:basic>

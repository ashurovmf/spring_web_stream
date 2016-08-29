<%@page session="true" language="java" contentType="text/html"
    pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Greetings page</title>
</head>
<body>
    <h1>${title}</h1>
    <p>${message}</p>

    <sec:authorize access="hasRole('ROLE_USER')">
    		<!-- For login user -->
    		<c:url value="/logout" var="logoutUrl" />
    		<form action="${logoutUrl}" method="post" id="logoutForm">
    			<input type="hidden" name="${_csrf.parameterName}"
    				value="${_csrf.token}" />
    		</form>
    		<script>
    			function formSubmit() {
    				document.getElementById("logoutForm").submit();
    			}
    		</script>

    		<c:if test="${pageContext.request.userPrincipal.name != null}">
            	<h2>
            		Welcome : ${pageContext.request.userPrincipal.name} | <a
            			href="javascript:formSubmit()"> Logout</a>
            	</h2>
            </c:if>
    </sec:authorize>

</body>
</html>
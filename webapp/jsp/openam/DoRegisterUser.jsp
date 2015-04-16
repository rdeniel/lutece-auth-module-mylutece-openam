<%@page import="fr.paris.lutece.portal.web.LocalVariables" trimDirectiveWhitespaces="true"%>
<%@ page errorPage="../site/ErrorPagePortal.jsp" %>
<%@ page import="fr.paris.lutece.portal.service.security.SecurityService" %>
<%@ page import="fr.paris.lutece.portal.service.spring.SpringContextService" %>

<%@ page import="fr.paris.lutece.portal.service.security.LuteceUser" %>


<%@ page import="fr.paris.lutece.plugins.mylutece.modules.openam.authentication.OpenamAuthentication" %>

<jsp:useBean id="myLuteceOpenamXPage" scope="session" class="fr.paris.lutece.plugins.mylutece.modules.openam.web.MyLuteceOpenamXPage" />
<%
	
	OpenamAuthentication openamAuthentication = (OpenamAuthentication) SpringContextService.getBean( 
        "mylutece-openam.authentication" );
	LuteceUser user = openamAuthentication.getHttpAuthenticatedUser( request );

	if ( user != null )
	{
    	SecurityService.getInstance(  ).registerUser( request, user );
	}
	
	LocalVariables.setLocal( config, request, response );
	
	response.sendRedirect( "../site/plugins/mylutece/DoMyLuteceLogin.jsp?auth_provider=mylutece-openam");
	


%>
	
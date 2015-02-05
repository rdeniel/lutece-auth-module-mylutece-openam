<%@page import="fr.paris.lutece.portal.web.LocalVariables" trimDirectiveWhitespaces="true"%>
<jsp:useBean id="myLuteceOpenamXPage" scope="session" class="fr.paris.lutece.plugins.mylutece.modules.openam.web.MyLuteceOpenamXPage" />
<%
	LocalVariables.setLocal( config, request, response );
%>
<%= myLuteceOpenamXPage.isUserAuthenticated(request) %>
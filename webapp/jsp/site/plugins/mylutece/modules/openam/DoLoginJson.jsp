<%@page import="fr.paris.lutece.portal.web.LocalVariables" trimDirectiveWhitespaces="true"%>
<jsp:useBean id="myLuteceOpenamXPage" scope="session" class="fr.paris.lutece.plugins.mylutece.modules.openam.web.MyLuteceOpenamXPage" />
<% 
	//Required by JSR168 portlets (added in v1.2)
	LocalVariables.setLocal( config, request, response );
	try
	{
		String strContent = myLuteceOpenamXPage.doLogin(request);
	
		out.print( strContent );
		out.flush();
	}
	finally
	    {
	        LocalVariables.setLocal( null, null, null );
	    }
%>



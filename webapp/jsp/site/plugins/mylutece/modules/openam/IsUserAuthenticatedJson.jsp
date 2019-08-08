<jsp:useBean id="myLuteceOpenamXPage" scope="session" class="fr.paris.lutece.plugins.mylutece.modules.openam.web.MyLuteceOpenamXPage" />
<%= myLuteceOpenamXPage.isUserAuthenticated(request) %>
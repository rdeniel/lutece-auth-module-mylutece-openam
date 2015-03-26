package fr.paris.lutece.plugins.mylutece.modules.openam.service;


public interface IOpenamLuteceUserSessionService {
	
	
	 boolean isLuteceUserUpToDate(String strSession);
	
	 void addLuteceUserSession(String strLuteceUserName,String strSession);
	 
	 void removeLuteceUserSession(String strSession);
	
	 void notifyLuteceUserUpdating(String strLuteceUserName);
}

package fr.paris.lutece.plugins.mylutece.modules.openam.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import fr.paris.lutece.portal.service.spring.SpringContextService;

public class OpenamLuteceUserSessionService implements IOpenamLuteceUserSessionService{

	
	private static final String BEAN_LUTECE_USER_SESSION_SERVICE="mylutece-openam.openamLuteceUserSessionService";
	
	private static IOpenamLuteceUserSessionService _singleton;
	private static Map<String,OpenamLuteceUserSession> _hashSession;
	private static Map<String,Set<String>> _hashLuteceUserName;
	
	
	
	
	public static IOpenamLuteceUserSessionService getInstance()
	{
		if(_singleton == null)
		{
			_singleton = SpringContextService
				.getBean(BEAN_LUTECE_USER_SESSION_SERVICE);
			_hashSession=new HashMap<String,OpenamLuteceUserSession>();
			_hashLuteceUserName=new HashMap<String,Set<String>>();
		
		}
		return _singleton;
	}
	
	
	
	public boolean isLuteceUserUpToDate(String strSession)
	{	
	
		if( _hashSession.containsKey(strSession) &&  !_hashSession.get(strSession).isUpToDate())
		{
			_hashSession.get(strSession).setUpToDate(true);
			return false;
			
		}
		return true;
		
	}
	
	
	public void addLuteceUserSession(String strLuteceUserName,String strSession)
	{
		
		if( !_hashLuteceUserName.containsKey(strLuteceUserName))
		{
			_hashLuteceUserName.put(strLuteceUserName,new HashSet<String>());
		}
		_hashLuteceUserName.get(strLuteceUserName).add(strSession);
		_hashSession.put(strSession, new OpenamLuteceUserSession(strSession, strLuteceUserName, true));
	}
	
	public void removeLuteceUserSession(String strSession)
	{
		if( _hashSession.containsKey(strSession) )
		{
			String strLuteceUserName=_hashSession.get(strSession).getLuteceUserName();	
			_hashSession.remove(strSession);
			if(_hashLuteceUserName.containsKey(strLuteceUserName))
			{
				_hashLuteceUserName.get(strLuteceUserName).remove(strSession);
				if(_hashLuteceUserName.get(strLuteceUserName).isEmpty())
				{
					_hashLuteceUserName.remove(strLuteceUserName);
				}
			}
			
		}
	}
	
	
	public void notifyLuteceUserUpdating(String strLuteceUserName)
	{
		if( _hashLuteceUserName.containsKey(strLuteceUserName))
		{
			
			Set<String> setSession=_hashLuteceUserName.get(strLuteceUserName);
			
			for(String strSession:setSession)
			{
				
				if(_hashSession.containsKey(strSession))
				{
					
					_hashSession.get(strSession).setUpToDate(false);
				}
			}
		}
		
	}
	
			
	
}

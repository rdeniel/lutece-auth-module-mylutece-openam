package fr.paris.lutece.plugins.mylutece.modules.openam.service;

public class OpenamLuteceUserSession {

	private String _strIdSession;
	private String _strLuteceUserName;
	private boolean  _bUpToDate;
	
	
	public String getIdSession() {
		return _strIdSession;
	}
	public void setIdSession(String _strIdSession) {
		this._strIdSession = _strIdSession;
	}
	public String getLuteceUserName() {
		return _strLuteceUserName;
	}
	public void setLuteceUserName(String _strLuteceUserName) {
		this._strLuteceUserName = _strLuteceUserName;
	}
	public boolean isUpToDate() {
		return _bUpToDate;
	}
	public void setUpToDate(boolean _bUpToDate) {
		this._bUpToDate = _bUpToDate;
	} 
	
	
	public OpenamLuteceUserSession(String strIdSession,String strLuteceUserName,boolean bUpToDate)
	{
		_strIdSession=strIdSession;
		_strLuteceUserName=strLuteceUserName;
		_bUpToDate=bUpToDate;
	}
	
}

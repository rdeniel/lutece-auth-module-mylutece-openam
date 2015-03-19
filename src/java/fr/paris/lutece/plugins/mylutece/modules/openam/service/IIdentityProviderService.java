package fr.paris.lutece.plugins.mylutece.modules.openam.service;

import java.util.Map;

/**
 * 
 * IIdentityProviderService
 *
 */
public interface IIdentityProviderService {

	
	/**
	 * 
	 * @param strName the guid of the user
	 * @param attributeUserMapping a map wich contains Key -> The Name of the attribute in the identity provider
	 * 												 
	 * @return a Map containing the users informations
	 */
	public Map<String,String> getIdentityInformations(String strName );
}

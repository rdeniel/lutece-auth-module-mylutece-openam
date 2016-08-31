/*
 * Copyright (c) 2002-2014, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.mylutece.modules.openam.service;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import fr.paris.lutece.plugins.mylutece.authentication.MultiLuteceAuthentication;
import fr.paris.lutece.plugins.mylutece.modules.openam.authentication.OpenamAuthentication;
import fr.paris.lutece.plugins.mylutece.modules.openam.authentication.OpenamUser;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;


/**
 *
 * OpenamService
 */
public final class OpenamService
{
    public static final String ERROR_ALREADY_SUBSCRIBE = "ALREADY_SUBSCRIBE";
    public static final String ERROR_DURING_SUBSCRIBE = "ERROR_DURING_SUBSCRIBE";
    private static final String AUTHENTICATION_BEAN_NAME = "mylutece-openam.authentication";
    private static boolean _bAgentEnable;
    private static OpenamService _singleton;
    private static final String PROPERTY_AGENT_ENABLE = "mylutece-openam.agentEnable";
    private static final String PROPERTY_COOKIE_OPENAM_NAME = "mylutece-openam.cookieName";
    private static final String PROPERTY_COOKIE_OPENAM_DOMAIN = "mylutece-openam.cookieDomain";
    private static final String PROPERTY_COOKIE_OPENAM_PATH = "mylutece-openam.cookiePath";
    private static final String PROPERTY_COOKIE_OPENAM_MAX_AGE = "mylutece-openam.cookieMaxAge";
    private static final String PROPERTY_COOKIE_OPENAM_MAX_SECURE = "mylutece-openam.cookieSecure";
    public static final String PROPERTY_USER_KEY_NAME = "mylutece-openam.attributeKeyUsername";
    public static final String PROPERTY_USER_MAPPING_ATTRIBUTES = "mylutece-openam.userMappingAttributes";
    public static final String CONSTANT_LUTECE_USER_PROPERTIES_PATH = "mylutece-openam.attribute";
    private static String COOKIE_OPENAM_NAME;
    private static String COOKIE_OPENAM_DOMAIN;
    private static String COOKIE_OPENAM_PATH;
    private static int COOKIE_OPENAM_MAX_AGE;
    private static boolean COOKIE_OPENAM_SECURE;
    private static final String SEPARATOR = ",";
    private static Map<String, List<String>> ATTRIBUTE_USER_MAPPING;
    private static String ATTRIBUTE_USER_KEY_NAME;

    /**
     * Empty constructor
     */
    private OpenamService(  )
    {
        // nothing
    }

    /**
     * Gets the instance
     *
     * @return the instance
     */
    public static OpenamService getInstance(  )
    {
        if ( _singleton == null )
        {
            _singleton = new OpenamService(  );
            COOKIE_OPENAM_NAME = AppPropertiesService.getProperty( PROPERTY_COOKIE_OPENAM_NAME );
            COOKIE_OPENAM_DOMAIN = AppPropertiesService.getProperty( PROPERTY_COOKIE_OPENAM_DOMAIN );
            COOKIE_OPENAM_PATH = AppPropertiesService.getProperty( PROPERTY_COOKIE_OPENAM_PATH );
            COOKIE_OPENAM_MAX_AGE = AppPropertiesService.getPropertyInt( PROPERTY_COOKIE_OPENAM_MAX_AGE, 60 * 30 );
            COOKIE_OPENAM_SECURE = AppPropertiesService.getPropertyBoolean( PROPERTY_COOKIE_OPENAM_MAX_SECURE, true );

            ATTRIBUTE_USER_KEY_NAME = AppPropertiesService.getProperty( PROPERTY_USER_KEY_NAME );

            String strUserMappingAttributes = AppPropertiesService.getProperty( PROPERTY_USER_MAPPING_ATTRIBUTES );
            ATTRIBUTE_USER_MAPPING = new HashMap<String, List<String>>(  );

            if ( StringUtils.isNotBlank( strUserMappingAttributes ) )
            {
                String[] tabUserProperties = strUserMappingAttributes.split( SEPARATOR );
                String userProperties;

                for ( int i = 0; i < tabUserProperties.length; i++ )
                {
                    userProperties = AppPropertiesService.getProperty( CONSTANT_LUTECE_USER_PROPERTIES_PATH + "." +
                            tabUserProperties[i] );

                    if ( StringUtils.isNotBlank( userProperties ) )
                    {
                    	if(!ATTRIBUTE_USER_MAPPING.containsKey(userProperties))
                    	{
                    		ATTRIBUTE_USER_MAPPING.put(userProperties,new ArrayList<String>());
                    	}
                    	ATTRIBUTE_USER_MAPPING.get(userProperties).add(tabUserProperties[i] );
                		
                    }
                }
            }
        }

        return _singleton;
    }

    /**
     * Inits plugin. Registers authentication
     */
    public void init(  )
    {
        _bAgentEnable = AppPropertiesService.getPropertyBoolean( PROPERTY_AGENT_ENABLE, false );

        OpenamAuthentication authentication = (OpenamAuthentication) SpringContextService.getPluginBean( OpenamPlugin.PLUGIN_NAME,
                AUTHENTICATION_BEAN_NAME );

        if ( authentication != null )
        {
            MultiLuteceAuthentication.registerAuthentication( authentication );
        }
        else
        {
            OpenamAPI._logger.error( 
                "OpenamAuthentication not found, please check your openam_context.xml configuration" );
        }
    }

    /**
     * Process login
     *
     * @param request
     *            The HTTP request
     * @param strUserName
     *            The user's name
     * @param strUserPassword
     *            The user's password
     * @param openamAuthentication
     *            The authentication
     * @return The LuteceUser
     */
    public OpenamUser doLogin( HttpServletRequest request, String strUserName, String strUserPassword,
        OpenamAuthentication openamAuthentication ) throws OpenamAuthenticationAgentException
    {
        String strTokenId;
        OpenamUser user = null;

        Map<String, String> headerUserInformations = null;

        if ( isAgentEnabled(  ) )
        {
            headerUserInformations = getUserInformationInHeaderRequest( request );

            if ( ( headerUserInformations != null ) && !headerUserInformations.isEmpty(  ) &&
                    headerUserInformations.containsKey( ATTRIBUTE_USER_KEY_NAME ) )
            {
                user = new OpenamUser( headerUserInformations.get( ATTRIBUTE_USER_KEY_NAME ), openamAuthentication,
                        getConnectionCookie( request ) );
                addUserAttributes( headerUserInformations, user );
            }
            else
            {
                throw new OpenamAuthenticationAgentException(  );
            }
        }

        else
        {
            try
            {
                strTokenId = OpenamAPIService.doLogin( strUserName, strUserPassword );

                if ( strTokenId != null )
                {
                    Map<String, String> userInformations = OpenamAPIService.getUserInformations( strTokenId,
                            strUserName,COOKIE_OPENAM_NAME, ATTRIBUTE_USER_MAPPING, ATTRIBUTE_USER_KEY_NAME );

                    // test contains guid
                    if ( ( userInformations != null ) && userInformations.containsKey( ATTRIBUTE_USER_KEY_NAME ) )
                    {
                        user = new OpenamUser( userInformations.get( ATTRIBUTE_USER_KEY_NAME ), openamAuthentication,
                                strTokenId );
                        addUserAttributes( userInformations, user );
                    }
                }
            }
            catch ( OpenamAPIException ex )
            {
                OpenamAPI._logger.error( "Error During Login Openam" + ex.getMessage(  ) );
            }
        }

        return user;
    }

    /**
     * Logout to openam
     *
     * @param user
     *            the User
     */
    public void doLogout( OpenamUser user )
    {
        try
        {
            OpenamAPIService.doDisconnect(COOKIE_OPENAM_NAME, user.getSubjectId(  ) );
        }
        catch ( OpenamAPIException ex )
        {
            OpenamAPI._logger.error( "Error During Logout Openam" + ex.getMessage(  ) );
        }
    }

    /**
     * Gets the authenticated user
     *
     * @param request
     *            The HTTP request
     * @param openamAuthentication
     *            The Authentication
     * @return The LuteceUser
     */
    public OpenamUser getHttpAuthenticatedUser( HttpServletRequest request, OpenamAuthentication openamAuthentication )
    {
        OpenamUser user = null;
        Map<String, String> headerUserInformations = null;

        if ( isAgentEnabled(  ) )
        {
            headerUserInformations = getUserInformationInHeaderRequest( request );
        }

        if ( ( headerUserInformations != null ) && !headerUserInformations.isEmpty(  ) &&
                headerUserInformations.containsKey( ATTRIBUTE_USER_KEY_NAME ) )
        {
            user = new OpenamUser( headerUserInformations.get( ATTRIBUTE_USER_KEY_NAME ), openamAuthentication,
                    getConnectionCookie( request ) );
            addUserAttributes( headerUserInformations, user );
        }
        else
        {
            String strTokenId = getConnectionCookie( request );

            if ( !StringUtils.isEmpty( strTokenId ) )
            {
                try
                {
                    String strUserId = OpenamAPIService.isValidate( strTokenId );

                    if ( strUserId != null )
                    {
                        Map<String, String> userInformations = OpenamAPIService.getUserInformations( strTokenId,
                                strUserId,COOKIE_OPENAM_NAME, ATTRIBUTE_USER_MAPPING, ATTRIBUTE_USER_KEY_NAME );

                        // test contains guid
                        if ( ( userInformations != null ) && userInformations.containsKey( ATTRIBUTE_USER_KEY_NAME ) )
                        {
                            user = new OpenamUser( userInformations.get( ATTRIBUTE_USER_KEY_NAME ),
                                    openamAuthentication, strTokenId );
                            addUserAttributes( userInformations, user );
                        }
                    }
                }
                catch ( OpenamAPIException ex )
                {
                    OpenamAPI._logger.error( "Error getting Openam user Informations" + ex.getMessage(  ) );
                }
            }
        }

        return user;
    }

    /**
     * Extract the value of the connection cookie
     *
     * @param request
     *            The HTTP request
     * @return The cookie's value
     */
    public String getConnectionCookie( HttpServletRequest request )
    {
        Cookie[] cookies = request.getCookies(  );
        String strOpenamCookie = null;

        if ( cookies != null )
        {
            for ( Cookie cookie : cookies )
            {
                if ( cookie.getName(  ).equals( COOKIE_OPENAM_NAME ) )
                {
                    strOpenamCookie = cookie.getValue(  );
                    OpenamAPI._logger.debug( "getHttpAuthenticatedUser : cookie '" + COOKIE_OPENAM_NAME +
                        "' found - value=" + strOpenamCookie );
                }
            }
        }

        return strOpenamCookie;
    }
    
    /**
     * true if the token is validated
     * @param strTokenId the token id
     * @return true if the token is validated
     */
    public boolean isTokenValidated(String strTokenId )
    {
	
	  if ( !StringUtils.isEmpty( strTokenId ) )
          {
              try
              {
                  String strUserId = OpenamAPIService.isValidate( strTokenId );
                  return !StringUtils.isEmpty(strUserId);
                  
              }
              catch ( OpenamAPIException ex )
              {
                  OpenamAPI._logger.error( "Error getting Openam user Informations" + ex.getMessage(  ) );
              }
          }
                  
	return false;
	
    }

    /**
     * set a paris connect cokkie in the HttpServletResponse
     *
     * @param strPCUID
     *            the user PCUID
     * @param response
     *            The HTTP response
     */
    public void setConnectionCookie( String strPCUID, HttpServletResponse response )
    {
        // set a connexion cookie to let the user access other PC Services
        // without sign in
        Cookie openamCookie = new Cookie( COOKIE_OPENAM_NAME, strPCUID );
        openamCookie.setDomain( COOKIE_OPENAM_DOMAIN );
        openamCookie.setSecure( COOKIE_OPENAM_SECURE );
        openamCookie.setMaxAge( COOKIE_OPENAM_MAX_AGE );
        openamCookie.setPath( COOKIE_OPENAM_PATH );

        response.addCookie( openamCookie );
    }

    /**
     * set a paris connect cokkie in the HttpServletResponse
     *
     * @param strPCUID
     *            the user PCUID
     * @param response
     *            The HTTP response
     */
    public void removeConnectionCookie( HttpServletResponse response )
    {
        // remove  openam cookie using the setMaxAgeParameters
        Cookie openamCookie = new Cookie( COOKIE_OPENAM_NAME, null );
        openamCookie.setDomain( COOKIE_OPENAM_DOMAIN );
        openamCookie.setSecure( COOKIE_OPENAM_SECURE );
        openamCookie.setMaxAge( 0 );
        openamCookie.setPath( COOKIE_OPENAM_PATH );
        response.addCookie( openamCookie );
    }

    /**
     * Fill user's data
     *
     * @param user
     *            The User
     * @param strUserData
     *            Data in JSON format
     */
    private void addUserAttributes( Map<String, String> userInformations, OpenamUser user )
    {
        for ( Entry<String, String> entry : userInformations.entrySet(  ) )
        {
            if ( ATTRIBUTE_USER_MAPPING.containsKey( entry.getKey(  ) ) )
            {
            	for(String strUserInfo:ATTRIBUTE_USER_MAPPING.get( entry.getKey(  )))
            	{
            		user.setUserInfo(strUserInfo, entry.getValue(  ) );
            	}
            }
        }

        Map<String, String> mapIdentitiesInformations;
        //Add Identities Informations
        mapIdentitiesInformations = getIdentityInformations( user.getName(  ), ATTRIBUTE_USER_MAPPING );

        if ( mapIdentitiesInformations != null )
        {
            for ( Entry<String, String> entry : mapIdentitiesInformations.entrySet(  ) )
            {
            	if ( ATTRIBUTE_USER_MAPPING.containsKey( entry.getKey(  ) ) )
                {
                	for(String strUserInfo:ATTRIBUTE_USER_MAPPING.get( entry.getKey(  )))
                	{
                		user.setUserInfo(strUserInfo, entry.getValue(  ) );
                	}
                }
            }

            userInformations.putAll( mapIdentitiesInformations );
        }
    }

    /**
     *
     * @return true if the user Agent is enabled
     */
    private boolean isAgentEnabled(  )
    {
        return _bAgentEnable;
    }

    private Map<String, String> getUserInformationInHeaderRequest( HttpServletRequest request )
    {
        Map<String, String> userInformations = new HashMap<String, String>(  );
        Enumeration headerNames = request.getHeaderNames(  );

        String strKey;

        while ( headerNames.hasMoreElements(  ) )
        {
            strKey = (String) headerNames.nextElement(  );

            if ( ATTRIBUTE_USER_MAPPING.containsKey( strKey ) || ATTRIBUTE_USER_KEY_NAME.equals( strKey ) )
            {
                userInformations.put( strKey, request.getHeader( strKey ) );
            }
        }

        if ( OpenamAPI._bDebug )
        {
            headerNames = request.getHeaderNames(  );
            OpenamAPI._logger.debug( "Openam Headers Informations" );

            while ( headerNames.hasMoreElements(  ) )
            {
                strKey = (String) headerNames.nextElement(  );
                OpenamAPI._logger.debug( strKey + "=" + request.getHeader( strKey ) );
            }
        }

        return userInformations;
    }

    public Map<String, String> getIdentityInformations( String strName, Map<String, List<String>> attributeUserMapping )
    {
        for ( IIdentityProviderService identityProviderService : SpringContextService.getBeansOfType( 
                IIdentityProviderService.class ) )
        {
            return identityProviderService.getIdentityInformations( strName );
        }

        return null;
    }
}

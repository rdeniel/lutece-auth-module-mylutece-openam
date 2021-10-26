/*
 * Copyright (c) 2002-2021, City of Paris
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
package fr.paris.lutece.plugins.mylutece.modules.openam.authentication;

import fr.paris.lutece.plugins.mylutece.authentication.PortalAuthentication;
import fr.paris.lutece.plugins.mylutece.modules.openam.service.OpenamAuthenticationAgentException;
import fr.paris.lutece.plugins.mylutece.modules.openam.service.OpenamLuteceUserSessionService;
import fr.paris.lutece.plugins.mylutece.modules.openam.service.OpenamPlugin;
import fr.paris.lutece.plugins.mylutece.modules.openam.service.OpenamService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.message.SiteMessage;
import fr.paris.lutece.portal.service.message.SiteMessageException;
import fr.paris.lutece.portal.service.message.SiteMessageService;
import fr.paris.lutece.portal.service.security.LoginRedirectException;
import fr.paris.lutece.portal.service.security.LuteceUser;
import fr.paris.lutece.portal.service.security.SecurityService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

import org.apache.commons.lang.StringUtils;

import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import javax.servlet.http.HttpServletRequest;

/**
 * OpenamAuthentication Authentication
 */
public class OpenamAuthentication extends PortalAuthentication
{
    private static final String AUTH_SERVICE_NAME = "Lutece Openam Authentication Service";
    private static final String URL_ICON = "images/local/skin/plugins/mylutece/modules/openam/openam.png";
    private static final String PROPERTY_CREATE_ACCOUNT_URL = "mylutece-openam.url.createAccount.page";
    private static final String PROPERTY_LOST_PASSWORD_URL = "mylutece-openam.url.lostPassword.page";
    private static final String PROPERTY_VIEW_ACCOUNT_URL = "mylutece-openam.url.viewAccount.page";
    private static final String PROPERTY_MESSAGE_FAILED_LOGIN = "module.mylutece.openam.message.error.failedLogin";
    private static final String PROPERTY_MESSAGE_ERROR_LOGIN_AGENT = "module.mylutece.openam.message.error.loginAgent";
    public static final String PROPERTY_BACK_URL_ERROR_AGENT = "mylutece-openam.backUrlErrorAgent";
    public static final String PROPERTY_URL_ERROR_LOGIN_AGENT = "mylutece-openam.urlErrorLoginAgent";
    public static final String PROPERTY_DEFINE_AS_EXTERNAL_AUTHENTICATION = "mylutece-openam.defineAsExternalAuthenticationModule";

    private static final String CONSTANT_HTTP = "http://";
    private static final String CONSTANT_HTTPS = "https://";

    /** Lutece User Attributs */

    /**
     * Constructor
     */
    public OpenamAuthentication( )
    {
    }

    /**
     * Gets the Authentification service name
     * 
     * @return The name of the authentication service
     */
    @Override
    public String getAuthServiceName( )
    {
        return AUTH_SERVICE_NAME;
    }

    /**
     * Gets the Authentification type
     * 
     * @param request
     *            The HTTP request
     * @return The type of authentication
     */
    @Override
    public String getAuthType( HttpServletRequest request )
    {
        return HttpServletRequest.BASIC_AUTH;
    }

    /**
     * This methods checks the login info in the base repository
     * 
     * @param strUserName
     *            The username
     * @param strUserPassword
     *            The password
     * @param request
     *            The HTTP request
     * @return A LuteceUser object corresponding to the login
     * @throws LoginException
     *             The LoginException
     */
    @Override
    public LuteceUser login( String strUserName, String strUserPassword, HttpServletRequest request ) throws LoginException, LoginRedirectException
    {
        LuteceUser user;

        if ( SecurityService.getInstance( ).getRegisteredUser( request ) == null )
        {
            try
            {
                user = OpenamService.getInstance( ).doLogin( request, strUserName, strUserPassword, this );
            }
            catch( OpenamAuthenticationAgentException e )
            {
                String strUrlErrorLoginAgent = AppPropertiesService.getProperty( PROPERTY_URL_ERROR_LOGIN_AGENT );
                String strBackUrlErrorAgent = AppPropertiesService.getProperty( PROPERTY_BACK_URL_ERROR_AGENT );

                if ( StringUtils.isEmpty( strUrlErrorLoginAgent ) )
                {
                    try
                    {
                        SiteMessageService.setMessage( request, PROPERTY_MESSAGE_ERROR_LOGIN_AGENT, null, " ", null, "", SiteMessage.TYPE_STOP, null,
                                strBackUrlErrorAgent );
                    }
                    catch( SiteMessageException lme )
                    {
                        strUrlErrorLoginAgent = AppPathService.getSiteMessageUrl( request );
                    }
                }

                if ( ( strUrlErrorLoginAgent == null )
                        || ( !strUrlErrorLoginAgent.startsWith( CONSTANT_HTTP ) && !strUrlErrorLoginAgent.startsWith( CONSTANT_HTTPS ) ) )
                {
                    strUrlErrorLoginAgent = AppPathService.getBaseUrl( request ) + strUrlErrorLoginAgent;
                }

                LoginRedirectException ex = new LoginRedirectException( strUrlErrorLoginAgent );
                throw ex;
            }

            if ( user == null )
            {
                throw new FailedLoginException( I18nService.getLocalizedString( PROPERTY_MESSAGE_FAILED_LOGIN, request.getLocale( ) ) );
            }

            // add Openam LuteceUser session
            OpenamLuteceUserSessionService.getInstance( ).addLuteceUserSession( user.getName( ), request.getSession( true ).getId( ) );
        }
        else
        {
            user = SecurityService.getInstance( ).getRegisteredUser( request );
        }

        return user;
    }

    /**
     * This methods logout the user
     * 
     * @param user
     *            The user
     */
    @Override
    public void logout( LuteceUser user )
    {
        OpenamService.getInstance( ).doLogout( (OpenamUser) user );
    }

    /**
     * This method returns an anonymous Lutece user
     * 
     * @return An anonymous Lutece user
     */
    @Override
    public LuteceUser getAnonymousUser( )
    {
        throw new java.lang.UnsupportedOperationException( "getAnonymousUser() is not implemented." );
    }

    /**
     * Checks that the current user is associated to a given role
     * 
     * @param user
     *            The user
     * @param request
     *            The HTTP request
     * @param strRole
     *            The role name
     * @return Returns true if the user is associated to the role, otherwise false
     */
    @Override
    public boolean isUserInRole( LuteceUser user, HttpServletRequest request, String strRole )
    {
        return request.isUserInRole( strRole );
    }

    /**
     * Indicate that the authentication uses only HttpRequest data to authenticate users (ex : Web Server authentication).
     * 
     * @return true if the authentication service authenticates users only with the Http Request, otherwise false.
     */
    public boolean isBasedOnHttpAuthentication( )
    {
        return true;
    }

    /**
     * Returns a Lutece user object if the user is already authenticated by Openam
     * 
     * @param request
     *            The HTTP request
     * @return Returns A Lutece User or null if there no user authenticated
     */
    @Override
    public LuteceUser getHttpAuthenticatedUser( HttpServletRequest request )
    {
        OpenamUser user = OpenamService.getInstance( ).getHttpAuthenticatedUser( request, this );

        if ( user != null )
        {
            // add Openam LuteceUser session
            OpenamLuteceUserSessionService.getInstance( ).addLuteceUserSession( user.getName( ), request.getSession( true ).getId( ) );
        }

        return user;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getIconUrl( )
    {
        return URL_ICON;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName( )
    {
        return OpenamPlugin.PLUGIN_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPluginName( )
    {
        return OpenamPlugin.PLUGIN_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNewAccountPageUrl( )
    {
        return AppPropertiesService.getProperty( PROPERTY_CREATE_ACCOUNT_URL );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLostPasswordPageUrl( )
    {
        return AppPropertiesService.getProperty( PROPERTY_LOST_PASSWORD_URL );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getViewAccountPageUrl( )
    {
        return AppPropertiesService.getProperty( PROPERTY_VIEW_ACCOUNT_URL );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isExternalAuthentication( )
    {
        return AppPropertiesService.getPropertyBoolean( PROPERTY_DEFINE_AS_EXTERNAL_AUTHENTICATION, false );
    }

}

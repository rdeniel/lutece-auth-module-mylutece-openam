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
package fr.paris.lutece.plugins.mylutece.modules.openam.web;

import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;

import fr.paris.lutece.plugins.mylutece.modules.openam.authentication.OpenamAuthentication;
import fr.paris.lutece.portal.service.security.LuteceUser;
import fr.paris.lutece.portal.service.security.SecurityService;
import fr.paris.lutece.portal.service.security.SecurityTokenService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.util.mvc.xpage.MVCApplication;
import fr.paris.lutece.portal.util.mvc.xpage.annotations.Controller;
import fr.paris.lutece.util.json.AbstractJsonResponse;
import fr.paris.lutece.util.json.JsonResponse;
import fr.paris.lutece.util.json.JsonUtil;


/**
 * MyLuteceParisConnectXPage
 *
 */
@Controller( xpageName = MyLuteceOpenamXPage.PAGE_MYLUTECE_PARIS_CONNECT, pageTitleI18nKey = "module.mylutece.parisconnect.xpage.myluteceParisConnect.pageTitle", pagePathI18nKey = "module.mylutece.parisconnect.xpage.myluteceParisConnect.pagePathLabel" )
public class MyLuteceOpenamXPage extends MVCApplication
{
    /**
     * Name of this application
     */
    public static final String PAGE_MYLUTECE_PARIS_CONNECT = "myluteceParisConnect";
    private static final long serialVersionUID = -4316691400124512414L;

    //Parameters
    private static final String PARAMETER_USERNAME = "username";
    private static final String PARAMETER_PASSWORD = "password";

    // Views
    private static final String VIEW_IS_USER_AUTHENTICATED = "isUserAuthenticatedJson";

    // Actions
    private static final String ACTION_DO_LOGIN_JSON = "doLoginJson";
    private static final String TOKEN_ACTION_LOGIN = "dologin";

    // Json ERROR CODE
    private static final String JSON_ERROR_AUTHENTICATION_NOT_ENABLE = "AUTHENTICATION_NOT_ENABLE";
    private static final String JSON_ERROR_LOGIN_ERROR = "LOGIN_ERROR";
    private OpenamAuthentication _openAmAuthentication = (OpenamAuthentication) SpringContextService.getBean( 
            "mylutece-openam.authentication" );

    /**
     * Check if the current user is authenticated
     * @param request The request
     * @return A JSON string  containing  true in the field result if the user is authenticated
     */
    public String isUserAuthenticated( HttpServletRequest request )
    {
        AbstractJsonResponse jsonResponse = null;

        LuteceUser user = null;

        if ( SecurityService.isAuthenticationEnable(  ) )
        {
            user = SecurityService.getInstance(  ).getRegisteredUser( request );

            if ( user != null )
            {
                jsonResponse = new JsonResponse( Boolean.TRUE );
            }
            else
            {
                jsonResponse = new JsonResponse( Boolean.FALSE );
            }
        }
        else
        {
            jsonResponse = new OpenamErrorJsonResponse( JSON_ERROR_AUTHENTICATION_NOT_ENABLE );
        }

        return JsonUtil.buildJsonResponse( jsonResponse );
    }

    /**
     * doLoginAuthentication
     * @param request The request
     * @return A JSON string  containing  true in the user is authenticated
     */
    public String doLogin( HttpServletRequest request )
    {
        String strUsername = request.getParameter( PARAMETER_USERNAME );
        String strPassword = request.getParameter( PARAMETER_PASSWORD );

        AbstractJsonResponse jsonResponse = null;

        LuteceUser user = null;

        if ( SecurityService.isAuthenticationEnable(  ) )
        {
            try
            {
                user = _openAmAuthentication.login( strUsername, strPassword, request );

                if ( user != null )
                {
                    SecurityService.getInstance(  ).registerUser( request, user );
                    jsonResponse = new JsonResponse( Boolean.TRUE );
                }
            }
            catch ( LoginException e )
            {
                jsonResponse = new OpenamErrorJsonResponse( JSON_ERROR_LOGIN_ERROR,
                        SecurityTokenService.getInstance(  ).getToken( request, TOKEN_ACTION_LOGIN ) );
            }
        }
        else
        {
            jsonResponse = new OpenamErrorJsonResponse( JSON_ERROR_AUTHENTICATION_NOT_ENABLE );
        }

        return JsonUtil.buildJsonResponse( jsonResponse );
    }
}

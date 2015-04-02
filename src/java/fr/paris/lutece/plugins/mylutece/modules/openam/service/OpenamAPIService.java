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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import fr.paris.lutece.portal.service.spring.SpringContextService;


/**
 * OpenamAPIService
 */
public final class OpenamAPIService
{
    public static final String USER_ATTRINUTE_NAME = "userdetails.attribute.name";
    public static final String USER_ATTRINUTE_VALUE = "userdetails.attribute.value";
    public static final String USER_UID = "uid";
    public static final String PCUID = "pcuid";
    public static final String MESSAGE = "message";
    public static final String PARAMETER_SUBJECT_ID = "subjectid";
    public static final String ERROR_TECHNICAL = "TECHNICAL_ERROR";
    private static final String KEY_TOKEN_ID = "tokenId";
    private static final String KEY_VALID = "valid";
    private static final String KEY_UID = "uid";
    private static final String KEY_RESULT = "result";
    private static final String SEPARATOR_COMMA = ",";

    //HEADERS
    private static final String HEADER_EMAIL = "X-OpenAM-Username";
    private static final String HEADER_PASSWORD = "X-OpenAM-Password";
    private static final String HEADER_SUBJECT_ID = "iplanetDirectoryPro";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_CONTENT_TYPE_JSON_VALUE = "application/json";
    private static final OpenamAPI _authenticateAPI = SpringContextService.getBean( "mylutece-openam.apiAuthenticate" );
    private static final OpenamAPI _sessionAPI = SpringContextService.getBean( "mylutece-openam.apiSession" );
    private static final OpenamAPI _usersAPI = SpringContextService.getBean( "mylutece-openam.apiUsers" );

    /** Private constructor */
    private OpenamAPIService(  )
    {
    }

    /**
     * Process login
     * @param strUserName The User's name
     * @param strUserPassword The User's password
     * @return The response provided by the API in JSON format
     */
    public static String doLogin( String strUserName, String strUserPassword )
        throws OpenamAPIException
    {
        String strResponse = null;
        String strTokenId = null;
        Map<String, String> headerParameters = new HashMap<String, String>(  );

        headerParameters.put( HEADER_EMAIL, strUserName );
        headerParameters.put( HEADER_PASSWORD, strUserPassword );
        headerParameters.put( HEADER_CONTENT_TYPE, HEADER_CONTENT_TYPE_JSON_VALUE );

        strResponse = _authenticateAPI.callMethod( "", null, headerParameters, true );

        JSONObject jo = (JSONObject) JSONSerializer.toJSON( strResponse );

        if ( jo.containsKey( KEY_TOKEN_ID ) )
        {
            strTokenId = jo.getString( KEY_TOKEN_ID );
        }

        return strTokenId;
    }

    /**
     * Checks the connection cookie
     * @param strSubjectId The 'account' cookie value
     * @return The UID if the cookie is valid otherwyse false
     */
    public static String isValidate( String strSubjectId )
        throws OpenamAPIException
    {
        Map<String, String> headerParameters = new HashMap<String, String>(  );
        headerParameters.put( HEADER_CONTENT_TYPE, HEADER_CONTENT_TYPE_JSON_VALUE );

        String strResponse = _sessionAPI.callMethod( strSubjectId + "?_action=validate", null, headerParameters, true );
        JSONObject jo = (JSONObject) JSONSerializer.toJSON( strResponse );

        String strValidResponse = jo.getString( KEY_VALID );

        if ( new Boolean( strValidResponse ) )
        {
            return jo.getString( KEY_UID );
        }

        return null;
    }

    /**
     * Process user logout
     * @param strPCUID the user PCUID
     * @return The response provided by the API in JSON format
     */
    public static String doDisconnect( String strSubjectId )
        throws OpenamAPIException
    {
        Map<String, String> headerParameters = new HashMap<String, String>(  );
        headerParameters.put( HEADER_SUBJECT_ID, strSubjectId );
        headerParameters.put( HEADER_CONTENT_TYPE, HEADER_CONTENT_TYPE_JSON_VALUE );

        String strResult = null;

        String strResponse = _sessionAPI.callMethod( "?_action=logout", null, headerParameters, true );

        JSONObject jo = (JSONObject) JSONSerializer.toJSON( strResponse );

        if ( jo.containsKey( KEY_RESULT ) )
        {
            strResult = jo.getString( KEY_RESULT );
        }

        return strResult;
    }

    /**
     * Get user infos
     * @param strPCUID The UserID
     * @return The response provided by the API in JSON format
     */
    public static Map<String, String> getUserInformations( String strSubjectId, String strUserId,
        Map<String, String> mapUserMapping, String strUserAttributeKey )
        throws OpenamAPIException
    {
        Map<String, String> headerParameters = new HashMap<String, String>(  );
        headerParameters.put( HEADER_SUBJECT_ID, strSubjectId );

        Map<String, String> mapInfos = new HashMap<String, String>(  );

        String strResponse = _usersAPI.callMethod( strUserId + "?_fields=" +
                buildRestrictListOfAttributesReturn( strUserAttributeKey, mapUserMapping ), null, headerParameters,
                true, true );
        JSONObject jo = (JSONObject) JSONSerializer.toJSON( strResponse );
        JSONArray joArray;

        if ( jo.containsKey( strUserAttributeKey ) )
        {
            joArray = jo.getJSONArray( strUserAttributeKey );

            for ( int i = 0; i < joArray.size(  ); i++ )
            {
                mapInfos.put( strUserAttributeKey, joArray.getString( i ) );
            }
        }

        if ( mapUserMapping != null )
        {
            for ( Entry<String, String> entry : mapUserMapping.entrySet(  ) )
            {
                if ( jo.containsKey( entry.getKey(  ) ) )
                {
                    joArray = jo.getJSONArray( entry.getKey(  ) );

                    for ( int i = 0; i < joArray.size(  ); i++ )
                    {
                        mapInfos.put( entry.getKey(  ), joArray.getString( i ) );
                    }
                }
            }
        }

        return mapInfos;
    }

    private static String buildRestrictListOfAttributesReturn( String strUserAttributeKey,
        Map<String, String> mapUserMapping )
    {
        StringBuffer strFields = new StringBuffer(  );
        strFields.append( strUserAttributeKey );

        if ( ( mapUserMapping != null ) && !mapUserMapping.isEmpty(  ) )
        {
            for ( Entry<String, String> entry : mapUserMapping.entrySet(  ) )
            {
                strFields.append( SEPARATOR_COMMA );
                strFields.append( entry.getKey(  ) );
            }
        }

        return strFields.toString(  );
    }
}

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.log4j.Logger;

import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppLogService;


/**
 * ParisConnect API Service
 */
public final class OpenamAPIService
{
	
	
	public static final String USER_ATTRINUTE_NAME = "userdetails.attribute.name";
	public static final String USER_ATTRINUTE_VALUE = "userdetails.attribute.value";
    public static final String USER_UID = "uid";
    public static final String PCUID = "pcuid";
    public static final String MESSAGE = "message";
    private static final String METHOD_DO_LOGIN = "do_login";
     private static final String METHOD_TOKEN_ID = "tokenId";
    
    private static final String METHOD_GET_ATTRIBUTES = "attributes";
    public static final String PARAMETER_SUBJECT_ID = "subjectid";
    public static final String PARAMETER_ID_ALERTES = "idalertes";
    public static final String PARAMETER_ID_EMAIL = "id_mail";
    private static final String KEY_TOKEN_ID = "tokenId";
    private static final String KEY_AUTH_ID = "authId";
    private static final String KEY_VALID = "valid";
    private static final String KEY_UID = "uid";
    private static final String KEY_REALM = "realm";
    private static final String KEY_RESULT = "result";
    private static final String KEY_IS_VERIFIED = "is_verified";
    private static final String KEY_MESSAGE = "message";
    
    
    //HEADERS
    private static final String HEADER_EMAIL = "X-OpenAM-Username";
    private static final String HEADER_PASSWORD = "X-OpenAM-Password";
    private static final String HEADER_SUBJECT_ID = "iplanetDirectoryPro";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_CONTENT_TYPE_JSON_VALUE = "application/json";
    
    private static final OpenamAPI _authenticateAPI = SpringContextService.getBean( "mylutece-openam.apiAuthenticate" );
      private static final OpenamAPI _identityAPI = SpringContextService.getBean( 
            "mylutece-openam.apiIdentity" );
    
    private static final OpenamAPI _sessionAPI = SpringContextService.getBean( 
            "mylutece-openam.apiSession" );
    
 
    private static Logger _logger = org.apache.log4j.Logger.getLogger( Constants.LOGGER_OPENAM );

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
    static String doLogin( String strUserName, String strUserPassword )
        throws OpenamAPIException
    {
        
    	String strResponse=null;
    	String strTokenId=null;
    	Map<String, String> headerParameters = new HashMap<String, String>(  );

        headerParameters.put( HEADER_EMAIL, strUserName );
        headerParameters.put( HEADER_PASSWORD, strUserPassword );
        headerParameters.put( HEADER_CONTENT_TYPE, HEADER_CONTENT_TYPE_JSON_VALUE );
        
        
        
        try
        {
            strResponse =  _authenticateAPI.callMethod( "", null,headerParameters,true );
         
            JSONObject jo = (JSONObject) JSONSerializer.toJSON( strResponse );
            strTokenId = jo.getString( KEY_TOKEN_ID );
        }
        catch ( OpenamAPIException ex )
        {
        	 _logger.error(ex);
        }
        return strTokenId;
        
    }

    /**
     * Checks the connection cookie
     * @param strSubjectId The 'account' cookie value
     * @return The UID if the cookie is valid otherwyse false
     */
    static boolean isValidate( String strSubjectId )
        throws OpenamAPIException
    {
    	Map<String, String> headerParameters = new HashMap<String, String>(  );
    	headerParameters.put( HEADER_CONTENT_TYPE, HEADER_CONTENT_TYPE_JSON_VALUE );
          
	   
          
        try
        {
        	String strResponse = _sessionAPI.callMethod(strSubjectId+"?action=validate" , null,headerParameters, true );
            JSONObject jo = (JSONObject) JSONSerializer.toJSON( strResponse );
            String strValidResponse = jo.getString( KEY_VALID);
            return new Boolean(strValidResponse);
            
        }
        catch ( OpenamAPIException ex )
        {
        	_logger.error(ex); 
        } 
        return false;
    }

    /**
     * Process user logout
     * @param strPCUID the user PCUID
     * @return The response provided by the API in JSON format
     */
    static String doDisconnect( String strSubjectId ) throws OpenamAPIException
    {
    	Map<String, String> headerParameters = new HashMap<String, String>(  );
    	//headerParameters.put( HEADER_CONTENT_TYPE, HEADER_CONTENT_TYPE_JSON_VALUE );
    	headerParameters.put( HEADER_SUBJECT_ID, strSubjectId );
    	
        String strResult=null;  
	   
          
        try
        {
        	String strResponse = _sessionAPI.callMethod( "?action=logout" , null,headerParameters, true );
            JSONObject jo = (JSONObject) JSONSerializer.toJSON( strResponse );
            strResult = jo.getString( KEY_RESULT);
            
            
        }
        catch ( OpenamAPIException ex )
        {
        	_logger.error(ex); 
        } 
        return strResult;
    }

  
    /**
     * Get user infos
     * @param strPCUID The UserID
     * @return The response provided by the API in JSON format
     */
    static Map<String, String> getUserInformations( String strSubjectId) throws OpenamAPIException
    {
        Map<String, String> mapParameters = new HashMap<String, String>(  );
        mapParameters.put( PARAMETER_SUBJECT_ID, strSubjectId );

        
        try
        {
           String strResponse = _identityAPI.callMethod( METHOD_GET_ATTRIBUTES, mapParameters,null,false );
          
           if(strResponse !=null)
           {
	           String strThisLine;
	           String strAttributeName;
	           String strAttributeValue;
	           
	           BufferedReader strBf=new BufferedReader(new StringReader(strResponse));
	            try {
					while ((strThisLine = strBf.readLine()) != null) { 
						
						
						if(strThisLine.contains(USER_ATTRINUTE_NAME) )
						{
							strAttributeName=strThisLine.substring(USER_ATTRINUTE_NAME.length()+1);
							strThisLine= strBf.readLine();
							if(strThisLine != null)
							{
								if(strThisLine.contains(USER_ATTRINUTE_VALUE) )
								{
									
									strAttributeValue=strThisLine.substring(USER_ATTRINUTE_VALUE.length()+1);
									
									mapParameters.put(strAttributeName,strAttributeValue);
								}
							}
							
						}
						
	         }
				} catch (IOException e) {
						AppLogService.error(e);
				}
           }
           
        }
        catch ( OpenamAPIException ex )
        {
            _logger.warn( "Metadata API call : attributes=" + strSubjectId + " - " + ex.getMessage(  ) );

            return null;
        }
        
        return mapParameters;
    }

    
}

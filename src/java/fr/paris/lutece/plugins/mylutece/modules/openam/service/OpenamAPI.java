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

import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.httpaccess.HttpAccess;
import fr.paris.lutece.util.httpaccess.HttpAccessException;
import fr.paris.lutece.util.url.UrlItem;

import net.sf.json.JSON;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.lang.StringUtils;

import org.apache.log4j.Logger;

import java.util.Map;
import java.util.Map.Entry;


/**
 * OpenamAPI
 */
public class OpenamAPI
{
    private static final String PROPERTY_API_CALL_DEBUG = "mylutece-openam.api.debug";
    private static final String KEY_ERROR_MSG = "str";
    public static Logger _logger = Logger.getLogger( Constants.LOGGER_OPENAM );
    public static boolean _bDebug = AppPropertiesService.getPropertyBoolean( PROPERTY_API_CALL_DEBUG, false );

    // Variables declarations 
    private String _strName;
    private String _strUrl;
    private Map _map;

    /**
     * Returns the Name
     *
     * @return The Name
     */
    public String getName(  )
    {
        return _strName;
    }

    /**
     * Sets the Name
     *
     * @param strName The Name
     */
    public void setName( String strName )
    {
        _strName = strName;
    }

    /**
     * Returns the Url
     *
     * @return The Url
     */
    public String getUrl(  )
    {
        return _strUrl;
    }

    /**
     * Sets the Url
     *
     * @param strUrl The Url
     */
    public void setUrl( String strUrl )
    {
        _strUrl = strUrl;
    }

    /**
     * Returns the Map
     *
     * @return The Map
     */
    public Map getMap(  )
    {
        return _map;
    }

    /**
     * Sets the Map
     *
     * @param map The Map
     */
    public void setMap( Map map )
    {
        _map = map;
    }

    /**
     * Call a Method of the API
     * @param strMethod The method name
     * @param mapParameters Parameters
     * @return The string returned by the API
     * @throws OpenamAPIException if an error occurs
     */
    public String callMethod( String strMethod, Map<String, String> mapParameters )
        throws OpenamAPIException
    {
        return callMethod( strMethod, mapParameters, null );
    }

    /**
     * Call a Method of the API
     * @param strMethod The method name
     * @param mapParameters Parameters
     * @param mapHeaders Headers
     * @return The string returned by the API
     * @throws OpenamAPIException if an error occurs
     */
    public String callMethod( String strMethod, Map<String, String> mapParameters, Map<String, String> mapHeaders )
        throws OpenamAPIException
    {
        return callMethod( strMethod, mapParameters, mapHeaders, true );
    }

    /**
     * Call a Method of the API
     * @param strMethod The method name
     * @param mapParameters Parameters
     * @param bJSON Is JSON output
     * @return The string returned by the API
     * @throws OpenamAPIException if an error occurs
     */
    public String callMethod( String strMethod, Map<String, String> mapParameters, Map<String, String> mapHeaders,
        boolean bJSON ) throws OpenamAPIException
    {
        return callMethod( strMethod, mapParameters, mapHeaders, bJSON, false );
    }

    /**
     * Call a Method of the API
     * @param strMethod The method name
     * @param mapParameters Parameters
     * @param bJSON Is JSON output
     * @return The string returned by the API
     * @throws OpenamAPIException if an error occurs
     */
    public String callMethod( String strMethod, Map<String, String> mapParameters, Map<String, String> mapHeaders,
        boolean bJSON, boolean bGet ) throws OpenamAPIException
    {
        HttpAccess httpAccess = new HttpAccess(  );
        UrlItem url = new UrlItem( _strUrl + strMethod );

        String strResponse = "";

        try
        {
            if ( bGet )
            {
                strResponse = httpAccess.doGet( url.getUrl(  ), null, null, mapHeaders );
            }
            else
            {
                strResponse = httpAccess.doPost( url.getUrl(  ), mapParameters, null, null, mapHeaders );
            }

            if ( _bDebug )
            {
                _logger.debug( "API call : " + getCallUrl( url.getUrl(  ), mapParameters ) );
            }

            //        if ( !StringUtils.isEmpty( strResponse ) )
            //        {
            //        	
            //        	checkJSONforErrors( strResponse );
            //           
            //        }
        }
        catch ( HttpAccessException ex )
        {
            _logger.error( "Error calling method '" + strMethod + " - " + ex.getMessage(  ), ex );
            throw new OpenamAPIException( ex.getResponseCode(  ).toString(  ) );
        }

        return strResponse;
    }

    //    /**
    //     * Ckecks JSON for errors
    //     * @param strResponse The response in JSON format
    //     * @throws OpenamAPIException if an error occurs
    //     */
    //    private void checkJSONforErrors( String strResponse )
    //        throws OpenamAPIException
    //    {
    //        JSONObject joObject = (JSONObject) JSONSerializer.toJSON( strResponse );
    //
    //        if ( joObject.containsKey( KEY_ERROR ) )
    //        {
    //            String strError = joObject.getString( KEY_ERROR );
    //            String strMessage;
    //
    //            // ERR value is not always a JSON object
    //            try
    //            {
    //                JSON joError = JSONSerializer.toJSON( strError );
    //
    //                if ( !joError.isArray(  ) )
    //                {
    //                    strMessage = ( (JSONObject) joError ).getString( KEY_ERROR_MSG );
    //                }
    //                else
    //                {
    //                    strMessage = joError.toString(  );
    //                }
    //            }
    //            catch ( JSONException e )
    //            {
    //                strMessage = strError;
    //            }
    //
    //            throw new OpenamAPIException( strMessage );
    //        }
    //    }

    /**
     * Build the URL
     * @param strUrl The base URL
     * @param mapParameters Parameters
     * @return The full URL
     */
    private String getCallUrl( String strUrl, Map<String, String> mapParameters )
    {
        UrlItem url = new UrlItem( strUrl );

        if ( mapParameters != null )
        {
            for ( Entry<String, String> entry : mapParameters.entrySet(  ) )
            {
                url.addParameter( entry.getKey(  ), entry.getValue(  ) );
            }
        }

        return url.getUrl(  );
    }
}

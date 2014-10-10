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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import fr.paris.lutece.plugins.mylutece.authentication.MultiLuteceAuthentication;
import fr.paris.lutece.plugins.mylutece.modules.openam.authentication.OpenamAuthentication;
import fr.paris.lutece.plugins.mylutece.modules.openam.authentication.OpenamUser;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

/**
 * 
 * ParisConnectService
 */
public final class OpenamService {

	public static final String ERROR_ALREADY_SUBSCRIBE = "ALREADY_SUBSCRIBE";
	public static final String ERROR_DURING_SUBSCRIBE = "ERROR_DURING_SUBSCRIBE";
	private static final String AUTHENTICATION_BEAN_NAME = "mylutece-openam.authentication";
	private static OpenamService _singleton;
	private static final String PROPERTY_COOKIE_PARIS_CONNECT_NAME = "mylutece-openam.cookieName";
	private static final String PROPERTY_COOKIE_PARIS_CONNECT_DOMAIN = "parisconnect.cookieDomain";
	private static final String PROPERTY_COOKIE_PARIS_CONNECT_PATH = "mylutece-openam.cookiePath";
	private static final String PROPERTY_COOKIE_PARIS_CONNECT_MAX_AGE = "mylutece-openam.cookieMaxAge";
	private static final String PROPERTY_COOKIE_PARIS_CONNECT_MAX_SECURE = "mylutece-openam.cookieSecure";

	public static final String PROPERTY_USER_KEY_NAME = "mylutece-openam.attributeKeyUsername";
	public static final String PROPERTY_USER_MAPPING_ATTRIBUTES = "mylutece-openam.userMappingAttributes";

	public static final String CONSTANT_LUTECE_USER_PROPERTIES_PATH = "mylutece-openam.attribute";

	private static String COOKIE_OPENAM_NAME;
	private static String COOKIE_OPENAM_DOMAIN;
	private static String COOKIE_OPENAM_PATH;
	private static int COOKIE_OPENAM_MAX_AGE;
	private static boolean COOKIE_OPENAM_SECURE;
	private static final String SEPARATOR = ",";
	private static Map<String, String> ATTRIBUTE_USER_MAPPING;
	private static String ATTRIBUTE_USER_KEY_NAME;
	private static Logger _logger = Logger
			.getLogger(Constants.LOGGER_OPENAM);

	/**
	 * Empty constructor
	 */
	private OpenamService() {
		// nothing
	}

	/**
	 * Gets the instance
	 * 
	 * @return the instance
	 */
	public static OpenamService getInstance() {
		if (_singleton == null) {
			_singleton = new OpenamService();
			COOKIE_OPENAM_NAME = AppPropertiesService
					.getProperty(PROPERTY_COOKIE_PARIS_CONNECT_NAME);
			COOKIE_OPENAM_DOMAIN = AppPropertiesService
					.getProperty(PROPERTY_COOKIE_PARIS_CONNECT_DOMAIN);
			COOKIE_OPENAM_PATH = AppPropertiesService
					.getProperty(PROPERTY_COOKIE_PARIS_CONNECT_PATH);
			COOKIE_OPENAM_MAX_AGE = AppPropertiesService.getPropertyInt(
					PROPERTY_COOKIE_PARIS_CONNECT_MAX_AGE, 60 * 30);
			COOKIE_OPENAM_SECURE = AppPropertiesService
					.getPropertyBoolean(
							PROPERTY_COOKIE_PARIS_CONNECT_MAX_SECURE, true);

			ATTRIBUTE_USER_KEY_NAME = AppPropertiesService
					.getProperty(PROPERTY_USER_KEY_NAME);

			String strUserMappingAttributes = AppPropertiesService
					.getProperty(PROPERTY_USER_MAPPING_ATTRIBUTES);
			ATTRIBUTE_USER_MAPPING = new HashMap<String, String>();

			if (StringUtils.isNotBlank(strUserMappingAttributes)) {
				String[] tabUserProperties = strUserMappingAttributes
						.split(SEPARATOR);
				String userProperties;

				for (int i = 0; i < tabUserProperties.length; i++) {
					userProperties = AppPropertiesService
							.getProperty(CONSTANT_LUTECE_USER_PROPERTIES_PATH
									+ "." + tabUserProperties[i]);

					if (StringUtils.isNotBlank(userProperties)) {
						ATTRIBUTE_USER_MAPPING.put(userProperties,
								tabUserProperties[i]);
					}
				}
			}
		}

		return _singleton;
	}

	/**
	 * Inits plugin. Registers authentication
	 */
	public void init() {
		OpenamAuthentication authentication = (OpenamAuthentication) SpringContextService
				.getPluginBean(OpenamPlugin.PLUGIN_NAME,
						AUTHENTICATION_BEAN_NAME);

		if (authentication != null) {
			MultiLuteceAuthentication.registerAuthentication(authentication);
		} else {
			_logger.error("ParisConnectAuthentication not found, please check your parisconnect_context.xml configuration");
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
	 * @param parisConnectAuthentication
	 *            The authentication
	 * @return The LuteceUser
	 */
	public OpenamUser doLogin(HttpServletRequest request, String strUserName,
			String strUserPassword,
			OpenamAuthentication parisConnectAuthentication) {
		String strTokenId;
		OpenamUser user = null;

		try {
			strTokenId = OpenamAPIService.doLogin(strUserName, strUserPassword);

			if (strTokenId != null) {

				Map<String, String> userInformations = OpenamAPIService
						.getUserInformations(strTokenId);

				// test contains guid
				if (userInformations != null
						&& userInformations
								.containsKey(ATTRIBUTE_USER_KEY_NAME)) {
					user = new OpenamUser(
							userInformations.get(ATTRIBUTE_USER_KEY_NAME),
							parisConnectAuthentication,strTokenId);
					addUserAttributes(userInformations, user);
				}

			}

		} catch (OpenamAPIException ex) {
			_logger.warn(ex.getMessage());
		}

		return user;
	}

	/**
	 * Logout to paris connect
	 * 
	 * @param user
	 *            the ParisConnectUser
	 */
	public void doLogout(OpenamUser user) {
		try {
			OpenamAPIService.doDisconnect(user.getSubjectId());
		} catch (OpenamAPIException ex) {
			_logger.warn(ex.getMessage());
		}
	}

	/**
	 * Gets the authenticated user
	 * 
	 * @param request
	 *            The HTTP request
	 * @param parisConnectAuthentication
	 *            The Authentication
	 * @return The LuteceUser
	 */
	public OpenamUser getHttpAuthenticatedUser(HttpServletRequest request,
			OpenamAuthentication parisConnectAuthentication) {
		OpenamUser user = null;

		String strTokenId  = getConnectionCookie(request);

		if (strTokenId  != null) {
			try {
				Boolean isValidate = OpenamAPIService.isValidate(strTokenId );

				if (isValidate) {
				
					   Map<String, String> userInformations = OpenamAPIService
								.getUserInformations(strTokenId );

						// test contains guid
						if (userInformations != null
								&& userInformations
										.containsKey(ATTRIBUTE_USER_KEY_NAME)) {
							user = new OpenamUser(
									userInformations.get(ATTRIBUTE_USER_KEY_NAME),
									parisConnectAuthentication,strTokenId);
							addUserAttributes(userInformations, user);
						}

				}
			} catch (OpenamAPIException ex) {
				_logger.warn(ex.getMessage());
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
	public String getConnectionCookie(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		String strOpenamCookie = null;

		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(COOKIE_OPENAM_NAME)) {
					strOpenamCookie = cookie.getValue();
					_logger.debug("getHttpAuthenticatedUser : cookie '"
							+ COOKIE_OPENAM_NAME + "' found - value="
							+ strOpenamCookie);
				}
			}
		}

		return strOpenamCookie;
	}

	/**
	 * set a paris connect cokkie in the HttpServletResponse
	 * 
	 * @param strPCUID
	 *            the user PCUID
	 * @param response
	 *            The HTTP response
	 */
	public void setConnectionCookie(String strPCUID,
			HttpServletResponse response) {
		// set a connexion cookie to let the user access other PC Services
		// without sign in
		Cookie parisConnectCookie = new Cookie(COOKIE_OPENAM_NAME,
				strPCUID);
		parisConnectCookie.setDomain(COOKIE_OPENAM_DOMAIN);
		parisConnectCookie.setSecure(COOKIE_OPENAM_SECURE);
		parisConnectCookie.setMaxAge(COOKIE_OPENAM_MAX_AGE);
		parisConnectCookie.setPath(COOKIE_OPENAM_PATH);

		response.addCookie(parisConnectCookie);
	}

	/**
	 * Fill user's data
	 * 
	 * @param user
	 *            The User
	 * @param strUserData
	 *            Data in JSON format
	 */
	private void addUserAttributes(Map<String, String> userInformations,
			OpenamUser user) {
		for (Entry<String, String> entry : userInformations.entrySet()) {
			if (ATTRIBUTE_USER_MAPPING.containsKey(entry.getKey())) {
				user.setUserInfo(ATTRIBUTE_USER_MAPPING.get(entry.getKey()),
						entry.getValue());
			}

		}

	}
}

// ========================================================================
// Copyright 2008-2009 NEXCOM Systems
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at 
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ========================================================================

package org.cipango.sip;

import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.BufferCache;
import org.eclipse.jetty.io.BufferCache.CachedBuffer;

public class SipHeaders 
{
	public static final String 
		ACCEPT = "Accept", 
		ACCEPT_CONTACT = "Accept-Contact",
		ACCEPT_ENCODING = "Accept-Encoding",
		ACCEPT_LANGUAGE = "Accept-Language",
		ACCEPT_RESOURCE_PRIORITY = "Accept-Resource-Priority",
		ALERT_INFO = "Alert-Info",
		ALLOW = "Allow",
		ALLOW_EVENTS = "Allow-Events",	
		AUTHENTICATION_INFO = "Authentication-Info",
		AUTHORIZATION = "Authorization", 
		CALL_ID = "Call-ID",
		CALL_INFO = "Call-Info",
		CONTACT = "Contact",
		CONTENT_DISPOSITION = "Content-Disposition",
		CONTENT_ENCODING = "Content-Encoding",
		CONTENT_LANGUAGE = "Content-Language",
		CONTENT_LENGTH = "Content-Length",
		CONTENT_TYPE = "Content-Type",
		CSEQ = "CSeq",
		DATE = "Date",
		ERROR_INFO = "Error-Info",
		EVENT = "Event",
		EXPIRES = "Expires",
		FROM = "From",
		HISTORY_INFO = "History-Info",
		IDENTITY = "Identity",
		IDENTITY_INFO = "Identity-Info",
		IN_REPLY_TO = "In-Reply-To",
		JOIN = "Join", 
		MAX_FORWARDS = "Max-Forwards",
		MIME_VERSION = "MIME-Version",
		MIN_EXPIRES = "Min-Expires",
		MIN_SE = "Min-SE",
		ORGANIZATION = "Organization",
		P_ACCESS_NETWORK_INFO = "P-Access-Network-Info",
		P_ASSERTED_IDENTITY = "P-Asserted-Identity",
		P_ASSOCIATED_URI = "P-Associated-URI",
		P_CALLED_PARTY_ID = "P-Called-Party-ID",
		P_CHARGING_FUNCTION_ADDRESSES = "P-Charging-Function-Addresses",
		P_CHARGING_VECTOR = "P-Charging-Vector",
		P_MEDIA_AUTHORIZATION = "P-Media-Authorization", 
		P_PREFERRED_IDENTITY = "P-Preferred-Identity",
		P_USER_DATABASE = "P-User-Database",
		P_VISITED_NETWORK_ID = "P-Visited-Network-ID",
		PATH = "Path", 
		PRIORITY = "Priority",
		PRIVACY = "Privacy", 
		PROXY_AUTHENTICATE = "Proxy-Authenticate",
		PROXY_AUTHORIZATION = "Proxy-Authorization",
		PROXY_REQUIRE = "Proxy-Require",
		RACK = "RAck",
		REASON = "Reason",
		RECORD_ROUTE = "Record-Route",
		REFER_SUB = "Refer-Sub",
		REFER_TO = "Refer-To",
		REFERRED_BY = "Referred-By",
		REJECT_CONTACT = "Reject-Contact",
		REPLACES = "Replaces",
		REPLY_TO = "Reply-To",
		REQUEST_DISPOSITION = "Request-Disposition",
		REQUIRE = "Require", 
		RESOURCE_PRIORITY = "Resource-Priority",
		RETRY_AFTER = "Retry-After", 
		ROUTE = "Route",
		RSEQ = "RSeq",
		SECURITY_CLIENT = "Secury-Client", 
		SECURITY_SERVER = "Security-Server",
		SECURITY_VERIFY = "Security-Verify",
		SERVER = "Server",
		SERVICE_ROUTE = "Service-Route", 
		SESSION_EXPIRES = "Session-Expires",
		SIP_ETAG = "SIP-ETag",
		SIP_IF_MATCH = "SIP-If-Match",
		SUBJECT = "Subject",
		SUBSCRIPTION_STATE = "Subscription-State",
		SUPPORTED = "Supported",
		TARGET_DIALOG = "Target-Dialog",
		TIMESTAMP = "Timestamp",
		TO = "To",
		UNSUPPORTED = "Unsupported",
		USER_AGENT = "User-Agent",
		VIA = "Via",
		WARNING = "Warning",
		WWW_AUTHENTICATE = "WWW-Authenticate";
	
	public static final int 
		ACCEPT_ORDINAL = 1, 
		ACCEPT_CONTACT_ORDINAL = 2,
		ACCEPT_ENCODING_ORDINAL = 3,
		ACCEPT_LANGUAGE_ORDINAL = 4,
		ACCEPT_RESOURCE_PRIORITY_ORDINAL = 5,
		ALERT_INFO_ORDINAL = 6,
		ALLOW_ORDINAL = 7,
		ALLOW_EVENTS_ORDINAL = 8,	
		AUTHENTICATION_INFO_ORDINAL = 9,
		AUTHORIZATION_ORDINAL = 10, 
		CALL_ID_ORDINAL = 11,
		CALL_INFO_ORDINAL = 12,
		CONTACT_ORDINAL = 13,
		CONTENT_DISPOSITION_ORDINAL = 14,
		CONTENT_ENCODING_ORDINAL = 15,
		CONTENT_LANGUAGE_ORDINAL = 16,
		CONTENT_LENGTH_ORDINAL = 17,
		CONTENT_TYPE_ORDINAL = 18,
		CSEQ_ORDINAL = 19,
		DATE_ORDINAL = 20,
		ERROR_INFO_ORDINAL = 21,
		EVENT_ORDINAL = 22,
		EXPIRES_ORDINAL = 23,
		FROM_ORDINAL = 24,
		HISTORY_INFO_ORDINAL = 25,
		IDENTITY_ORDINAL = 26,
		IDENTITY_INFO_ORDINAL = 27,
		IN_REPLY_TO_ORDINAL = 28,
		JOIN_ORDINAL = 29, 
		MAX_FORWARDS_ORDINAL = 30,
		MIME_VERSION_ORDINAL = 31,
		MIN_EXPIRES_ORDINAL = 32,
		MIN_SE_ORDINAL = 33,
		ORGANIZATION_ORDINAL = 34,
		P_ACCESS_NETWORK_INFO_ORDINAL = 35,
		P_ASSERTED_IDENTITY_ORDINAL = 36,
		P_ASSOCIATED_URI_ORDINAL = 37,
		P_CALLED_PARTY_ID_ORDINAL = 38,
		P_CHARGING_FUNCTION_ADDRESSES_ORDINAL = 39,
		P_CHARGING_VECTOR_ORDINAL = 40,
		P_MEDIA_AUTHORIZATION_ORDINAL = 41, 
		P_PREFERRED_IDENTITY_ORDINAL = 42,
		P_USER_DATABASE_ORDINAL = 43,
		P_VISITED_NETWORK_ID_ORDINAL = 44,
		PATH_ORDINAL = 45, 
		PRIORITY_ORDINAL = 46,
		PRIVACY_ORDINAL = 47, 
		PROXY_AUTHENTICATE_ORDINAL = 48,
		PROXY_AUTHORIZATION_ORDINAL = 49,
		PROXY_REQUIRE_ORDINAL = 50,
		RACK_ORDINAL = 51,
		REASON_ORDINAL = 52,
		RECORD_ROUTE_ORDINAL = 53,
		REFER_SUB_ORDINAL = 54,
		REFER_TO_ORDINAL = 55,
		REFERRED_BY_ORDINAL = 56,
		REJECT_CONTACT_ORDINAL = 57,
		REPLACES_ORDINAL = 58,
		REPLY_TO_ORDINAL = 59,
		REQUEST_DISPOSITION_ORDINAL = 60,
		REQUIRE_ORDINAL = 61, 
		RESOURCE_PRIORITY_ORDINAL = 62,
		RETRY_AFTER_ORDINAL = 63, 
		ROUTE_ORDINAL = 64,
		RSEQ_ORDINAL = 65,
		SECURITY_CLIENT_ORDINAL = 66, 
		SECURITY_SERVER_ORDINAL = 67,
		SECURITY_VERIFY_ORDINAL = 68,
		SERVER_ORDINAL = 69,
		SERVICE_ROUTE_ORDINAL = 70, 
		SESSION_EXPIRES_ORDINAL = 71,
		SIP_ETAG_ORDINAL = 72,
		SIP_IF_MATCH_ORDINAL = 73,
		SUBJECT_ORDINAL = 74,
		SUBSCRIPTION_STATE_ORDINAL = 75,
		SUPPORTED_ORDINAL = 76,
		TARGET_DIALOG_ORDINAL = 77,
		TIMESTAMP_ORDINAL = 78,
		TO_ORDINAL = 79,
		UNSUPPORTED_ORDINAL = 80,
		USER_AGENT_ORDINAL = 81,
		VIA_ORDINAL = 82,
		WARNING_ORDINAL = 83,
		WWW_AUTHENTICATE_ORDINAL = 84;
	
	public static final BufferCache CACHE = new BufferCache();
	
	public static final CachedBuffer
	
		ACCEPT_BUFFER = CACHE.add(ACCEPT, ACCEPT_ORDINAL),
		ACCEPT_CONTACT_BUFFER = CACHE.add(ACCEPT_CONTACT, ACCEPT_CONTACT_ORDINAL),
		ACCEPT_ENCODING_BUFFER = CACHE.add(ACCEPT_ENCODING, ACCEPT_ENCODING_ORDINAL),
		ACCEPT_LANGUAGE_BUFFER = CACHE.add(ACCEPT_LANGUAGE, ACCEPT_LANGUAGE_ORDINAL),
		ACCEPT_RESOURCE_PRIORITY_BUFFER = CACHE.add(ACCEPT_RESOURCE_PRIORITY, ACCEPT_RESOURCE_PRIORITY_ORDINAL),
		ALERT_INFO_BUFFER = CACHE.add(ALERT_INFO, ALERT_INFO_ORDINAL),
		ALLOW_BUFFER = CACHE.add(ALLOW, ALLOW_ORDINAL),
		ALLOW_EVENTS_BUFFER = CACHE.add(ALLOW_EVENTS, ALLOW_EVENTS_ORDINAL),
		AUTHENTICATION_INFO_BUFFER = CACHE.add(AUTHENTICATION_INFO, AUTHENTICATION_INFO_ORDINAL),
		AUTHORIZATION_BUFFER = CACHE.add(AUTHORIZATION, AUTHORIZATION_ORDINAL),
		CALL_ID_BUFFER = CACHE.add(CALL_ID, CALL_ID_ORDINAL),
		CALL_INFO_BUFFER = CACHE.add(CALL_INFO, CALL_INFO_ORDINAL),
		CONTACT_BUFFER = CACHE.add(CONTACT, CONTACT_ORDINAL),
		CONTENT_DISPOSITION_BUFFER = CACHE.add(CONTENT_DISPOSITION, CONTENT_DISPOSITION_ORDINAL),
		CONTENT_ENCODING_BUFFER = CACHE.add(CONTENT_ENCODING, CONTENT_ENCODING_ORDINAL),
		CONTENT_LANGUAGE_BUFFER = CACHE.add(CONTENT_LANGUAGE, CONTENT_LANGUAGE_ORDINAL),
		CONTENT_LENGTH_BUFFER = CACHE.add(CONTENT_LENGTH, CONTENT_LENGTH_ORDINAL),
		CONTENT_TYPE_BUFFER = CACHE.add(CONTENT_TYPE, CONTENT_TYPE_ORDINAL),
		CSEQ_BUFFER = CACHE.add(CSEQ, CSEQ_ORDINAL),
		DATE_BUFFER = CACHE.add(DATE, DATE_ORDINAL),
		ERROR_INFO_BUFFER = CACHE.add(ERROR_INFO, ERROR_INFO_ORDINAL),
		EVENT_BUFFER = CACHE.add(EVENT, EVENT_ORDINAL),
		EXPIRES_BUFFER = CACHE.add(EXPIRES, EXPIRES_ORDINAL),
		FROM_BUFFER = CACHE.add(FROM, FROM_ORDINAL),
		HISTORY_INFO_BUFFER = CACHE.add(HISTORY_INFO, HISTORY_INFO_ORDINAL),
		IDENTITY_BUFFER = CACHE.add(IDENTITY, IDENTITY_ORDINAL),
		IDENTITY_INFO_BUFFER = CACHE.add(IDENTITY_INFO, IDENTITY_INFO_ORDINAL),
		IN_REPLY_TO_BUFFER = CACHE.add(IN_REPLY_TO, IN_REPLY_TO_ORDINAL),
		JOIN_BUFFER = CACHE.add(JOIN, JOIN_ORDINAL),
		MAX_FORWARDS_BUFFER = CACHE.add(MAX_FORWARDS, MAX_FORWARDS_ORDINAL),
		MIME_VERSION_BUFFER = CACHE.add(MIME_VERSION, MIME_VERSION_ORDINAL),
		MIN_EXPIRES_BUFFER = CACHE.add(MIN_EXPIRES, MIN_EXPIRES_ORDINAL),
		MIN_SE_BUFFER = CACHE.add(MIN_SE, MIN_SE_ORDINAL),
		ORGANIZATION_BUFFER = CACHE.add(ORGANIZATION, ORGANIZATION_ORDINAL),
		P_ACCESS_NETWORK_INFO_BUFFER = CACHE.add(P_ACCESS_NETWORK_INFO, P_ACCESS_NETWORK_INFO_ORDINAL),
		P_ASSERTED_IDENTITY_BUFFER = CACHE.add(P_ASSERTED_IDENTITY, P_ASSERTED_IDENTITY_ORDINAL),
		P_ASSOCIATED_URI_BUFFER = CACHE.add(P_ASSOCIATED_URI, P_ASSOCIATED_URI_ORDINAL),
		P_CALLED_PARTY_ID_BUFFER = CACHE.add(P_CALLED_PARTY_ID, P_CALLED_PARTY_ID_ORDINAL),
		P_CHARGING_FUNCTION_ADDRESSES_BUFFER = CACHE.add(P_CHARGING_FUNCTION_ADDRESSES, P_CHARGING_FUNCTION_ADDRESSES_ORDINAL),
		P_CHARGING_VECTOR_BUFFER = CACHE.add(P_CHARGING_VECTOR, P_CHARGING_VECTOR_ORDINAL),
		P_MEDIA_AUTHORIZATION_BUFFER = CACHE.add(P_MEDIA_AUTHORIZATION, P_MEDIA_AUTHORIZATION_ORDINAL),
		P_PREFERRED_IDENTITY_BUFFER = CACHE.add(P_PREFERRED_IDENTITY, P_PREFERRED_IDENTITY_ORDINAL),
		P_USER_DATABASE_BUFFER = CACHE.add(P_USER_DATABASE, P_USER_DATABASE_ORDINAL),
		P_VISITED_NETWORK_ID_BUFFER = CACHE.add(P_VISITED_NETWORK_ID, P_VISITED_NETWORK_ID_ORDINAL),
		PATH_BUFFER = CACHE.add(PATH, PATH_ORDINAL),
		PRIORITY_BUFFER = CACHE.add(PRIORITY, PRIORITY_ORDINAL), 
		PRIVACY_BUFFER = CACHE.add(PRIVACY, PRIVACY_ORDINAL),
		PROXY_AUTHENTICATE_BUFFER = CACHE.add(PROXY_AUTHENTICATE, PROXY_AUTHENTICATE_ORDINAL),
		PROXY_AUTHORIZATION_BUFFER = CACHE.add(PROXY_AUTHORIZATION, PROXY_AUTHORIZATION_ORDINAL),
		PROXY_REQUIRE_BUFFER = CACHE.add(PROXY_REQUIRE, PROXY_REQUIRE_ORDINAL),
		RACK_BUFFER = CACHE.add(RACK, RACK_ORDINAL),
		REASON_BUFFER = CACHE.add(REASON, REASON_ORDINAL),
		RECORD_ROUTE_BUFFER = CACHE.add(RECORD_ROUTE, RECORD_ROUTE_ORDINAL),
		REFER_SUB_BUFFER = CACHE.add(REFER_SUB, REFER_SUB_ORDINAL),
		REFER_TO_BUFFER = CACHE.add(REFER_TO, REFER_TO_ORDINAL),
		REFERRED_BY_BUFFER = CACHE.add(REFERRED_BY, REFERRED_BY_ORDINAL),
		REJECT_CONTACT_BUFFER = CACHE.add(REJECT_CONTACT, REJECT_CONTACT_ORDINAL),
		REPLACES_BUFFER = CACHE.add(REPLACES, REPLACES_ORDINAL),
		REPLY_TO_BUFFER = CACHE.add(REPLY_TO, REPLY_TO_ORDINAL),
		REQUEST_DISPOSITION_BUFFER = CACHE.add(REQUEST_DISPOSITION, REQUEST_DISPOSITION_ORDINAL),
		REQUIRE_BUFFER = CACHE.add(REQUIRE, REQUIRE_ORDINAL),
		RESOURCE_PRIORITY_BUFFER = CACHE.add(RESOURCE_PRIORITY, RESOURCE_PRIORITY_ORDINAL),
		RETRY_AFTER_BUFFER = CACHE.add(RETRY_AFTER, RETRY_AFTER_ORDINAL),
		ROUTE_BUFFER = CACHE.add(ROUTE, ROUTE_ORDINAL),
		RSEQ_BUFFER = CACHE.add(RSEQ, RSEQ_ORDINAL), 
		SECURITY_CLIENT_BUFFER = CACHE.add(SECURITY_CLIENT, SECURITY_CLIENT_ORDINAL),
		SECURITY_SERVER_BUFFER = CACHE.add(SECURITY_SERVER, SECURITY_SERVER_ORDINAL),
		SECURITY_VERIFY_BUFFER = CACHE.add(SECURITY_VERIFY, SECURITY_VERIFY_ORDINAL),
		SERVER_BUFFER = CACHE.add(SERVER, SERVER_ORDINAL),
		SERVICE_ROUTE_BUFFER = CACHE.add(SERVICE_ROUTE, SERVICE_ROUTE_ORDINAL),
		SESSION_EXPIRES_BUFFER = CACHE.add(SESSION_EXPIRES, SESSION_EXPIRES_ORDINAL),
		SIP_ETAG_BUFFER = CACHE.add(SIP_ETAG, SIP_ETAG_ORDINAL),
		SIP_IF_MATCH_BUFFER = CACHE.add(SIP_IF_MATCH, SIP_IF_MATCH_ORDINAL),
		SUBJECT_BUFFER = CACHE.add(SUBJECT, SUBJECT_ORDINAL),
		SUBSCRIPTION_STATE_BUFFER = CACHE.add(SUBSCRIPTION_STATE, SUBSCRIPTION_STATE_ORDINAL),
		SUPPORTED_BUFFER = CACHE.add(SUPPORTED, SUPPORTED_ORDINAL),
		TARGET_DIALOG_BUFFER = CACHE.add(TARGET_DIALOG, TARGET_DIALOG_ORDINAL),
		TIMESTAMP_BUFFER = CACHE.add(TIMESTAMP, TIMESTAMP_ORDINAL),
		TO_BUFFER = CACHE.add(TO, TO_ORDINAL),
		UNSUPPORTED_BUFFER = CACHE.add(UNSUPPORTED, UNSUPPORTED_ORDINAL),
		USER_AGENT_BUFFER = CACHE.add(USER_AGENT, USER_AGENT_ORDINAL),
		VIA_BUFFER = CACHE.add(VIA, VIA_ORDINAL),
		WARNING_BUFFER = CACHE.add(WARNING, WARNING_ORDINAL),
		WWW_AUTHENTICATE_BUFFER = CACHE.add(WWW_AUTHENTICATE, WWW_AUTHENTICATE_ORDINAL);
	
    
	private static CachedBuffer[] __compact = new CachedBuffer['z'+1];
	private static final BufferCache COMPACT_CACHE = new BufferCache();
	static 
	{
		__compact['a'] = __compact['A'] = ACCEPT_CONTACT_BUFFER;
		__compact['b'] = __compact['B'] = REFERRED_BY_BUFFER;
		__compact['c'] = __compact['C'] = CONTENT_TYPE_BUFFER;
		__compact['d'] = __compact['D'] = REQUEST_DISPOSITION_BUFFER;
		__compact['e'] = __compact['E'] = CONTENT_ENCODING_BUFFER;
		__compact['f'] = __compact['F'] = FROM_BUFFER;
		__compact['g'] = __compact['G'] = null;
		__compact['h'] = __compact['H'] = null;
		__compact['i'] = __compact['I'] = CALL_ID_BUFFER;
		__compact['j'] = __compact['J'] = REJECT_CONTACT_BUFFER;
		__compact['k'] = __compact['K'] = SUPPORTED_BUFFER;
		__compact['l'] = __compact['L'] = CONTENT_LENGTH_BUFFER;
		__compact['m'] = __compact['M'] = CONTACT_BUFFER;
		__compact['n'] = __compact['N'] = IDENTITY_INFO_BUFFER;
		__compact['o'] = __compact['O'] = EVENT_BUFFER;
		__compact['p'] = __compact['P'] = null;
		__compact['q'] = __compact['Q'] = null;
		__compact['r'] = __compact['R'] = REFER_TO_BUFFER;
		__compact['s'] = __compact['S'] = SUBJECT_BUFFER;
		__compact['t'] = __compact['T'] = TO_BUFFER;
		__compact['u'] = __compact['U'] = ALLOW_EVENTS_BUFFER;
		__compact['v'] = __compact['V'] = VIA_BUFFER;
		__compact['w'] = __compact['W'] = null;
		__compact['x'] = __compact['X'] = SESSION_EXPIRES_BUFFER;
		__compact['y'] = __compact['Y'] = IDENTITY_BUFFER;
		__compact['z'] = __compact['Z'] = null;
		
		for (int i = 0; i < 85; i++)
		{
			CachedBuffer buffer = CACHE.get(i);
			for (char c = 'a'; c <= 'z'; c++)
			{
				if (__compact[c] == buffer)
				{
					COMPACT_CACHE.add("" + c, i);
					break;
				}
			}
		}
	}
	
	
	public static CachedBuffer getCompact(int ch)
	{
		if (ch < 0 || ch > 'z')
			return null;
		return __compact[ch];
	}
    
	
	public static Buffer getCompact(Buffer buffer)
	{
		if (buffer instanceof CachedBuffer)
		{
			CachedBuffer cachedBuffer = (CachedBuffer) buffer;
		
			Buffer compact = COMPACT_CACHE.get(cachedBuffer.getOrdinal());
			if (compact != null)
				return compact;
		}
		return buffer;
	}
	
	public static CachedBuffer getCachedName(String s)
	{
		CachedBuffer name = null;
		
		if (s.length() == 1)
			name = getCompact(s.charAt(0));
		
		if (name == null)
			name = (CachedBuffer) CACHE.lookup(s);
		
		return name;
	}
	
	public static final HeaderInfo[] __types = new HeaderInfo[85];
    
	static 
    {
        __types[ACCEPT_ORDINAL] = new HeaderInfo(ACCEPT_ORDINAL, HeaderInfo.PARAMETERABLE, false, true, true);
        __types[ACCEPT_CONTACT_ORDINAL] = new HeaderInfo(ACCEPT_CONTACT_ORDINAL, true);
        __types[ACCEPT_ENCODING_ORDINAL] = new HeaderInfo(ACCEPT_ENCODING_ORDINAL, HeaderInfo.PARAMETERABLE, false, true, true);
        __types[ACCEPT_LANGUAGE_ORDINAL] = new HeaderInfo(ACCEPT_LANGUAGE_ORDINAL, true);
        __types[ACCEPT_RESOURCE_PRIORITY_ORDINAL] = new HeaderInfo(ACCEPT_RESOURCE_PRIORITY_ORDINAL, true);
        __types[ALERT_INFO_ORDINAL] = new HeaderInfo(ALERT_INFO_ORDINAL, HeaderInfo.PARAMETERABLE, false, true, true);
        __types[ALLOW_ORDINAL] = new HeaderInfo(ALLOW_ORDINAL, true);
        __types[ALLOW_EVENTS_ORDINAL] = new HeaderInfo(ALLOW_EVENTS_ORDINAL, true);
        __types[AUTHENTICATION_INFO_ORDINAL] = new HeaderInfo(AUTHENTICATION_INFO_ORDINAL, true);
        __types[AUTHORIZATION_ORDINAL] = new HeaderInfo(AUTHENTICATION_INFO_ORDINAL, false);
        __types[CALL_INFO_ORDINAL] = new HeaderInfo(CALL_INFO_ORDINAL, HeaderInfo.ADDRESS, false, true, true);
        __types[CONTACT_ORDINAL] = new HeaderInfo(CONTACT_ORDINAL, HeaderInfo.ADDRESS, false, true, true);
        __types[CONTENT_DISPOSITION_ORDINAL] = new HeaderInfo(CONTENT_DISPOSITION_ORDINAL, HeaderInfo.PARAMETERABLE, false, true, true);
        __types[CONTENT_ENCODING_ORDINAL] = new HeaderInfo(CONTENT_ENCODING_ORDINAL, true);
        __types[CONTENT_LANGUAGE_ORDINAL] = new HeaderInfo(CONTENT_LANGUAGE_ORDINAL, true);
        __types[CONTENT_LENGTH_ORDINAL] = new HeaderInfo(CONTENT_LENGTH_ORDINAL, false);
        __types[CONTENT_TYPE_ORDINAL] = new HeaderInfo(CONTENT_TYPE_ORDINAL, HeaderInfo.PARAMETERABLE, false, false, false);
        __types[DATE_ORDINAL] = new HeaderInfo(DATE_ORDINAL, false);
        __types[ERROR_INFO_ORDINAL] = new HeaderInfo(ERROR_INFO_ORDINAL, HeaderInfo.PARAMETERABLE, false, true, true);
        __types[EVENT_ORDINAL] = new HeaderInfo(EVENT_ORDINAL, true);
        __types[EXPIRES_ORDINAL] = new HeaderInfo(EXPIRES_ORDINAL, false);
        __types[HISTORY_INFO_ORDINAL] = new HeaderInfo(HISTORY_INFO_ORDINAL, true);
        __types[IDENTITY_ORDINAL] = new HeaderInfo(IDENTITY_ORDINAL, false);      
        __types[IDENTITY_INFO_ORDINAL] = new HeaderInfo(IDENTITY_INFO_ORDINAL, false);       
        __types[JOIN_ORDINAL] = new HeaderInfo(JOIN_ORDINAL, false);       
        __types[MAX_FORWARDS_ORDINAL] = new HeaderInfo(MAX_FORWARDS_ORDINAL, false);
        __types[MIN_SE_ORDINAL] = new HeaderInfo(MIN_SE_ORDINAL, false);
        __types[P_ACCESS_NETWORK_INFO_ORDINAL] = new HeaderInfo(P_ACCESS_NETWORK_INFO_ORDINAL, false);
        __types[P_ASSERTED_IDENTITY_ORDINAL] = new HeaderInfo(P_ASSERTED_IDENTITY_ORDINAL, HeaderInfo.ADDRESS, false, true, true);
        __types[P_ASSOCIATED_URI_ORDINAL] = new HeaderInfo(P_ASSOCIATED_URI_ORDINAL, HeaderInfo.ADDRESS, false, true, true);
        __types[P_CALLED_PARTY_ID_ORDINAL] = new HeaderInfo(P_CALLED_PARTY_ID_ORDINAL, false);
        __types[P_CHARGING_FUNCTION_ADDRESSES_ORDINAL] = new HeaderInfo(P_CHARGING_FUNCTION_ADDRESSES_ORDINAL, false);
        __types[P_CHARGING_VECTOR_ORDINAL] = new HeaderInfo(P_CHARGING_VECTOR_ORDINAL, false);
        __types[P_MEDIA_AUTHORIZATION_ORDINAL] = new HeaderInfo(P_MEDIA_AUTHORIZATION_ORDINAL, false); 
        __types[P_PREFERRED_IDENTITY_ORDINAL] = new HeaderInfo(P_PREFERRED_IDENTITY_ORDINAL, HeaderInfo.ADDRESS, false, true, true);
        __types[P_USER_DATABASE_ORDINAL] = new HeaderInfo(P_USER_DATABASE_ORDINAL, false);
        __types[P_VISITED_NETWORK_ID_ORDINAL] = new HeaderInfo(P_VISITED_NETWORK_ID_ORDINAL, false);
        __types[PATH_ORDINAL] = new HeaderInfo(PATH_ORDINAL, HeaderInfo.ADDRESS, false, true, true);
        __types[PRIVACY_ORDINAL] = new HeaderInfo(PRIVACY_ORDINAL, false); 
        __types[RACK_ORDINAL] = new HeaderInfo(RACK_ORDINAL, HeaderInfo.STRING, true, false, false);
        __types[REASON_ORDINAL] = new HeaderInfo(REASON_ORDINAL, true);
        __types[REFER_SUB_ORDINAL] = new HeaderInfo(REFER_SUB_ORDINAL, false);
        __types[REFER_TO_ORDINAL] = new HeaderInfo(REFER_TO_ORDINAL, HeaderInfo.ADDRESS, false, false, false);
        __types[REFERRED_BY_ORDINAL] = new HeaderInfo(REFERRED_BY_ORDINAL, HeaderInfo.ADDRESS, false, false, false);
        __types[REJECT_CONTACT_ORDINAL] = new HeaderInfo(REJECT_CONTACT_ORDINAL, true);
        __types[REPLACES_ORDINAL] = new HeaderInfo(REPLACES_ORDINAL, true);
        __types[REPLY_TO_ORDINAL] = new HeaderInfo(REPLY_TO_ORDINAL, HeaderInfo.ADDRESS, false, false, false);
        __types[REQUEST_DISPOSITION_ORDINAL] = new HeaderInfo(REQUEST_DISPOSITION_ORDINAL, true);
        __types[RESOURCE_PRIORITY_ORDINAL] = new HeaderInfo(RESOURCE_PRIORITY_ORDINAL, true);
        __types[RETRY_AFTER_ORDINAL] = new HeaderInfo(RETRY_AFTER_ORDINAL, HeaderInfo.PARAMETERABLE, false, true, true);
        __types[RSEQ_ORDINAL] = new HeaderInfo(RSEQ_ORDINAL, HeaderInfo.STRING, true, false, false);
        __types[SECURITY_CLIENT_ORDINAL] = new HeaderInfo(SECURITY_CLIENT_ORDINAL, true);
        __types[SECURITY_SERVER_ORDINAL] = new HeaderInfo(SECURITY_SERVER_ORDINAL, true);
        __types[SECURITY_VERIFY_ORDINAL] = new HeaderInfo(SECURITY_VERIFY_ORDINAL, true);
        __types[SERVICE_ROUTE_ORDINAL] = new HeaderInfo(SERVICE_ROUTE_ORDINAL, HeaderInfo.ADDRESS, false, true, true);
        __types[SESSION_EXPIRES_ORDINAL] = new HeaderInfo(SESSION_EXPIRES_ORDINAL, false);
        __types[SIP_ETAG_ORDINAL] = new HeaderInfo(SIP_ETAG_ORDINAL, false);
        __types[SIP_IF_MATCH_ORDINAL] = new HeaderInfo(SIP_IF_MATCH_ORDINAL, false);
        __types[SUBSCRIPTION_STATE_ORDINAL] = new HeaderInfo(SUBSCRIPTION_STATE_ORDINAL, HeaderInfo.PARAMETERABLE, false, false, false);
        __types[TARGET_DIALOG_ORDINAL] = new HeaderInfo(TARGET_DIALOG_ORDINAL, false);
        __types[IN_REPLY_TO_ORDINAL] = new HeaderInfo(IN_REPLY_TO_ORDINAL, true);
        __types[MIN_EXPIRES_ORDINAL] = new HeaderInfo(MIN_EXPIRES_ORDINAL, false);
        __types[MIME_VERSION_ORDINAL] = new HeaderInfo(MIME_VERSION_ORDINAL, true);
        __types[ORGANIZATION_ORDINAL] = new HeaderInfo(ORGANIZATION_ORDINAL, false);
        __types[PRIORITY_ORDINAL] = new HeaderInfo(PRIORITY_ORDINAL, false);
        __types[PROXY_AUTHENTICATE_ORDINAL] = new HeaderInfo(PROXY_AUTHENTICATE_ORDINAL, false);
        __types[PROXY_AUTHORIZATION_ORDINAL] = new HeaderInfo(PROXY_AUTHORIZATION_ORDINAL, false);
        __types[PROXY_REQUIRE_ORDINAL] = new HeaderInfo(PROXY_REQUIRE_ORDINAL, true);
        __types[REQUIRE_ORDINAL] = new HeaderInfo(REQUIRE_ORDINAL, true);
        __types[RETRY_AFTER_ORDINAL] = new HeaderInfo(RETRY_AFTER_ORDINAL, false);
        __types[SERVER_ORDINAL] = new HeaderInfo(SERVER_ORDINAL, false);
        __types[SUBJECT_ORDINAL] = new HeaderInfo(SUBJECT_ORDINAL, false);
        __types[SUPPORTED_ORDINAL] = new HeaderInfo(SUPPORTED_ORDINAL, true);
        __types[TIMESTAMP_ORDINAL] = new HeaderInfo(TIMESTAMP_ORDINAL, true);
        __types[UNSUPPORTED_ORDINAL] = new HeaderInfo(UNSUPPORTED_ORDINAL, true);
        __types[USER_AGENT_ORDINAL] = new HeaderInfo(USER_AGENT_ORDINAL, false);
        __types[WARNING_ORDINAL] = new HeaderInfo(WARNING_ORDINAL, true);
        __types[WWW_AUTHENTICATE_ORDINAL] = new HeaderInfo(WWW_AUTHENTICATE_ORDINAL, false);
        __types[CALL_ID_ORDINAL] = new HeaderInfo(CALL_ID_ORDINAL, HeaderInfo.STRING, true, false, false);
        __types[FROM_ORDINAL] = new HeaderInfo(FROM_ORDINAL, HeaderInfo.ADDRESS, true, false, false);
        __types[TO_ORDINAL] = new HeaderInfo(TO_ORDINAL, HeaderInfo.ADDRESS, true, false, false);
        __types[ROUTE_ORDINAL] = new HeaderInfo(ROUTE_ORDINAL, HeaderInfo.ADDRESS, true, true, true);
        __types[RECORD_ROUTE_ORDINAL] = new HeaderInfo(RECORD_ROUTE_ORDINAL, HeaderInfo.ADDRESS, true, true, true);
        __types[VIA_ORDINAL] = new HeaderInfo(VIA_ORDINAL, HeaderInfo.VIA, true, true, false);
        __types[CSEQ_ORDINAL] = new HeaderInfo(CSEQ_ORDINAL, HeaderInfo.STRING, true, false, false);
	}
    
    public static HeaderInfo getType(Buffer name)
    {
    	int ordinal = CACHE.getOrdinal(name);
    	if (ordinal > 0)
    		return __types[ordinal];
        return new HeaderInfo(-1, false);
    }

	public static class HeaderInfo
    {	
		public static final int STRING = 0;
		public static final int PARAMETERABLE = 1;
		public static final int ADDRESS = 2;
		public static final int VIA = 3;
        
        private boolean _system;
        private int _type;
        private boolean _list;
        private int _ordinal;
        private boolean _merge;
		
		public HeaderInfo(int ordinal, int type, boolean system, boolean list, boolean merge) 
        {
			_ordinal = ordinal;
			_system = system;
			_type = type;
            _list = list;
            _merge = merge;
		}
        
        public HeaderInfo(int ordinal, boolean list, boolean merge)
        {
            this(ordinal, HeaderInfo.STRING, false, list, merge);
        }
        
        public HeaderInfo(int ordinal, boolean list)
        {
            this(ordinal, list, list);
        }
		
        public int getOrdinal()
        {
        	return _ordinal;
        }
        
		public boolean isSystem() 
        {
			return _system;
		}
        
        public boolean isList()
        {
            return _list;
        }
		
		public int getType() 
        {
			return _type;
		}
		
		public boolean isMerge()
		{
			return _merge; 
		}
	}
}

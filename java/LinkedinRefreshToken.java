
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.LinkedInApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

/*
 * This utility is to refresh linkedin tokens without UI / user interaction.
 *
 * @author iSocialLabs Team
 * Contact: info@isociallabs.com
 *
 *
 * Dependencies:
 * scribe-1.3.0.jar
 * log4j-1.2.15.jar
 * commons-logging-1.1.1.jar
 * commons-codec-1.6.jar
 */
public class LinkedinRefreshToken {
	private static final Log LOG = LogFactory
			.getLog(LinkedinRefreshToken.class);

	// TODO - Change with your app api key
	private static final String apiKey = "dm9refrri2bo";
	// TODO Change with your app api secret key
	private static final String secretKey = "tr33462O6amHysf5yx";

	private static OAuthService service = null;

	public static final Pattern p = Pattern
			.compile("<div class=\"access-code\">[0-9]*</div>");
	public static final Pattern codePattern = Pattern.compile("\\d+");

	public static OAuthService getLinkedinService() {

		if (service == null) {
			service = new ServiceBuilder().provider(LinkedInApi.class)
					.apiKey(apiKey).apiSecret(secretKey)
					.scope("r_basicprofile,rw_nus").debug().build();
		}
		return service;
	}

	public String test(String token, String tokenSecret) {

		Token userToken = new Token(token, tokenSecret);
		// get the request token
		Token reqToken = getLinkedinService().getRequestToken();
		// create the authenticate request

		OAuthRequest authRequest = new OAuthRequest(Verb.POST,
				"https://www.linkedin.com/uas/oauth/authenticate?oauth_token="
						+ reqToken.getToken());
		// sign the request
		getLinkedinService().signRequest(userToken, authRequest);
		LOG.debug("request1=" + authRequest.getCompleteUrl());
		// invoke the URL
		Response response = authRequest.send();

		// System.out.println("Response " + response.getBody());
		showResponse(response);

		// extract the verifier from the response
		String verifier = null;
		if (response != null && response.isSuccessful()) {
			verifier = extractVerifier(response);
		} else {
			throw new RuntimeException(
					"Could not access user token... Notify user!");
		}
		// get the new/updated access token with the ex
		if (verifier != null) {
			// this should return a string like:
			// oauth_token=a7375c17-4406-4a26-ba0c-525012ff66c8&oauth_token_secret=d60441fe-696c-4fe1-b0e7-b9614d2d56e6&oauth_expires_in=5183999&oauth_authorization_expires_in=5183999
			String authResponse = getAccessTokenResponse(reqToken, verifier);

			/*
			 * Parse authResponse and store token / token_secret &
			 * oauth_expire_in in appropriate place for your application
			 */
			StringTokenizer st = new StringTokenizer(authResponse, "&");

			while (st.hasMoreTokens()) {
				String tokenString = st.nextToken();
				String[] keyValue = tokenString.split("=");
				if (("oauth_token_secret").equals(keyValue[0])) {
					// TODO Do something
				} else if (("oauth_token").equals(keyValue[0])) {
					// TODO Do something
				} else if (("oauth_expires_in").equals(keyValue[0])) {
					// TODO Do something
				}

			}

		}

		return "";
	}

	private String extractVerifier(Response response) throws RuntimeException {
		String responseStr = response.getBody();
		String v = getVerifier(responseStr);
		if (v == null)
			throw new RuntimeException(
					"Did not find access-code in the response... Notify user!");

		return getVerifier(responseStr);
	}

	private String getVerifier(String str) {
		String v = null;
		Matcher m = p.matcher(str);
		if (m.find()) {
			String line = m.group();
			Matcher codeMatcher = codePattern.matcher(line);
			if (codeMatcher.find())
				v = codeMatcher.group();
		}

		return v;
	}

	public String getAccessTokenResponse(Token reqToken, String verify) {
		String response = null;
		Verifier v = new Verifier(verify);
		try {

			Token aToken = getLinkedinService().getAccessToken(reqToken, v);
			response = aToken.getRawResponse();
		} catch (Exception e) {
			// TODO do something...
		}

		return response;
	}

	private void showResponse(Response response) {
		if (response != null) {
			LOG.debug("headers=" + response.getHeaders());
			if (response.isSuccessful()) {
				LOG.debug(response.getBody());
			}
		}
	}

	public static void main(String args[]) throws Exception {

		// TODO replace with token which your want to refresh
		String LINKEDIN_TOKEN = "01cf3ba0-b18e-4dc2-b92a-3ba319d438cd9d";
		// TODO replace with tokensecret for the token which you want to refresh
		String LINKEDIN_TOKEN_SECRET = "8b27f20f-d5ec-41ac-a9a6-e23b5478be314c";

		LinkedinRefreshToken util = new LinkedinRefreshToken();
		System.out
				.println("Running Refresh Token for token" + LINKEDIN_TOKEN + "  " + " tokenSecret " + LINKEDIN_TOKEN_SECRET);

		System.out.println("Refresh token " + LINKEDIN_TOKEN + "  "	+ LINKEDIN_TOKEN_SECRET);

		util.test(LINKEDIN_TOKEN, LINKEDIN_TOKEN_SECRET);


	}

}

package dev.nipafx.livefx.twitch;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.nipafx.livefx.twitch.store.AppCredentials;
import dev.nipafx.livefx.twitch.store.Credentials;
import dev.nipafx.livefx.twitch.store.UserCredentials;
import dev.nipafx.livefx.twitch.store.UserToken;
import org.microhttp.EventLoop;
import org.microhttp.Handler;
import org.microhttp.Options;
import org.microhttp.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

public class TwitchAuthorizer {

	private static final URI VALIDATE_ENDPOINT = URI.create("https://id.twitch.tv/oauth2/validate");
	private static final URI AUTHORIZE_ENDPOINT = URI.create("https://id.twitch.tv/oauth2/authorize");
	private static final URI TOKEN_ENDPOINT = URI.create("https://id.twitch.tv/oauth2/token");
	private static final String AUTHORIZE_ENDPOINT_USER_TOKEN_PARAMS = "?response_type=code&client_id=%s&redirect_uri=http://localhost:%s&scope=%s";
	private static final String USER_TOKEN_SCOPE = "channel:read:redemptions channel:manage:redemptions";

	private static final int LOCAL_HOST_PORT = 3000;
	private static final Pattern AUTH_CODE_FORWARD_URI = Pattern.compile("\\/\\?.*code=(?<code>[^&]+).*&scope=(?<scope>[^&]+).*");

	private static final Logger LOG = LoggerFactory.getLogger(TwitchAuthorizer.class);

	private final HttpClient http;
	private final ObjectMapper json;
	private final Path credentialStore;

	public TwitchAuthorizer(HttpClient http, ObjectMapper json, Path credentialStore) {
		this.http = requireNonNull(http);
		this.json = requireNonNull(json);
		this.credentialStore = requireNonNull(credentialStore);
	}

	public TwitchCredentials createCredentials() throws IOException, InterruptedException {
		LOG.info("Gathering credentials...");

		LOG.debug("Loading (partial) credentials from store...");
		var storedCredentials = loadFromCredentialStore();

		LOG.debug("Requesting needed tokens...");
		// request app and user token from API because they expire pretty quickly
		var appCredentials = fetchAppToken(storedCredentials.app());
		var userToken = fetchUserToken(storedCredentials.user(), storedCredentials.app());

		LOG.info("Gathered all needed credentials");
		var resolvedCredentials = new TwitchCredentials(
				storedCredentials.user().name(),
				storedCredentials.user().id(),
				userToken.access(),
				storedCredentials.app().id(),
				// if there's no token at this point, the code above has a bug
				appCredentials.token().get()
		);
		updateCredentialStore(storedCredentials, appCredentials, userToken);

		return resolvedCredentials;
	}

	private Credentials loadFromCredentialStore() throws IOException {
		return json
				.readerFor(Credentials.class)
				.readValue(credentialStore.toFile());
	}

	private void updateCredentialStore(Credentials storedCredentials, AppCredentials appCredentials, UserToken userToken) throws IOException {
		var appUnchanged = storedCredentials.app().equals(appCredentials);
		var userUnchanged = storedCredentials.user().token().filter(userToken::equals).isPresent();
		if (appUnchanged && userUnchanged)
			return;

		LOG.debug("Updating credential store...");
		var updatedCredentials = new Credentials(
				new UserCredentials(
						storedCredentials.user().name(),
						storedCredentials.user().id(),
						Optional.of(userToken)),
				appCredentials);
		json
				.writerWithDefaultPrettyPrinter()
				.writeValue(credentialStore.toFile(), updatedCredentials);
	}

	private AppCredentials fetchAppToken(AppCredentials appCredentials) throws IOException, InterruptedException {
		if (appCredentials.token().isPresent() && isValidToken(appCredentials.token().get())) {
			LOG.debug("Stored app token is still valid and will be reused");
			return appCredentials;
		}

		var appToken = requestAppToken(appCredentials.id(), appCredentials.secret());
		return new AppCredentials(appCredentials.id(), appCredentials.secret(), Optional.of(appToken));
	}

	private boolean isValidToken(String token) throws IOException, InterruptedException {
		var request = HttpRequest
				.newBuilder(VALIDATE_ENDPOINT)
				.GET()
				.header("Authorization", "OAuth " + token)
				.build();
		var validationResponse = http.send(request, BodyHandlers.ofString());
		if (validationResponse.statusCode() != 200)
			return false;

		var validationResponseJson = json.readTree(validationResponse.body());
		var expirationSeconds = validationResponseJson.get("expires_in");
		if (expirationSeconds == null || !expirationSeconds.isInt())
			return false;

		return expirationSeconds.intValue() > 60;
	}

	private String requestAppToken(String appId, String appSecret) throws IOException, InterruptedException {

		// TODO:
		//  This way to request an app token does not seem to allow requesting permissions (a "scope)
		//  and hence the token doesn't actually work for the IRC server. Weird.

		LOG.debug("Requesting app token...");
		var request = HttpRequest
				.newBuilder(TOKEN_ENDPOINT)
				.POST(BodyPublishers.ofString("client_id=%s&client_secret=%s&grant_type=client_credentials".formatted(appId, appSecret)))
				.header("Content-Type", "application/x-www-form-urlencoded")
				.build();
		var appTokenAuthResponse = http.send(request, BodyHandlers.ofString());
		if (appTokenAuthResponse.statusCode() != 200) {
			var message = "Failed to request app token; status code: %s / response: %s".formatted(appTokenAuthResponse.statusCode(), appTokenAuthResponse.body());
			LOG.error(message);
			throw new IOException(message);
		}

		var appTokenAuthResponseJson = json.readTree(appTokenAuthResponse.body());
		var appTokenNode = appTokenAuthResponseJson.get("access_token");
		if (appTokenNode == null || appTokenNode.textValue().isBlank()) {
			var message = "App token response contained no access_token:  %s".formatted(appTokenAuthResponse.body());
			LOG.error(message);
			throw new IOException(message);
		}
		return appTokenNode.textValue();
	}

	private UserToken fetchUserToken(UserCredentials userCredentials, AppCredentials appCredentials) throws IOException, InterruptedException {
		if (userCredentials.token().isPresent()) {
			if (isValidToken(userCredentials.token().get().access())) {
				LOG.debug("Stored user token is still valid and will be reused");
				return userCredentials.token().get();
			}

			LOG.debug("Stored user token expired. Trying a refresh...");
			var refreshedToken = refreshToken(appCredentials, userCredentials.token().get().refresh());
			if (refreshedToken.isPresent()) {
				LOG.debug("Refresh successful");
				return refreshedToken.get();
			}
			LOG.debug("Refresh unsuccessful");
		}

		// we need an auth code to be able to request the token
		LOG.info("A new auth code is needed to fetch a new user access token");
		var userAuthCode = getAuthCode(appCredentials.id());
		return resolveAuthCodeToUserToken(appCredentials.id(), appCredentials.secret(), userAuthCode);
	}

	private Optional<UserToken> refreshToken(AppCredentials appCredentials, String refreshToken) throws IOException, InterruptedException {
		var request = HttpRequest
				.newBuilder(TOKEN_ENDPOINT)
				.POST(BodyPublishers.ofString(
						"grant_type=refresh_token&refresh_token=%s&client_id=%s&client_secret=%s&grant_type=client_credentials"
								.formatted(refreshToken, appCredentials.id(), appCredentials.secret())))
				.header("Content-Type", "application/x-www-form-urlencoded")
				.build();
		var refreshResponse = http.send(request, BodyHandlers.ofString());
		if (refreshResponse.statusCode() != 200) {
			LOG.warn("Unexpected response with status code %d when refreshing user token: %s"
					.formatted(refreshResponse.statusCode(), refreshResponse.body()));
			return Optional.empty();
		}

		return Optional.of(extractUserTokenFromResponse(refreshResponse.body()));
	}

	private UserToken extractUserTokenFromResponse(String userTokenAuthResponse) throws IOException {
		var userTokenAuthResponseJson = json.readTree(userTokenAuthResponse);
		var userTokenNode = userTokenAuthResponseJson.get("access_token");
		if (userTokenNode == null || userTokenNode.textValue().isBlank()) {
			var message = "User token response contained no access_token:  %s".formatted(userTokenAuthResponse);
			LOG.error(message);
			throw new IOException(message);
		}
		var refreshTokenNode = userTokenAuthResponseJson.get("refresh_token");
		if (refreshTokenNode == null || userTokenNode.textValue().isBlank()) {
			var message = "User token response contained no refresh_token:  %s".formatted(userTokenAuthResponse);
			LOG.error(message);
			throw new IOException(message);
		}

		return new UserToken(userTokenNode.textValue(), refreshTokenNode.textValue());
	}

	private static String getAuthCode(String appId) throws IOException, InterruptedException {
		var deferredAuthCode = new BlockingValue<String>();
		var serverShutdown = launchLocalServer(deferredAuthCode);
		visitAuthorizationUrl(appId);

		var authCode = deferredAuthCode.get();
		serverShutdown.shutdown();
		return authCode;
	}

	private static ServerShutdown launchLocalServer(BlockingValue<String> deferredAuthCode) throws IOException {
		LOG.debug("Launching a server on localhost to capture the auth code...");
		var options = new Options()
				.withHost("localhost")
				.withPort(LOCAL_HOST_PORT)
				.withConcurrency(1);
		Handler handler = (request, callback) -> {
			extractAuthCodeFromUri(request.uri()).ifPresent(deferredAuthCode::set);
			callback.accept(new Response(200, "OK", List.of(), new byte[0]));
		};
		var eventLoop = new EventLoop(options, handler);
		eventLoop.start();
		return () -> {
			eventLoop.stop();
			eventLoop.join();
		};
	}

	private static Optional<String> extractAuthCodeFromUri(String uri) {
		var authCodeMatcher = AUTH_CODE_FORWARD_URI.matcher(uri);
		if (!authCodeMatcher.matches()) {
			LOG.warn("Caught forward that didn't match the auth code regex!");
			return Optional.empty();
		}

		var code = authCodeMatcher.group("code");
		LOG.debug("Caught auth code!");
		return Optional.of(code);
	}

	private static void visitAuthorizationUrl(String appId) throws IOException {
		LOG.debug("Visiting authorization URL to trigger forward to localhost with auth code...");
		var queryString = AUTHORIZE_ENDPOINT_USER_TOKEN_PARAMS
				.formatted(appId, LOCAL_HOST_PORT, URLEncoder.encode(USER_TOKEN_SCOPE, StandardCharsets.UTF_8));
		var authorizationUrl = AUTHORIZE_ENDPOINT + queryString;

		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
			Desktop.getDesktop().browse(URI.create(authorizationUrl));
		else
			// maybe we're on Linux - let's hope this works 🤞🏾
			new ProcessBuilder("xdg-open", authorizationUrl).start();
	}

	private UserToken resolveAuthCodeToUserToken(String appId, String appSecret, String userAuthCode) throws IOException, InterruptedException {
		LOG.debug("Requesting user token with auth code...");
		var request = HttpRequest
				.newBuilder(TOKEN_ENDPOINT)
				.POST(BodyPublishers.ofString("client_id=%s&client_secret=%s&code=%s&grant_type=authorization_code&redirect_uri=http://localhost:3000"
						.formatted(appId, appSecret, userAuthCode)))
				.header("Content-Type", "application/x-www-form-urlencoded")
				.build();
		var userTokenAuthResponse = http.send(request, BodyHandlers.ofString());
		if (userTokenAuthResponse.statusCode() != 200) {
			var message = "Failed to request user token; status code: %s / response: %s".formatted(userTokenAuthResponse.statusCode(), userTokenAuthResponse.body());
			LOG.error(message);
			throw new IOException(message);
		}

		return extractUserTokenFromResponse(userTokenAuthResponse.body());
	}

	private static class BlockingValue<VALUE> {

		private final BlockingQueue<VALUE> queue = new ArrayBlockingQueue<>(1);

		public VALUE get() throws InterruptedException {
			return queue.take();
		}

		public void set(VALUE value) {
			queue.clear();
			var added = queue.offer(value);
			if (!added)
				throw new IllegalStateException("Could not add the value to the cleared queue - was `set` used concurrently?");
		}

	}

	private interface ServerShutdown {

		void shutdown() throws InterruptedException;

	}

}

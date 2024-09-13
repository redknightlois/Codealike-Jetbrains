/*
 * Copyright (c) 2022-2023. All rights reserved to Torc LLC.
 */
package com.codealike.client.core.api;

import com.codealike.client.core.internal.dto.ActivityInfo;
import com.codealike.client.core.internal.dto.HealthInfo;
import com.codealike.client.core.internal.dto.PluginSettingsInfo;
import com.codealike.client.core.internal.dto.ProfileInfo;
import com.codealike.client.core.internal.dto.SolutionContextInfo;
import com.codealike.client.core.internal.dto.UserConfigurationInfo;
import com.codealike.client.core.internal.dto.Version;
import com.codealike.client.core.internal.startup.PluginContext;
import com.codealike.client.core.internal.utils.LogManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.UUID;

/**
 * Api class to communicate with Codealike server.
 *
 * @author Daniel, pvmagacho
 * @version 1.6.0.0
 */
public class ApiClient {
    private static final LogManager LOG = LogManager.INSTANCE;

    private static final String X_EAUTH_CLIENT_HEADER = "X-Eauth-Client";
    private static final String X_EAUTH_TOKEN_HEADER = "X-Api-Token";
    private static final String X_EAUTH_IDENTITY_HEADER = "X-Api-Identity";

    private static final int CONNECT_TIMEOUT = 30000;
    private static final int READ_TIMEOUT = 5000;

    private final HttpClient httpClient;
    private final String apiUrl;

    private final String identity;
    private final String token;

    /**
     * Create a new API client. Used to communicate with the Codealike remote server.
     *
     * @param identity the user identity
     * @param token    the user token
     * @return the created APIClient instance
     * @throws KeyManagementException if any error with token occurs
     */
    public static ApiClient tryCreateNew(String identity, String token) throws KeyManagementException {
        return new ApiClient(identity, token);
    }

    /**
     * Create a new API client. Used to communicate with the Codealike remote server.
     *
     * @return the created APIClient instance
     * @throws KeyManagementException if any error with token occurs
     */
    public static ApiClient tryCreateNew() throws KeyManagementException {
        return new ApiClient("", "");
    }

    /**
     * Constructor for `ApiClient` class.
     * <p>
     * It initializes the HttpClient, apiUrl, identity, and token fields.
     *
     * @param identity A String representing the identity, if it's null it will be assigned an empty string
     * @param token    A String representing the token, if it's null it will be assigned an empty string
     * @throws KeyManagementException if a failure occurred during the SSL context configuration in the HttpClient
     */
    private ApiClient(String identity, String token) throws KeyManagementException {
        this.httpClient = createHttpClient();
        this.apiUrl = PluginContext.getInstance().getConfiguration().getApiUrl();
        this.identity = identity != null ? identity : "";
        this.token = token != null ? token : "";
    }

    /**
     * Factory method that creates and returns an HttpClient object with specified settings.
     *
     * @return HttpClient which is configured for SSL context, HTTP version, connection timeout, and follow redirects
     * @throws KeyManagementException if a failure occurred during retrieving or setting SSL Context or SSL context initialization
     */
    private HttpClient createHttpClient() throws KeyManagementException {
        TrustManager[] trustManagers = createTrustManagers();
        SSLContext sslContext = createSslContext(trustManagers);

        return HttpClient.newBuilder()
                .sslContext(sslContext)
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofMillis(CONNECT_TIMEOUT))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    /**
     * Factory method that creates and returns an array of TrustManager objects with custom settings.
     *
     * @return Array of TrustManager objects
     */
    private TrustManager[] createTrustManagers() {
        return new TrustManager[]{new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }
        }};
    }

    /**
     * Factory method that creates and returns an SSLContext object with specified TrustManagers.
     *
     * @param trustManagers an array of trust managers to use for initializing the SSL context
     * @return SSLContext which is configured with the given trust managers
     * @throws KeyManagementException if a failure occurred during initializing the SSL context
     */
    private SSLContext createSslContext(TrustManager[] trustManagers) throws KeyManagementException {
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustManagers, new SecureRandom());
            return sslContext;
        } catch (NoSuchAlgorithmException | KeyManagementException exception) {
            LOG.logWarn("Error initializing SSL context: " + exception.getMessage());
            throw new KeyManagementException("Failed to initialize SSL context", exception);
        }
    }

    /**
     * Get the plugin settings from the remote server.
     *
     * @return the {@link ApiResponse} instance with {@link PluginSettingsInfo} information
     */
    public static ApiResponse<PluginSettingsInfo> getPluginSettings() {
        ObjectMapper mapper = new ObjectMapper();
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(CONNECT_TIMEOUT))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://codealike.com/api/v2/public/PluginsConfiguration"))
                .header("Accept", "application/json")
                .timeout(Duration.ofMillis(READ_TIMEOUT))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                String serializedObject = response.body();
                String normalizedObject = serializedObject.substring(1, serializedObject.length() - 1).replace("\\", "");

                PluginSettingsInfo pluginSettingsInfo = mapper.readValue(normalizedObject, PluginSettingsInfo.class);
                if (pluginSettingsInfo != null) {
                    return new ApiResponse<>(response.statusCode(), response.headers()
                            .firstValue("reason").orElse("OK"), pluginSettingsInfo);
                } else {
                    return new ApiResponse<>(ApiResponse.Status.ClientError,
                            "Problem parsing data from the server.");
                }
            } else {
                return new ApiResponse<>(response.statusCode(), response.headers()
                        .firstValue("reason").orElse("Error"));
            }
        } catch (Exception exception) {
            return new ApiResponse<>(ApiResponse.Status.ClientError,
                    String.format("Problem parsing data from the server: %s", exception.getMessage()));
        }
    }

    /**
     * Log the plugin health information to the remote server.
     *
     * @param healthInfo the health information object to update
     */
    public void logHealth(HealthInfo healthInfo) {
        try {
            URI uri = URI.create(apiUrl + "/health");

            String healthInfoLog = serializeDataToJson(healthInfo);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header(X_EAUTH_IDENTITY_HEADER, this.identity)
                    .header(X_EAUTH_TOKEN_HEADER, this.token)
                    .header(X_EAUTH_CLIENT_HEADER, "intellij")
                    .timeout(Duration.ofMillis(READ_TIMEOUT))
                    .PUT(HttpRequest.BodyPublishers.ofString(healthInfoLog))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());

            int statusCode = response.statusCode();
            String reasonPhrase = response.headers().firstValue("reason").orElse("No reason provided");

            LOG.logInfo("Health log response: " + statusCode + " - " + reasonPhrase);

        } catch (JsonProcessingException exception) {
            LOG.logWarn("Error processing JSON for health log: " + exception.getMessage());
        } catch (Exception exception) {
            LOG.logWarn("Error sending health log request: " + exception.getMessage());
        }
    }

    /**
     * Do an account authentication using the Codealike token.
     *
     * @return the {@link ApiResponse} instance
     */
    public ApiResponse<Void> tokenAuthenticate() {
        try {
            URI uri = URI.create(apiUrl + "/account/" + this.identity + "/authorized");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Accept", "application/json")
                    .header(X_EAUTH_IDENTITY_HEADER, this.identity)
                    .header(X_EAUTH_TOKEN_HEADER, this.token)
                    .header(X_EAUTH_CLIENT_HEADER, "intellij")
                    .timeout(Duration.ofMillis(READ_TIMEOUT))
                    .GET()
                    .build();

            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());

            return new ApiResponse<>(response.statusCode(), response.headers().firstValue("reason").orElse("OK"));
        } catch (Exception exception) {
            LOG.logWarn("Error during token authentication " + exception.getMessage());
            return new ApiResponse<>(ApiResponse.Status.ConnectionProblems);
        }
    }

    /**
     * Get version for intellij plugin.
     *
     * @return the {@link ApiResponse} instance with {@link Version} information
     */
    public ApiResponse<Version> version() {
        String path = "/version?client=intellij";
        return get(path, Version.class);
    }

    /**
     * Get solution context information.
     *
     * @param projectId the current project id being tracker
     * @return the {@link ApiResponse} instance with {@link SolutionContextInfo} information
     */
    public ApiResponse<SolutionContextInfo> getSolutionContext(UUID projectId) {
        String path = "/solution/" + projectId.toString();
        return get(path, SolutionContextInfo.class);
    }

    /**
     * Get project information.
     *
     * @param username the profile username
     * @return the {@link ApiResponse} instance with {@link ProfileInfo} information
     */
    public ApiResponse<ProfileInfo> getProfile(String username) {
        String path = "/account/" + username + "/profile";
        return get(path, ProfileInfo.class);
    }

    /**
     * Get user configuration information.
     *
     * @param username the profile username
     * @return the {@link ApiResponse} instance with {@link UserConfigurationInfo} information
     */
    public ApiResponse<UserConfigurationInfo> getUserConfiguration(String username) {
        String path = "/account/" + username + "/config";
        return get(path, UserConfigurationInfo.class);
    }

    /**
     * Register the project being tracked with the remote server.
     *
     * @param projectId the project identifier to track
     * @param name      the project name
     * @return the {@link ApiResponse} instance
     */
    public ApiResponse<Void> registerProjectContext(UUID projectId, String name) {
        String path = "/solution";
        return post(path, new SolutionContextInfo(projectId, name));
    }

    /**
     * Post project activity information.
     *
     * @param info the activity information object
     * @return the {@link ApiResponse} instance
     */
    public ApiResponse<Void> postActivityInfo(ActivityInfo info) {
        String path = "/activity";
        return post(path, info);
    }

    /**
     * Private method to do an API POST.
     */
    @NotNull
    private ApiResponse<Void> post(String path, Object data) {
        try {
            URI uri = URI.create(apiUrl + path);
            String jsonData = serializeDataToJson(data);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header(X_EAUTH_IDENTITY_HEADER, this.identity)
                    .header(X_EAUTH_TOKEN_HEADER, this.token)
                    .header(X_EAUTH_CLIENT_HEADER, "intellij")
                    .timeout(Duration.ofMillis(READ_TIMEOUT))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonData))
                    .build();
            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());

            return new ApiResponse<>(response.statusCode(),
                    response.headers().firstValue("reason").orElse("OK"));
        } catch (JsonProcessingException e) {
            return new ApiResponse<>(ApiResponse.Status.ClientError,
                    String.format("Problem parsing data to JSON: %s", e.getMessage()));
        } catch (Exception e) {
            return new ApiResponse<>(ApiResponse.Status.ConnectionProblems);
        }
    }

    /**
     * Private method to do an API GET.
     */
    private <T> ApiResponse<T> get(String path, Class<T> type) {
        try {
            URI uri = URI.create(apiUrl + path);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header(X_EAUTH_IDENTITY_HEADER, this.identity)
                    .header(X_EAUTH_TOKEN_HEADER, this.token)
                    .header(X_EAUTH_CLIENT_HEADER, "intellij")
                    .timeout(Duration.ofMillis(READ_TIMEOUT))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = PluginContext.getInstance().getJsonMapper();
                T contextInfo = mapper.readValue(response.body(), type);
                if (contextInfo != null) {
                    return new ApiResponse<>(response.statusCode(),
                            response.headers().firstValue("reason").orElse("OK"), contextInfo);
                } else {
                    return new ApiResponse<>(ApiResponse.Status.ClientError,
                            "Problem parsing data from the server.");
                }
            } else {
                return new ApiResponse<>(response.statusCode(),
                        response.headers().firstValue("reason").orElse("Error"));
            }
        } catch (Exception exception) {
            return new ApiResponse<>(ApiResponse.Status.ClientError,
                    String.format("Problem parsing data from the server: %s", exception.getMessage()));
        }
    }

    /**
     * Converts a given object to its JSON representation.
     *
     * @param data the object that needs to be converted to JSON.
     * @return a String containing the JSON representation of the input object.
     * @throws JsonProcessingException if the input object could not be converted to JSON.
     */
    private String serializeDataToJson(Object data) throws JsonProcessingException {
        ObjectWriter writer = PluginContext.getInstance().getJsonWriter();
        return writer.writeValueAsString(data);
    }
}

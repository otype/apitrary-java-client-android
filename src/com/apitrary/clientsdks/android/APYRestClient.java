package com.apitrary.clientsdks.android;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * Main class of the apitrary client library.
 * 
 * @author Sebastian Engel <se@apitrary.com>
 * 
 */
public class APYRestClient {

    /**
     * The apitrary log tag used for log output in Android log output.
     */
    private static final String APITARY_LOG_TAG = "Apitrary";
    
    /**
     * The connection timeout in milliseconds.
     */
    private static final int CONNECTION_TIMEOUT_MS = 15000;

    /**
     * The key of the JSON "result" array/object in a response.
     */
    private static final String JSON_RESPONSE_RESULT_KEY = "result";

    /**
     * The key for the ID value of a single result object in a response.
     */
    private static final String JSON_RESPONSE_ID_KEY = "_id";

    /**
     * The key for a single data object in a response.
     */
    private static final String JSON_RESPONSE_DATA_KEY = "_data";

    /**
     * The base URL of the target API.
     */
    private String apiBaseUrl;

    /**
     * The API ID used to identify the API to use.
     */
    private String apiId;

    /**
     * The version of the API to use.
     */
    private int apiVersion;

    /**
     * Constructs an instance of {@link APYRestClient} used to interact with an apitrary API.
     * 
     * @param apiBaseUrl
     *            the base URL of the target API
     * @param apiId
     *            the API ID used to identify the API to use
     * @param apiVersion
     *            the version of your API as an integer >= 1
     * @throws IllegalArgumentException
     *             if the given apiBaseUrl is null or empty,
     *             if the given apiId is null or empty,
     *             if the given API version is less or equal to 0
     */
    public APYRestClient(String apiBaseUrl, String apiId, int apiVersion) throws IllegalArgumentException {
        // Validate the parameters
        if (APYUtils.isNullOrEmpty(apiBaseUrl)) {
            throw new IllegalArgumentException(
                    "The API base URL must not be null or empty.");
        }

        if (APYUtils.isNullOrEmpty(apiId)) {
            throw new IllegalArgumentException(
                    "The API ID must not be null or empty.");
        }

        if (apiVersion <= 0) {
            throw new IllegalArgumentException(
                    "The API version must be an integer >= 1.");
        }

        this.apiBaseUrl = apiBaseUrl;
        this.apiId = apiId;
        this.apiVersion = apiVersion;
    }

    /**
     * Fetches all entities for the given (entity / resource) name.
     * 
     * @param entityName
     *            the entity / resource name
     * @return a list of {@link APYEntity}
     * @throws IllegalArgumentException
     *             if the given entity name is null or empty
     * @throws APYException
     *             if anything went wrong while trying to fetch the entities from the API
     */
    public List<APYEntity> getAll(String entityName) throws IllegalArgumentException, APYException {
        // Validate the parameters
        if (APYUtils.isNullOrEmpty(entityName)) {
            throw new IllegalArgumentException("Parameter 'entityName' is null or empty."
                        + " In order to fetch all entities you must specify the entity type's name.");
        }

        // Prepare and perform the request
        String requestUrl = getApiUrl().concat(entityName);
        HttpURLConnection connection;
        int responseCode;
        String responseMessage;
        try {
            Log.d(APITARY_LOG_TAG, "Sending GET request to " + requestUrl);

            connection = setupGetConnection(requestUrl);
            connection.connect();

            responseCode = connection.getResponseCode();
            responseMessage = connection.getResponseMessage();
        } catch (MalformedURLException e) {
            throw new APYException("Malformed URL '" + requestUrl, e);
        } catch (IOException e) {
            throw new APYException("Unsuccessful request to " + requestUrl, e);
        }

        // Handle the response
        List<APYEntity> resultEntities = new ArrayList<APYEntity>();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            // Everything is fine, so extract the entities from the response
            try {
                JSONObject responseJsonObject = getJsonObjectFromResponseStream(connection.getInputStream());
                JSONArray jsonResultArray = responseJsonObject.getJSONArray(JSON_RESPONSE_RESULT_KEY);

                JSONObject jsonResultObject = null;
                APYEntity apyEntity = null;
                // Iterate over all result objects
                for (int index = 0; index < jsonResultArray.length(); index++) {
                    jsonResultObject = jsonResultArray.getJSONObject(index);

                    // Convert the _data object into an APYEntity
                    JSONObject jsonDataObject = jsonResultObject.getJSONObject(JSON_RESPONSE_DATA_KEY);
                    apyEntity = APYUtils.convertFromJson(entityName, jsonDataObject);
                    
                    // Get the value for the _id and set it into our APYEntity 
                    apyEntity.setId(jsonResultObject.optString(JSON_RESPONSE_ID_KEY, null));

                    // Filter out the _init object
                    // TODO Remove this as soon as we stopped returning the _init object
                    if (apyEntity.get("_init") != null) {
                        continue;
                    }
                    resultEntities.add(apyEntity);
                }
            } catch (IOException e) {
                throw new APYException("IOException while reading the response stream.", e);
            } catch (JSONException e) {
                throw new APYException(
                        "Response JSON data was in unexpected format and could not be parsed correctly.", e);
            }
        } else {
            Log.i(APITARY_LOG_TAG, 
                    "Could not fetch the entities. HTTP status: " + responseCode + " - " + responseMessage);
            throw new APYException(
                    "Could not fetch the entities. HTTP status: " + responseCode + " - " + responseMessage);
        }
        return resultEntities;
    }
    
    /**
     * Fetches an entity for the given (entity / resource) name and id.
     * 
     * @param entityName
     *            the entity / resource name
     * @param entityId
     *            the id of the entity to fetch
     * @return the entity found, or null if none matched
     * @throws IllegalArgumentException
     *             if the given entity name is null or empty
     * @throws APYException
     *             if anything went wrong while trying to fetch the entity from the API
     */
    public APYEntity get(String entityName, String entityId) throws IllegalArgumentException, APYException {
        // Validate the parameters
        if (APYUtils.isNullOrEmpty(entityName) && APYUtils.isNullOrEmpty(entityId)) {
            throw new IllegalArgumentException("In order to fetch an entity you must specify its name and id.");
        } else if (APYUtils.isNullOrEmpty(entityName)) {
            throw new IllegalArgumentException("Parameter 'entityName' is null or empty."
                    + " In order to fetch an entity you must specify its name and id.");
        } else if (APYUtils.isNullOrEmpty(entityId)) {
            throw new IllegalArgumentException("Parameter 'entityId' is null or empty."
                    + " In order to fetch an entity you must specify its name and id.");
        }

        // Prepare and perform the request
        String requestUrl = getApiUrl().concat(entityName).concat("/").concat(entityId);
        HttpURLConnection connection;
        int responseCode;
        String responseMessage;
        try {
            Log.d(APITARY_LOG_TAG, "Sending GET request to " + requestUrl);

            connection = setupGetConnection(requestUrl);
            connection.connect();

            responseCode = connection.getResponseCode();
            responseMessage = connection.getResponseMessage();
        } catch (MalformedURLException e) {
            throw new APYException("Malformed URL '" + requestUrl, e);
        } catch (IOException e) {
            throw new APYException("Unsuccessful request to " + requestUrl, e);
        }
        
        // Handle the response
        APYEntity resultEntity = null;
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // Everything is fine, so extract the entities from the response
            try {
                JSONObject responseJsonObject = getJsonObjectFromResponseStream(connection.getInputStream());
                JSONObject jsonResultObject = responseJsonObject.getJSONObject(JSON_RESPONSE_RESULT_KEY);

                // Convert the _data object into an APYEntity
                JSONObject jsonDataObject = jsonResultObject.getJSONObject(JSON_RESPONSE_DATA_KEY);
                resultEntity = APYUtils.convertFromJson(entityName, jsonDataObject);
                
                // Get the value for the _id and set it into our APYEntity 
                resultEntity.setId(jsonResultObject.optString(JSON_RESPONSE_ID_KEY, null));
            } catch (IOException e) {
                throw new APYException("IOException while reading the response stream.", e);
            } catch (JSONException e) {
                throw new APYException(
                        "Response JSON data was in unexpected format and could not be parsed correctly.", e);
            }
        } else {
            Log.i(APITARY_LOG_TAG, 
                    "Could not fetch the entity. HTTP status: " + responseCode + " - " + responseMessage);
            throw new APYException(
                    "Could not fetch the entity. HTTP status: " + responseCode + " - " + responseMessage);
        }
        return resultEntity;
    }

    /**
     * Creates the given {@link APYEntity} instance on the backend.
     * If successful, sets the generated ID on the given entity.
     * 
     * @param entity
     *            the entity to create on the backend.
     * @throws IllegalArgumentException
     *             when the given entity is null
     * @throws APYException
     *             if anything went wrong while trying to fetch the entities from the API
     */
    public void create(APYEntity entity) throws IllegalArgumentException, APYException {
        // Validate the parameters
        APYUtils.validateEntity(entity);

        // Create a JSON object for the entity
        JSONObject jsonObject;
        try {
            jsonObject = APYUtils.convertToJson(entity);
        } catch (JSONException e) {
            throw new APYException("Entity could not be converted to JSON.", e);
        }

        // Prepare and perform the request
        String requestUrl = getApiUrl().concat(entity.getName());
        HttpURLConnection connection;
        int responseCode;
        String responseMessage;
        try {
            Log.d(APITARY_LOG_TAG, "Sending POST request to " + requestUrl);

            byte[] jsonBytes = jsonObject.toString().getBytes(); // TODO Use charset parameter?
            connection = setupPostConnection(requestUrl, jsonBytes.length);

            // Write the JSON entity data into the request body
            OutputStream outputStream = new BufferedOutputStream(connection.getOutputStream());
            outputStream.write(jsonBytes);
            outputStream.flush();
            outputStream.close();

            responseCode = connection.getResponseCode();
            responseMessage = connection.getResponseMessage();
        } catch (MalformedURLException e) {
            throw new APYException("Malformed URL '" + requestUrl + "'.", e);
        } catch (IOException e) {
            throw new APYException("Unsuccessful request to " + requestUrl, e);
        }

        // Handle the response
        if (responseCode == HttpURLConnection.HTTP_CREATED) {
            // Everything is fine
            try {
                JSONObject responseJsonObject = getJsonObjectFromResponseStream(connection.getInputStream());
                // Extract the created entity ID
                String entityId = responseJsonObject.getString(JSON_RESPONSE_ID_KEY);
                entity.setId(entityId);
            } catch (IOException e) {
                throw new APYException("IOException while reading the response stream.", e);
            } catch (JSONException e) {
                throw new APYException(
                        "Response JSON data was in unexpected format and could not be parsed correctly.", e);
            }
        } else {
            Log.i(APITARY_LOG_TAG, 
                    "Could not create the entity. HTTP status: " + responseCode + " - " + responseMessage);
            throw new APYException(
                    "Could not create the entity. HTTP status: " + responseCode + " - " + responseMessage);
        }
    }

    /**
     * Updates the given {@link APYEntity} instance on the backend.
     * 
     * @param entity
     *            the entity to update on the backend.
     * @throws IllegalArgumentException
     *             when the given entity is null or has no ID
     * @throws APYException
     *             if anything went wrong while trying to fetch the entities from the API
     */
    public void update(APYEntity entity) throws IllegalArgumentException, APYException {
        // Validate the parameters
        APYUtils.validateEntity(entity);

        if (APYUtils.isNullOrEmpty(entity.getId())) {
            throw new IllegalArgumentException("The entity to update must contain an id.");
        }

        // Create a JSON object for the entity
        JSONObject jsonObject;
        try {
            jsonObject = APYUtils.convertToJson(entity);
        } catch (JSONException e) {
            throw new APYException("Entity could not be converted to JSON.", e);
        }

        // Prepare the request
        String requestUrl = getApiUrl().concat(entity.getName()).concat("/").concat(entity.getId());
        HttpURLConnection connection;
        int responseCode;
        String responseMessage;
        try {
            Log.d(APITARY_LOG_TAG, "Sending PUT request to " + requestUrl);

            byte[] jsonBytes = jsonObject.toString().getBytes(); // TODO Use charset parameter?
            connection = setupPutConnection(requestUrl, jsonBytes.length);

            // Write the JSON entity data into the request body
            OutputStream outputStream = new BufferedOutputStream(connection.getOutputStream());
            outputStream.write(jsonBytes);
            outputStream.flush();
            outputStream.close();

            responseCode = connection.getResponseCode();
            responseMessage = connection.getResponseMessage();
        } catch (MalformedURLException e) {
            throw new APYException("Malformed URL '" + requestUrl + "'.", e);
        } catch (IOException e) {
            throw new APYException("Unsuccessful request to " + requestUrl, e);
        }

        // Handle the response
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // Everything is fine
            try {
                JSONObject responseJsonObject = getJsonObjectFromResponseStream(connection.getInputStream());
                JSONObject jsonResultObject = responseJsonObject.getJSONObject(JSON_RESPONSE_RESULT_KEY);

                // Convert the _data object into an APYEntity
                JSONObject jsonDataObject = jsonResultObject.getJSONObject(JSON_RESPONSE_DATA_KEY);
                entity = APYUtils.convertFromJson(entity.getName(), jsonDataObject);
                
                // Get the value for the _id and set it into our APYEntity 
                entity.setId(jsonResultObject.optString(JSON_RESPONSE_ID_KEY, null));
            } catch (IOException e) {
                throw new APYException("IOException while reading the response stream.", e);
            } catch (JSONException e) {
                throw new APYException(
                        "Response JSON data was in unexpected format and could not be parsed correctly.", e);
            }
        } else {
            Log.i(APITARY_LOG_TAG, 
                    "Could not update the entity. HTTP status: " + responseCode + " - " + responseMessage);
            throw new APYException(
                    "Could not update the entity. HTTP status: " + responseCode + " - " + responseMessage);
        }
    }

    /**
     * Delete the given {@link APYEntity} instance on the backend.
     * 
     * @param entity
     *            the entity to delete on the backend.
     * @throws IllegalArgumentException
     *             when the given entity is null or has no ID
     * @throws APYException
     *             if anything went wrong while trying to fetch the entities from the API
     */
    public void delete(APYEntity entity) throws IllegalArgumentException, APYException {
        // Validate the parameters
        APYUtils.validateEntity(entity);

        if (APYUtils.isNullOrEmpty(entity.getId())) {
            throw new IllegalArgumentException("The entity to delete must contain an id.");
        }

        // Prepare and perform the request
        String requestUrl = getApiUrl().concat(entity.getName()).concat("/").concat(entity.getId());
        HttpURLConnection connection;
        int responseCode;
        String responseMessage;
        try {
            Log.d(APITARY_LOG_TAG, "Sending DELETE request to " + requestUrl);

            connection = setupDeleteConnection(requestUrl);
            connection.connect();

            responseCode = connection.getResponseCode();
            responseMessage = connection.getResponseMessage();
        } catch (MalformedURLException e) {
            throw new APYException("Malformed URL '" + requestUrl + "'.", e);
        } catch (IOException e) {
            throw new APYException("Unsuccessful request to " + requestUrl, e);
        }

        // Handle the response
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // Everything is fine
        } else {
            Log.i(APITARY_LOG_TAG, 
                    "Could not delete the entity. HTTP status: " + responseCode + " - " + responseMessage);
            throw new APYException(
                    "Could not delete the entity. HTTP status: " + responseCode + " - " + responseMessage);
        }
    }

    /**
     * Sets up an {@link HttpURLConnection} for the given request URL. To
     * finally execute the request, call "connect()" on the returned
     * {@link HttpURLConnection} instance.
     * 
     * @param requestUrl
     *            the URL to send the request to
     * @return an {@link HttpURLConnection} set up for a GET request to the
     *         given URL
     * @throws MalformedURLException
     *             when the given URL was malformed
     * @throws IOException
     *             when there was a problem setting up the connection object
     */
    private static HttpURLConnection setupGetConnection(String requestUrl) throws MalformedURLException, IOException {
        URL connectionUrl = new URL(requestUrl);
        HttpURLConnection connection = (HttpURLConnection) connectionUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoInput(true);
        connection.setConnectTimeout(CONNECTION_TIMEOUT_MS);
        return connection;
    }

    /**
     * Sets up an {@link HttpURLConnection} for a POST request to the given
     * request URL. To set the content body, write into the connection's
     * {@link OutputStream}.
     * 
     * @param requestUrl
     *            the URL to send the request to
     * @param contentLength
     *            the content length of the data in the request body
     * @return an {@link HttpURLConnection} set up for a GET request to the
     *         given URL
     * @throws MalformedURLException
     *             when the given URL was malformed
     * @throws IOException
     *             when there was a problem setting up the connection object
     */
    private static HttpURLConnection setupPostConnection(String requestUrl,
            int contentLength) throws MalformedURLException, IOException {
        URL connectionUrl = new URL(requestUrl);
        HttpURLConnection connection = (HttpURLConnection) connectionUrl
                .openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setUseCaches(false);
        connection.setFixedLengthStreamingMode(contentLength);
        connection.setConnectTimeout(CONNECTION_TIMEOUT_MS);
        return connection;
    }

    /**
     * Sets up an {@link HttpURLConnection} for a PUT request to the given
     * request URL. To set the content body, write into the connection's
     * {@link OutputStream}.
     * 
     * @param requestUrl
     *            the URL to send the request to
     * @param contentLength
     *            the content length of the data in the request body
     * @return an {@link HttpURLConnection} set up for a GET request to the
     *         given URL
     * @throws MalformedURLException
     *             when the given URL was malformed
     * @throws IOException
     *             when there was a problem setting up the connection object
     */
    private static HttpURLConnection setupPutConnection(String requestUrl,
            int contentLength) throws MalformedURLException, IOException {
        URL connectionUrl = new URL(requestUrl);
        HttpURLConnection connection = (HttpURLConnection) connectionUrl
                .openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setUseCaches(false);
        connection.setFixedLengthStreamingMode(contentLength);
        connection.setConnectTimeout(CONNECTION_TIMEOUT_MS);
        return connection;
    }

    /**
     * Sets up an {@link HttpURLConnection} for a PUT request to the given
     * request URL. To set the content body, write into the connection's
     * {@link OutputStream}.
     * 
     * @param requestUrl
     *            the URL to send the request to
     * @param contentLength
     *            the content length of the data in the request body
     * @return an {@link HttpURLConnection} set up for a GET request to the
     *         given URL
     * @throws MalformedURLException
     *             when the given URL was malformed
     * @throws IOException
     *             when there was a problem setting up the connection object
     */
    private static HttpURLConnection setupDeleteConnection(String requestUrl)
            throws MalformedURLException, IOException {
        URL connectionUrl = new URL(requestUrl);
        HttpURLConnection connection = (HttpURLConnection) connectionUrl.openConnection();
        connection.setRequestMethod("DELETE");
        connection.setRequestProperty("Accept", "application/json");
        connection.setUseCaches(false);
        connection.setConnectTimeout(CONNECTION_TIMEOUT_MS);
        return connection;
    }

    /**
     * Reads the content of the given response {@link InputStream} and converts
     * it into an {@link JSONObject}.
     * 
     * @param responseStream
     *            the response stream to read the JSON from
     * @return a {@link JSONObject} created of the content of the response stream
     * @throws IOException
     *             when there was a problem reading the response stream
     */
    private static JSONObject getJsonObjectFromResponseStream(InputStream responseStream) throws IOException {
        InputStream inputStream = new BufferedInputStream(responseStream);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String responseLine = null;
        StringBuilder responseStringBuilder = new StringBuilder();
        while ((responseLine = reader.readLine()) != null) { // TODO Need to
                                                             // close the stream
                                                             // in case of
                                                             // IOException?
            responseStringBuilder.append(responseLine);
        }

        String responseJson = responseStringBuilder.toString();
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(responseJson);
        } catch (JSONException e) {
            // Invalid JSON in response string
            // TODO What now?
        }
        return jsonObject;
    }

    /**
     * Returns the complete API URL including the API ID and version.
     * 
     * @return the complete API URL including the API ID and version
     */
    private String getApiUrl() {
        String apiUrl = apiBaseUrl;

        // Check if the url ends with a slash
        if (!apiUrl.endsWith("/")) {
            apiUrl = apiUrl.concat("/");
        }

        // Append the API ID
        apiUrl = apiUrl.concat(apiId);

        // Append the API version and a slash
        apiUrl = apiUrl.concat("/v").concat(apiVersion + "").concat("/");

        return apiUrl;
    }

}

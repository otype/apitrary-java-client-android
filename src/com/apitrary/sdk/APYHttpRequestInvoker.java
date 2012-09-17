package com.apitrary.sdk;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.apitrary.sdk.APYException.APYExceptionDetailCode;

/**
 * Class used to invoke the actual HTTP requests.
 */
class APYHttpRequestInvoker {

    /**
     * LOG Tag used for Android logging statements.
     */
    private static final String LOG_TAG = "APITRARY";

    /**
     * The API URL to execute all requests on. This URL includes protocol, host,
     * port, API key and API version.
     */
    private URL apiUrl;

    /**
     * The timeout for any request in milliseconds.
     */
    private int timeout;

    /**
     * Constructs an instance of {@link APYHttpRequestInvoker} used to invoke
     * HTTP requests on the given URL.
     * 
     * @param apiUrl the API URL to invoke HTTP requests on
     * @param timeout the request timeout in milliseconds. If < 1 no timeout is set
     */
    APYHttpRequestInvoker(URL apiUrl, int timeout) {
        // TODO Validate the URL and timeout
        this.apiUrl = apiUrl;
        this.timeout = timeout;
    }

    /**
     * Fetches all entities for the given entity (type) name from the apitrary backend.
     * 
     * @param entityName
     *            the name identifying the kind of entities to fetch
     * @return a list of all entities
     * @throws IllegalArgumentException
     *             if the given entity name was null or empty
     * @throws APYException
     *             <ul>
     *             <li>if there was an error on the backend side</li>
     *             <li>if anything else went wrong</li>
     *             </ul>
     */
    List<APYEntity> fetchAll(String entityName) throws IllegalArgumentException, APYException {
        // Validate the entity name
        if (APYUtils.isNullOrEmpty(entityName)) {
            throw new IllegalArgumentException("The given entity name was null or empty.");
        }

        try {
            // The full URL the request will be sent to
            URL requestUrl = new URL(apiUrl, entityName.toLowerCase(Locale.US));

            Log.d(LOG_TAG, "GET ".concat(requestUrl.toString()));

            HttpURLConnection connection = prepareGetConnection(requestUrl, timeout);
            connection.connect();

            // Handle the response
            int responseCode; responseCode = connection.getResponseCode();
            String responseMessage = connection.getResponseMessage();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Everything is fine

                Log.d(LOG_TAG, "Successfully fetched the entities. HTTP status: "
                        .concat(String.valueOf(responseCode)).concat(" - ").concat(responseMessage));

                // Extract the JSON response object form the response stream
                JSONObject responseJsonObject = getResponseJson(connection.getInputStream());

                // Extract the JSON 'result' array
                JSONArray jsonResultArray = getResultArrayFromResponseJson(responseJsonObject);

                // Iterate over all single result objects and convert those into APYEntity instances
                JSONObject singleJsonResultObject = null;
                List<APYEntity> resultEntities = new ArrayList<APYEntity>();
                APYEntity resultEntity = null;
                for (int index = 0; index < jsonResultArray.length(); index++) {
                    singleJsonResultObject = jsonResultArray.getJSONObject(index);

                    // Convert the _data object into an APYEntity
                    JSONObject jsonDataObject = getResultObjectData(singleJsonResultObject);
                    resultEntity = APYUtils.convertFromJson(entityName, jsonDataObject);
                    
                    // Get the value for the _id and set it into our APYEntity 
                    resultEntity.setId(getResultObjectId(singleJsonResultObject));

                    // Filter out the _init object
                    // TODO Remove this as soon as we stopped returning the _init object
                    if (resultEntity.get("_init") != null) {
                        continue;
                    }
                    resultEntities.add(resultEntity);
                }
                return resultEntities;
            } else if (responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {
                // 500 - Internal Server Error
                Log.i(LOG_TAG,
                        "Entities of type '".concat(entityName).concat("' could not be fetched. HTTP status: ")
                        .concat(String.valueOf(responseCode)).concat(" - ").concat(responseMessage));
                throw new APYException(APYExceptionDetailCode.BACKEND_ERROR,
                        "Entities of type '".concat(entityName).concat("' could not be fetched. HTTP status: ")
                        .concat(String.valueOf(responseCode)).concat(" - ").concat(responseMessage));
            } else {
                Log.i(LOG_TAG,
                        "Entities of type '".concat(entityName).concat("' could not be fetched. HTTP status: ")
                        .concat(String.valueOf(responseCode)).concat(" - ").concat(responseMessage));
                throw new APYException(
                        "Entities of type '".concat(entityName).concat("' could not be fetched. HTTP status: ")
                        .concat(String.valueOf(responseCode)).concat(" - ").concat(responseMessage));
            }
        } catch (Exception e) {
            throw new APYException("Entities of type '".concat(entityName).concat("' could not be fetched."), e);
        }
    }

    /**
     * Fetches the entity for the given entity (type) name matching the given entity ID from the apitrary
     * backend.
     * 
     * @param entityName
     *            the name identifying the kind of entity to fetch
     * @param entityId
     *            the ID of the entity to fetch
     * @return the fetched {@link APYEntity} or null if none could be found
     * @throws IllegalArgumentException
     *             if the given entity name or ID was null or empty
     * @throws APYException
     *             <ul>
     *             <li>if the entity to fetch could not be found on the apitrary backend</li>
     *             <li>if there was an error on the backend side</li>
     *             <li>if anything else went wrong</li>
     *             </ul>
     */
    APYEntity fetchOne(String entityName, String entityId) throws IllegalArgumentException, APYException {
        // Validate the entity name
        if (APYUtils.isNullOrEmpty(entityName)) {
            throw new IllegalArgumentException("The given entity name was null or empty.");
        }

        if (APYUtils.isNullOrEmpty(entityId)) {
            throw new IllegalArgumentException("The given entity ID was null or empty.");
        }

        try {
            // The full URL the request will be sent to
            URL requestUrl = new URL(apiUrl, entityName.toLowerCase(Locale.US).concat("/").concat(entityId));

            Log.d(LOG_TAG, "GET ".concat(requestUrl.toString()));

            HttpURLConnection connection = prepareGetConnection(requestUrl, timeout);
            connection.connect();

            // Handle the response
            int responseCode = connection.getResponseCode();
            String responseMessage = connection.getResponseMessage();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Everything is fine

                Log.d(LOG_TAG, "Successfully fetched entity of type '".concat(entityName).concat("' (id: ")
                        .concat(entityId).concat("). HTTP status: ")
                        .concat(String.valueOf(responseCode)).concat(" - ").concat(responseMessage));

                // Extract the JSON response object form the response stream
                JSONObject responseJsonObject = getResponseJson(connection.getInputStream());

                // Extract the JSON 'result' object
                JSONObject jsonResultObject = getResultObjectFromResponseJson(responseJsonObject);

                // Convert the _data object into an APYEntity
                JSONObject jsonDataObject = getResultObjectData(jsonResultObject);
                APYEntity fetchedEntity = APYUtils.convertFromJson(entityName, jsonDataObject);

                // Get the value for the _id and set it into our APYEntity
                fetchedEntity.setId(getResultObjectId(jsonResultObject));

                return fetchedEntity;
            } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                // 404 - Not found
                Log.d(LOG_TAG, "Entity to fetch (name: ".concat(entityName)
                        .concat(", id: ").concat(entityId).concat(") could not be found. HTTP status: ")
                        .concat(String.valueOf(responseCode)).concat(" - ").concat(responseMessage));
                throw new APYException(APYExceptionDetailCode.ENTITY_NOT_FOUND,
                        "Entity to fetch (name: ".concat(entityName).concat(", id: ").concat(entityId)
                        .concat(") could not be found. HTTP status: ")
                        .concat(String.valueOf(responseCode)).concat(" - ").concat(responseMessage));
            } else if (responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {
                // 500 - Internal Server Error
                Log.d(LOG_TAG,
                        "Entity (name: ".concat(entityName).concat(", id: ").concat(entityId)
                        .concat(") could not be fetched. HTTP status: ")
                        .concat(String.valueOf(responseCode)).concat(" - ").concat(responseMessage));
                throw new APYException(APYExceptionDetailCode.BACKEND_ERROR,
                        "Entity (name: ".concat(entityName).concat(", id: ").concat(entityId)
                        .concat(") could not be fetched. HTTP status: ")
                        .concat(String.valueOf(responseCode)).concat(" - ").concat(responseMessage));
            } else {
                Log.i(LOG_TAG,
                        "Entity of type '".concat(entityName).concat("' could not be fetched. HTTP status: ")
                        .concat(String.valueOf(responseCode)).concat(" - ").concat(responseMessage));
                throw new APYException(
                        "Entity of type '".concat(entityName).concat("' could not be fetched. HTTP status: ")
                        .concat(String.valueOf(responseCode)).concat(" - ").concat(responseMessage));
            }
        } catch (Exception e) {
            throw new APYException("Entity of type '".concat(entityName).concat("' could not be fetched."), e);
        }
    }

    /**
     * Creates the given entity on the apitrary backend.
     * 
     * @param entity
     *            the entity to create
     * @return the created {@link APYEntity}
     * @throws IllegalArgumentException
     *             if the entity was null or if its name was null or empty
     * @throws APYException
     *             <ul>
     *             <li>if there was an error on the backend side</li>
     *             <li>if anything else went wrong</li>
     *             </ul>
     */
    APYEntity create(APYEntity entity) throws IllegalArgumentException, APYException {
        // Validate the entity
        if (entity == null) {
            throw new IllegalArgumentException("The given entity was null.");
        }

        if (APYUtils.isNullOrEmpty(entity.getName())) {
            throw new IllegalArgumentException("The name of the given entity was null or empty.");
        }

        String entityName = entity.getName();

        try {
            // The full URL the request will be sent to
            URL requestUrl = new URL(apiUrl, entity.getName().toLowerCase(Locale.US));

            Log.d(LOG_TAG, "POST ".concat(requestUrl.toString()));
    
            // Convert the entity into a JSONObject
            JSONObject jsonObject = APYUtils.convertToJson(entity);
    
            // The JSON bytes to be written
            byte[] jsonBytes = jsonObject.toString().getBytes();
    
            HttpURLConnection connection = preparePostConnection(requestUrl, timeout, jsonBytes.length);
    
            // Write the JSON bytes into the request body
            OutputStream outputStream = new BufferedOutputStream(connection.getOutputStream());
            outputStream.write(jsonBytes);
            outputStream.flush();
            outputStream.close();
    
            // Handle the response
            int responseCode = connection.getResponseCode();
            String responseMessage = connection.getResponseMessage();
            if (responseCode == HttpURLConnection.HTTP_CREATED) {
                // Everything is fine

                Log.d(LOG_TAG, "Successfully created entity '".concat(entityName).concat("'. HTTP status: ")
                        .concat(String.valueOf(responseCode)).concat(" - ").concat(responseMessage));

                // Extract the JSON response object form the response stream
                JSONObject responseJsonObject = getResponseJson(connection.getInputStream());

                // Extract the JSON 'result' object
                JSONObject jsonResultObject = getResultObjectFromResponseJson(responseJsonObject);

                // Get the returned ID of the updated entity and set it on the entity
                entity.setId(getResultObjectId(jsonResultObject));

                return entity;
            } else if (responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {
                // 500 - Internal Server Error
                Log.i(LOG_TAG,
                        "Entity of type '".concat(entityName).concat("' could not be created. HTTP status: ")
                        .concat(String.valueOf(responseCode)).concat(" - ").concat(responseMessage));
                throw new APYException(APYExceptionDetailCode.BACKEND_ERROR,
                        "Entity of type '".concat(entityName).concat("' could not be created. HTTP status: ")
                        .concat(String.valueOf(responseCode)).concat(" - ").concat(responseMessage));
            } else {
                Log.i(LOG_TAG,
                        "Entity of type '".concat(entityName).concat("' could not be created. HTTP status: ")
                        .concat(String.valueOf(responseCode)).concat(" - ").concat(responseMessage));
                throw new APYException(
                        "Entity of type '".concat(entityName).concat("' could not be created. HTTP status: ")
                        .concat(String.valueOf(responseCode)).concat(" - ").concat(responseMessage));
            }
        } catch (Exception e) {
            throw new APYException("Entity of type '".concat(entityName).concat("' could not be created."), e);
        }
    }

    /**
     * Updates the given entity on the apitrary backend.
     * 
     * @param entity
     *            the entity to update
     * @return the updated {@link APYEntity}
     * @throws IllegalArgumentException
     *             if the entity was null or if its name or ID was null or empty
     * @throws APYException
     *             <ul>
     *             <li>if the entity to update could not be found on the apitrary backend</li>
     *             <li>if there was an error on the backend side</li>
     *             <li>if anything else went wrong</li>
     *             </ul>
     */
    APYEntity update(APYEntity entity) throws IllegalArgumentException, APYException {
        // Validate the entity
        if (entity == null) {
            throw new IllegalArgumentException("The given entity was null.");
        }

        if (APYUtils.isNullOrEmpty(entity.getName())) {
            throw new IllegalArgumentException("The name of the given entity was null or empty.");
        }

        if (APYUtils.isNullOrEmpty(entity.getId())) {
            throw new IllegalArgumentException("The ID of the given entity was null or empty.");
        }

        String entityName = entity.getName();
        String entityId = entity.getId();

        try {
            // The full URL the request will be sent to
            URL requestUrl = new URL(apiUrl, entityName.toLowerCase(Locale.US).concat("/").concat(entityId));

            Log.d(LOG_TAG, "PUT ".concat(requestUrl.toString()));

            // Convert the entity into a JSONObject
            JSONObject jsonObject = APYUtils.convertToJson(entity);
    
            // The JSON bytes to be written
            byte[] jsonBytes = jsonObject.toString().getBytes();
    
            HttpURLConnection connection = preparePutConnection(requestUrl, timeout, jsonBytes.length);

            // Write the JSON bytes into the request body
            OutputStream outputStream = new BufferedOutputStream(connection.getOutputStream());
            outputStream.write(jsonBytes);
            outputStream.flush();
            outputStream.close();

            // Handle the response
            int responseCode = connection.getResponseCode();
            String responseMessage = connection.getResponseMessage();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Everything is fine

                Log.d(LOG_TAG, "Successfully updated entity of type '".concat(entityName).concat("' (id: ")
                        .concat(entityId).concat("). HTTP status: ")
                        .concat(String.valueOf(responseCode)).concat(" - ").concat(responseMessage));

                // Extract the JSON response object form the response stream
                JSONObject responseJsonObject = getResponseJson(connection.getInputStream());

                // Extract the JSON 'result' object
                JSONObject jsonResultObject = getResultObjectFromResponseJson(responseJsonObject);

                // Get the returned ID of the updated entity and re-set it on the entity 
                entity.setId(getResultObjectId(jsonResultObject));

                return entity;
            } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                // 404 - Not found
                Log.d(LOG_TAG,
                        "Entity to update (name: ".concat(entityName)
                        .concat(", id: ").concat(entityId).concat(") could not be found. HTTP status: ")
                        .concat(String.valueOf(responseCode)).concat(" - ").concat(responseMessage));
                throw new APYException(APYExceptionDetailCode.ENTITY_NOT_FOUND,
                        "Entity to update (name: ".concat(entityName).concat(", id: ").concat(entityId)
                        .concat(") could not be found. HTTP status: ")
                        .concat(String.valueOf(responseCode)).concat(" - ").concat(responseMessage));
            } else if (responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {
                // 500 - Internal Server Error
                Log.d(LOG_TAG,
                        "Entity (name: ".concat(entityName).concat(", id: ").concat(entityId)
                        .concat(") could not be updated. HTTP status: ")
                        .concat(String.valueOf(responseCode)).concat(" - ").concat(responseMessage));
                throw new APYException(APYExceptionDetailCode.BACKEND_ERROR,
                        "Entity (name: ".concat(entityName).concat(", id: ").concat(entityId)
                        .concat(") could not be updated. HTTP status: ")
                        .concat(String.valueOf(responseCode)).concat(" - ").concat(responseMessage));
            } else {
                Log.i(LOG_TAG,
                        "Entity of type '".concat(entityName).concat("' (id: ").concat(entityId)
                        .concat(") could not be updated. HTTP status: ")
                        .concat(String.valueOf(responseCode)).concat(" - ").concat(responseMessage));
                throw new APYException(
                        "Entity of type '".concat(entityName).concat("' (id: ").concat(entityId)
                        .concat(") could not be updated. HTTP status: ")
                        .concat(String.valueOf(responseCode)).concat(" - ").concat(responseMessage));
            }
        } catch (Exception e) {
            throw new APYException("Entity of type '".concat(entityName)
                    .concat("' (id: ").concat(entityId).concat(") could not be updated."), e);
        }
    }

    /**
     * Deletes the given entity on the apitrary backend.
     * 
     * @param entity
     *            the entity to delete
     * @return the ID of the deleted entity
     * @throws IllegalArgumentException
     *             if the entity was null or if its name or ID was null or empty
     * @throws APYException
     *             <ul>
     *             <li>if the entity to delete could not be found on the apitrary backend</li>
     *             <li>if there was an error on the backend side</li>
     *             <li>if anything else went wrong</li>
     *             </ul>
     */
    String delete(APYEntity entity) throws IllegalArgumentException, APYException {
        // Validate the entity
        if (entity == null) {
            throw new IllegalArgumentException("The given entity was null.");
        }

        if (APYUtils.isNullOrEmpty(entity.getName())) {
            throw new IllegalArgumentException("The name of the given entity was null or empty.");
        }

        if (APYUtils.isNullOrEmpty(entity.getId())) {
            throw new IllegalArgumentException("The ID of the given entity was null or empty.");
        }

        String entityName = entity.getName();
        String entityId = entity.getId();

        try {
            // The full URL the request will be sent to
            URL requestUrl = new URL(apiUrl, entityName.toLowerCase(Locale.US).concat("/").concat(entityId));

            Log.d(LOG_TAG, "DELETE ".concat(requestUrl.toString()));

            HttpURLConnection connection = prepareDeleteConnection(requestUrl, timeout);
            connection.connect();

            // Handle the response
            int responseCode = connection.getResponseCode();
            String responseMessage = connection.getResponseMessage();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Everything is fine

                Log.d(LOG_TAG, "Successfully deleted entity of type '".concat(entityName).concat("' (id: ")
                        .concat(entityId).concat("). HTTP status: ")
                        .concat(String.valueOf(responseCode)).concat(" - ").concat(responseMessage));

                // Extract the JSON response object form the response stream
                JSONObject responseJsonObject = getResponseJson(connection.getInputStream());

                // Extract the JSON 'result' object
                JSONObject jsonResultObject = getResultObjectFromResponseJson(responseJsonObject);

                // Get the returned ID of the deleted entity and return it to the caller
                return getResultObjectId(jsonResultObject);
            } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                // 404 - Not found
                Log.d(LOG_TAG,
                        "Entity to delete (name: ".concat(entityName).concat(", id: ").concat(entityId)
                        .concat(") could not be found. HTTP status: ")
                        .concat(String.valueOf(responseCode)).concat(" - ").concat(responseMessage));
                throw new APYException(APYExceptionDetailCode.ENTITY_NOT_FOUND,
                        "Entity to delete (name: ".concat(entityName).concat(", id: ").concat(entityId)
                        .concat(") could not be found. HTTP status: ")
                        .concat(String.valueOf(responseCode)).concat(" - ").concat(responseMessage));
            } else if (responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {
                // 500 - Internal Server Error
                Log.d(LOG_TAG,
                        "Entity (name: ".concat(entityName).concat(", id: ").concat(entityId)
                        .concat(") could not be deleted. HTTP status: ")
                        .concat(String.valueOf(responseCode)).concat(" - ").concat(responseMessage));
                throw new APYException(APYExceptionDetailCode.BACKEND_ERROR,
                        "Entity (name: ".concat(entityName).concat(", id: ").concat(entityId)
                        .concat(") could not be deleted. HTTP status: ")
                        .concat(String.valueOf(responseCode)).concat(" - ").concat(responseMessage));
            } else {
                Log.i(LOG_TAG,
                        "Entity of type '".concat(entityName).concat("' could not be deleted. HTTP status: ")
                        .concat(String.valueOf(responseCode)).concat(" - ").concat(responseMessage));
                throw new APYException(
                        "Entity of type '".concat(entityName).concat("' could not be deleted. HTTP status: ")
                        .concat(String.valueOf(responseCode)).concat(" - ").concat(responseMessage));
            }
        } catch (Exception e) {
            throw new APYException("Entity of type '".concat(entityName)
                    .concat("' (id: ").concat(entityId).concat(") could not be deleted."), e);
        }
    }

    /**
     * Prepares an {@link HttpURLConnection} for a GET request on the given URL.
     * To execute the request, call "connect()" on the returned
     * {@link HttpURLConnection} instance.
     * 
     * @param requestUrl
     *            the URL to send the request to
     * @param timeout
     *            the maximal timeout in milliseconds
     * @throws MalformedURLException
     *             when the given URL was malformed
     * @throws SocketTimeoutException
     *             if the request timed out
     * @throws IOException
     *             when there was a problem setting up the connection object
     */
    private static HttpURLConnection prepareGetConnection(URL requestUrl, int timeout) 
            throws MalformedURLException, SocketTimeoutException, IOException {
        HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoInput(true);
        connection.setUseCaches(true);
        connection.setConnectTimeout(timeout);
        connection.setReadTimeout(timeout);
        return connection;
    }

    /**
     * Prepares an {@link HttpURLConnection} for a POST request on the given
     * URL. To set the content body, write into the connection's
     * {@link OutputStream}.
     * 
     * @param requestUrl
     *            the URL to send the request to
     * @param timeout
     *            the maximal timeout in milliseconds
     * @param contentLength
     *            the content length of the data that will be written into the
     *            request body
     * @return an {@link HttpURLConnection} set up for a POST request to the
     *         given URL
     * @throws MalformedURLException
     *             when the given URL was malformed
     * @throws SocketTimeoutException
     *             if the request timed out
     * @throws IOException
     *             when there was a problem setting up the connection object
     */
    private static HttpURLConnection preparePostConnection(URL requestUrl, int timeout,
            int contentLength) throws MalformedURLException, IOException {
        HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setUseCaches(false);
        connection.setFixedLengthStreamingMode(contentLength);
        connection.setConnectTimeout(timeout);
        connection.setReadTimeout(timeout);
        return connection;
    }

    /**
     * Prepares an {@link HttpURLConnection} for a PUT request on the given
     * URL. To set the content body, write into the connection's
     * {@link OutputStream}.
     * 
     * @param requestUrl
     *            the URL to send the request to
     * @param timeout
     *            the maximal timeout in milliseconds
     * @param contentLength
     *            the content length of the data that will be written into the
     *            request body
     * @return an {@link HttpURLConnection} set up for a PUT request to the
     *         given URL
     * @throws MalformedURLException
     *             when the given URL was malformed
     * @throws SocketTimeoutException
     *             if the request timed out
     * @throws IOException
     *             when there was a problem setting up the connection object
     */
    private static HttpURLConnection preparePutConnection(URL requestUrl, int timeout,
            int contentLength) throws MalformedURLException, IOException {
        HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setUseCaches(false);
        connection.setFixedLengthStreamingMode(contentLength);
        connection.setConnectTimeout(timeout);
        connection.setReadTimeout(timeout);
        return connection;
    }

    /**
     * Prepares an {@link HttpURLConnection} for a DELETE request on the given
     * URL. To execute the request, call "connect()" on the returned
     * {@link HttpURLConnection} instance.
     * 
     * @param requestUrl
     *            the URL to send the request to
     * @param timeout
     *            the maximal timeout in milliseconds
     * @return an {@link HttpURLConnection} set up for a DELETE request to the
     *         given URL
     * @throws MalformedURLException
     *             when the given URL was malformed
     * @throws SocketTimeoutException
     *             if the request timed out
     * @throws IOException
     *             when there was a problem setting up the connection object
     */
    private static HttpURLConnection prepareDeleteConnection(URL requestUrl, int timeout)
            throws MalformedURLException, IOException {
        HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
        connection.setRequestMethod("DELETE");
        connection.setRequestProperty("Accept", "application/json");
        connection.setUseCaches(false);
        connection.setConnectTimeout(timeout);
        connection.setReadTimeout(timeout);
        return connection;
    }
    
    /**
     * Reads the JSON out of the given response {@link InputStream} and converts
     * it into a {@link JSONObject}.
     * 
     * @param responseStream
     *            the {@link InputStream} to read the JSON response data from
     * @return a {@link JSONObject} created of the response stream's contents
     * @throws IOException
     *             if there was a problem reading the response stream
     */
    private static JSONObject getResponseJson(InputStream responseStream) throws JSONException, IOException {
        InputStream inputStream = new BufferedInputStream(responseStream);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String responseLine = null;
        StringBuilder responseStringBuilder = new StringBuilder();
        try {
        while ((responseLine = reader.readLine()) != null) {
            responseStringBuilder.append(responseLine);
        }
        } finally {
            inputStream.close();
        }

        String responseJson = responseStringBuilder.toString();
        return new JSONObject(responseJson);
    }

    /**
     * Returns the value of the 'statusCode' property contained in the response
     * JSONObject.
     * 
     * @param responseJson
     *            the response {@link JSONObject} retrieved from an
     *            {@link InputStream}
     * @return the int value of the 'statusCode' property
     * @throws JSONException
     *             if there was no property 'statusCode' in the JSON or if the
     *             value was not an int
     */
    @SuppressWarnings("unused")
    private static int getStatusCodeFromResponseJson(JSONObject responseJson) throws JSONException {
        return responseJson.getInt(APYConstants.KEY_RESPONSE_STATUS_CODE);
    }

    /**
     * Returns the value of the 'statusMessage' property contained in the response
     * JSONObject.
     * 
     * @param responseJson
     *            the response {@link JSONObject} retrieved from an
     *            {@link InputStream}
     * @return the String value of the 'statusMessage' property
     * @throws JSONException
     *             if there was no property 'statusMessage' in the JSON or if the
     *             value was not a String
     */
    @SuppressWarnings("unused")
    private static String getStatusMessageFromResponseJson(JSONObject responseJson) throws JSONException {
        return responseJson.getString(APYConstants.KEY_RESPONSE_STATUS_MESSAGE);
    }

    /**
     * Returns the {@link JSONObject} value of the 'result' property contained in the response
     * JSONObject.
     * 
     * @param responseJson
     *            the response {@link JSONObject} retrieved from an
     *            {@link InputStream}
     * @return the {@link JSONObject} value of the 'result' property
     * @throws JSONException
     *             if there was no property 'result' in the JSON or if the
     *             value was not a {@link JSONObject}
     */
    private static JSONObject getResultObjectFromResponseJson(JSONObject responseJson) throws JSONException {
        return responseJson.getJSONObject(APYConstants.KEY_RESPONSE_RESULT);
    }

    /**
     * Returns the {@link JSONArray} value of the 'result' property contained in the response
     * JSONObject.
     * 
     * @param responseJson
     *            the response {@link JSONObject} retrieved from an
     *            {@link InputStream}
     * @return the {@link JSONArray} value of the 'result' property
     * @throws JSONException
     *             if there was no property 'result' in the JSON or if the
     *             value was not a {@link JSONArray}
     */
    private static JSONArray getResultArrayFromResponseJson(JSONObject responseJson) throws JSONException {
        return responseJson.getJSONArray(APYConstants.KEY_RESPONSE_RESULT);
    }

    /**
     * Returns the ID of the given result object.
     * 
     * @param resultObject
     *            a single result {@link JSONObject} read from a JSON response stream
     * @return the String ID of the given result object
     * @throws JSONException
     *             if there was no property '_id' in the JSON or if the
     *             value was not a String
     */
    private static String getResultObjectId(JSONObject resultObject) throws JSONException {
        return resultObject.getString(APYConstants.KEY_RESULT_OBJECT_ID);
    }

    /**
     * Returns the data of the given result object.
     * 
     * @param resultObject
     *            a single result {@link JSONObject} read from a JSON response stream
     * @return the data {@link JSONObject} of the given result object
     * @throws JSONException
     *             if there was no property '_data' in the JSON or if the
     *             value was not a {@link JSONObject}
     */
    private static JSONObject getResultObjectData(JSONObject resultObject) throws JSONException {
        return resultObject.getJSONObject(APYConstants.KEY_RESPONSE_OBJECT_DATA);
    }

}

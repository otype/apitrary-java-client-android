package com.apitrary.sdk;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

abstract class APYUtils {

    private APYUtils() {
    }

    /**
     * Returns the full API URL constructed by the given values.
     * 
     * @param apiBaseUrl
     *            the base API URL consisting of protocol, host and port (e.g.
     *            "http://HOST:PORT")
     * @param apiId
     *            the API ID identifying the API work with
     * @param apiVersion
     *            the API's version to work with
     * @return the full API URL ending with a slash (e.g.
     *         "http://HOST:PORT/API_ID/API_VERSION/")
     * @throws MalformedURLException
     *             if no valid URL could be constructed using the given values
     */
    static URL getFullApiUrl(String apiBaseUrl, String apiId, int apiVersion) throws MalformedURLException {
        StringBuilder urlStringBuilder = new StringBuilder(apiBaseUrl);

        // Check if the apiBaseUrl ends with a slash. If not, append one
        if (!apiBaseUrl.endsWith("/")) {
            urlStringBuilder = urlStringBuilder.append("/");
        }

        // Append the API ID
        urlStringBuilder = urlStringBuilder.append(apiId);

        // Append the API version and a slash
        urlStringBuilder = urlStringBuilder.append("/v").append(apiVersion).append("/");

        return new URL(urlStringBuilder.toString());
    }

    /**
     * Validates the given entity.
     * 
     * @param entity
     *            the entity to validate
     * @throws IllegalArgumentException
     *             when the given entity is null,
     *             when the entity's name is null or empty
     */
    static void validateEntity(APYEntity entity) throws IllegalArgumentException {
        if (entity == null) {
            throw new IllegalArgumentException("The entity to validate must not be null.");
        }

        if (isNullOrEmpty(entity.getName())) {
            throw new IllegalArgumentException("The name of the entity is null or empty.");
        }
    }

    /**
     * Checks if the given parameter string is null or empty.
     * 
     * @param checkedParameter
     *            the string parameter to check on null or empty
     * @return true if the parameter is null or empty, false otherwise
     */
    static boolean isNullOrEmpty(String checkedParameter) {
        return checkedParameter == null || checkedParameter.trim().length() == 0;
    }

    /**
     * Converts the given {@link APYEntity} into a {@link JSONObject}.
     * 
     * @param entity
     *            the entity to convert.
     * @return the {@link JSONObject} the entity was converted to
     * @throws IllegalArgumentException
     *             when the given entity is null
     * @throws APYException
     *             when there was a problem during conversion
     * @throws JSONException if there was a problem parsing the JSON
     */
    static JSONObject convertToJson(APYEntity entity) throws IllegalArgumentException, APYException, JSONException {
        if (entity == null) {
            throw new IllegalArgumentException("The entity to convert must not be null.");
        }

        JSONObject jsonObject = new JSONObject();
        for (String propertyKey : entity.getProperties().keySet()) {
            jsonObject.put(propertyKey, entity.get(propertyKey));
        }
        return jsonObject;
    }

    /**
     * Converts the given {@link JSONObject} into a {@link APYEntity}.
     * 
     * @param entityName
     *            the name of the entity
     * @param jsonDataObject
     *            the JSONObject to convert
     * @return the {@link APYEntity} the {@link JSONObject} was converted to
     * @throws IllegalArgumentException
     *             if the given entity name is null or empty
     *             if the given JSONObject was null
     * @throws JSONException if there was a problem parsing the JSON
     */
    static APYEntity convertFromJson(String entityName, JSONObject jsonDataObject) throws IllegalArgumentException, JSONException {
        if (isNullOrEmpty(entityName)) {
            throw new IllegalArgumentException("Parameter 'entityName' is null or empty.");
        }
        if (jsonDataObject == null) {
            throw new IllegalArgumentException("Parameter 'jsonDataObject' is null.");
        }

        APYEntity apyEntity = new APYEntity(entityName);

        // Iterate over all keys to get the object's properties and add those to our APYEntity
        @SuppressWarnings("unchecked")
        Iterator<String> dataPropertyKeys = jsonDataObject.keys();
        String dataPropertyKey = null;
        while (dataPropertyKeys.hasNext()) {
            dataPropertyKey = dataPropertyKeys.next();
            if (dataPropertyKey.equalsIgnoreCase("_createdAt")) {
                // Extract the creation and convert the Python format ("seconds.milliseconds")
                // to Java long format (milliseconds)
                double _createdAt = jsonDataObject.getDouble("_createdAt");
                long createdAt = (long) (_createdAt * 1000);
                apyEntity.setCreatedAt(createdAt);
            } else if (dataPropertyKey.equalsIgnoreCase("_updatedAt")) {
                // Extract the update date and convert the Python format ("seconds.milliseconds")
                // to Java long format (milliseconds)
                double _updatedAt = jsonDataObject.getDouble("_updatedAt");
                long updatedAt = (long) (_updatedAt * 1000);
                apyEntity.setUpdatedAt(updatedAt);
            } else {
                apyEntity.put(dataPropertyKey, jsonDataObject.optString(dataPropertyKey, null));
            }
        }
        
        return apyEntity;
    }

}

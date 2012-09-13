package com.apitrary.sdk;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Main class of the apitrary client library.
 */
public class APYClient {

    /**
     * The apitrary log tag used for log output in Android log output.
     */
    @SuppressWarnings("unused")
    private static final String APITARY_LOG_TAG = "Apitrary";
    
    /**
     * The timeout in milliseconds for any request.
     */
    private int requestTimeout = 15000;

    /**
     * The full URL of the apitrary API to work with.
     */
    private URL fullApiUrl;

    /**
     * Constructs an instance of {@link APYClient} used to interact with an
     * apitrary API.
     * 
     * @param apiBaseUrl
     *            the base URL of the target API
     * @param apiId
     *            the API ID used to identify the API to use
     * @param apiVersion
     *            the version of your API as an integer >= 1
     * @throws IllegalArgumentException
     *             if the given apiBaseUrl was null or empty, if the given apiId
     *             was null or empty, if the given API version was less or equal
     *             to 0
     * @throws MalformedURLException
     *             if no valid URL could be constructed using the given values
     */
    public APYClient(String apiBaseUrl, String apiId, int apiVersion) throws IllegalArgumentException, MalformedURLException {
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

        fullApiUrl = APYUtils.getFullApiUrl(apiBaseUrl, apiId, apiVersion);
    }

    /**
     * Fetches all entities for the given entity (type) name from the apitrary
     * backend.
     * 
     * @param entityName
     *            the name identifying the kind of entities to fetch
     * @return a list of all entities
     * @throws IllegalArgumentException
     *             if the given entity name was null or empty
     * @throws APYException
     *             if anything went wrong while trying to fetch the entities
     */
    public List<APYEntity> fetchAll(String entityName) throws IllegalArgumentException, APYException {
        if (APYUtils.isNullOrEmpty(entityName)) {
            throw new IllegalArgumentException("The given entity name was null or empty.");
        }

        APYHttpRequestInvoker requestInvoker = new APYHttpRequestInvoker(fullApiUrl, requestTimeout);
        return requestInvoker.fetchAll(entityName);
    }

    /**
     * Asynchronously fetches all entities for the given entity (type) name from the apitrary
     * backend.
     * 
     * @param entityName
     *            the name identifying the kind of entities to fetch
     * @param callback
     *            the {@link APYFetchAllCallback} used to inform the caller about
     *            the operations outcome.
     * @throws IllegalArgumentException
     *             if the given entity name was null or empty, or the callback was null
     */
    public void fetchAllAsync(String entityName, APYFetchAllCallback callback) throws IllegalArgumentException {
        if (APYUtils.isNullOrEmpty(entityName)) {
            throw new IllegalArgumentException("The given entity name was null or empty.");
        }

        if (callback == null) {
            throw new IllegalArgumentException("The given callback was null.");
        }

        APYHttpRequestInvoker requestInvoker = new APYHttpRequestInvoker(fullApiUrl, requestTimeout);
        new APYFetchAllTask(requestInvoker, callback).execute(entityName);
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
     *             if anything went wrong while trying to fetch the entity
     */
    public APYEntity fetchOne(String entityName, String entityId) throws IllegalArgumentException, APYException {
        if (APYUtils.isNullOrEmpty(entityName)) {
            throw new IllegalArgumentException("The given entity name was null or empty.");
        }

        if (APYUtils.isNullOrEmpty(entityId)) {
            throw new IllegalArgumentException("The given entity id was null or empty.");
        }

        APYHttpRequestInvoker requestInvoker = new APYHttpRequestInvoker(fullApiUrl, requestTimeout);
        return requestInvoker.fetchOne(entityName, entityId);
    }

    /**
     * Asynchronously fetches the entity for the given entity (type) name
     * matching the given entity ID from the apitrary backend.
     * 
     * @param entityName
     *            the name identifying the kind of entities to fetch
     * @param entityId
     *            the ID of the entity to fetch
     * @param callback
     *            the {@link APYFetchOneCallback} used to inform the caller
     *            about the operations outcome.
     * @throws IllegalArgumentException
     *             if the given entity name or ID was null or empty, or the callback
     *             was null
     */
    public void fetchOneAsync(String entityName, String entityId, APYFetchOneCallback callback)
            throws IllegalArgumentException {
        if (APYUtils.isNullOrEmpty(entityName)) {
            throw new IllegalArgumentException("The given entity name was null or empty.");
        }

        if (APYUtils.isNullOrEmpty(entityId)) {
            throw new IllegalArgumentException("The given entity id was null or empty.");
        }

        if (callback == null) {
            throw new IllegalArgumentException("The given callback was null.");
        }

        APYHttpRequestInvoker requestInvoker = new APYHttpRequestInvoker(fullApiUrl, requestTimeout);
        new APYFetchOneTask(requestInvoker, callback).execute(entityName, entityId);
    }

    /**
     * Creates the given {@link APYEntity} on the apitrary backend.
     * 
     * @param entity
     *            the {@link APYEntity} to create.
     * @throws IllegalArgumentException if the entity was null or if its name was null or empty
     * @throws APYException if anything went wrong while trying to create the entity
     */
    public APYEntity create(APYEntity entity) throws IllegalArgumentException, APYException {
        if (entity == null) {
            throw new IllegalArgumentException("The given entity was null.");
        }

        if (APYUtils.isNullOrEmpty(entity.getName())) {
            throw new IllegalArgumentException("The name of the given entity was null or empty.");
        }

        APYHttpRequestInvoker requestInvoker = new APYHttpRequestInvoker(fullApiUrl, requestTimeout);
        return requestInvoker.create(entity);
    }

    /**
     * Asynchronously creates the given {@link APYEntity} on the apitrary backend.
     * 
     * @param entity
     *            the {@link APYEntity} to create.
     * @param callback
     *            the {@link APYCreateCallback} used to inform the caller about
     *            the operations outcome. May be null, if the caller is not interested in the result.
     * @throws IllegalArgumentException if the entity was null or if its name was null or empty
     */
    public void createAsync(APYEntity entity, APYCreateCallback callback) throws IllegalArgumentException {
        if (entity == null) {
            throw new IllegalArgumentException("The given entity was null.");
        }

        if (APYUtils.isNullOrEmpty(entity.getName())) {
            throw new IllegalArgumentException("The name of the given entity was null or empty.");
        }

        APYHttpRequestInvoker requestInvoker = new APYHttpRequestInvoker(fullApiUrl, requestTimeout);
        new APYCreateTask(requestInvoker, callback).execute(entity);
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
     *             if anything went wrong while trying to create the entity
     */
    public APYEntity update(APYEntity entity) throws IllegalArgumentException, APYException {
        if (entity == null) {
            throw new IllegalArgumentException("The given entity was null.");
        }

        if (APYUtils.isNullOrEmpty(entity.getName())) {
            throw new IllegalArgumentException("The name of the given entity was null or empty.");
        }

        if (APYUtils.isNullOrEmpty(entity.getId())) {
            throw new IllegalArgumentException("The ID of the given entity was null or empty.");
        }

        APYHttpRequestInvoker requestInvoker = new APYHttpRequestInvoker(fullApiUrl, requestTimeout);
        return requestInvoker.update(entity);
    }
    
    /**
     * Asynchronously updates the given entity on the apitrary backend.
     * 
     * @param entity
     *            the {@link APYEntity} to update.
     * @param callback
     *            the {@link APYUpdateCallback} used to inform the caller about
     *            the operations outcome. May be null, if the caller is not interested in the result.
     * @throws IllegalArgumentException if the entity was null or if its name or ID was null or empty
     */
    public void updateAsync(APYEntity entity, APYUpdateCallback callback) throws IllegalArgumentException {
        if (entity == null) {
            throw new IllegalArgumentException("The given entity was null.");
        }

        if (APYUtils.isNullOrEmpty(entity.getName())) {
            throw new IllegalArgumentException("The name of the given entity was null or empty.");
        }

        if (APYUtils.isNullOrEmpty(entity.getId())) {
            throw new IllegalArgumentException("The ID of the given entity was null or empty.");
        }

        APYHttpRequestInvoker requestInvoker = new APYHttpRequestInvoker(fullApiUrl, requestTimeout);
        new APYUpdateTask(requestInvoker, callback).execute(entity);
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
     *             if anything went wrong while trying to create the entity
     */
    public String delete(APYEntity entity) throws IllegalArgumentException, APYException {
        if (entity == null) {
            throw new IllegalArgumentException("The given entity was null.");
        }

        if (APYUtils.isNullOrEmpty(entity.getName())) {
            throw new IllegalArgumentException("The name of the given entity was null or empty.");
        }

        if (APYUtils.isNullOrEmpty(entity.getId())) {
            throw new IllegalArgumentException("The ID of the given entity was null or empty.");
        }

        APYHttpRequestInvoker requestInvoker = new APYHttpRequestInvoker(fullApiUrl, requestTimeout);
        return requestInvoker.delete(entity);
    }

    /**
     * Asynchronously deletes the given entity on the apitrary backend.
     * 
     * @param entity
     *            the {@link APYEntity} to delete.
     * @param callback
     *            the {@link APYDeleteCallback} used to inform the caller about
     *            the operations outcome. May be null, if the caller is not interested in the result.
     * @throws IllegalArgumentException if the entity was null or if its name or ID was null or empty
     */
    public void deleteAsync(APYEntity entity, APYDeleteCallback callback) throws IllegalArgumentException {
        if (entity == null) {
            throw new IllegalArgumentException("The given entity was null.");
        }

        if (APYUtils.isNullOrEmpty(entity.getName())) {
            throw new IllegalArgumentException("The name of the given entity was null or empty.");
        }

        if (APYUtils.isNullOrEmpty(entity.getId())) {
            throw new IllegalArgumentException("The ID of the given entity was null or empty.");
        }

        APYHttpRequestInvoker requestInvoker = new APYHttpRequestInvoker(fullApiUrl, requestTimeout);
        new APYDeleteTask(requestInvoker, callback).execute(entity);
    }

    /**
     * Sets the timeout used for any request to the apitrary backend.
     *
     * @param timeout the timeout in milliseconds
     */
    public void setRequestTimeout(int timeout) {
        this.requestTimeout = timeout;
    }

}

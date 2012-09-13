package com.apitrary.sdk;

/**
 * Callback interface used by the {@link APYFetchOneTask} to inform the caller
 * about the operations outcome.
 */
public interface APYFetchOneCallback {

    /**
     * Called if the entity was fetched successfully.
     *
     * @param fetchedEntity the fetched entity
     */
    void onSuccess(APYEntity fetchedEntity);

    /**
     * Called if there was an error while trying to fetch the entity.
     *
     * @param error an instance of {@link APYException} containing further information about the error
     */
    void onError(APYException error);

}

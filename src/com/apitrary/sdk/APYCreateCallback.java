package com.apitrary.sdk;

/**
 * Callback interface used by the {@link APYCreateTask} to inform the caller
 * about the operations outcome.
 */
public interface APYCreateCallback {

    /**
     * Called if the entity was created successfully.
     *
     * @param createdEntity the created entity
     */
    void onSuccess(APYEntity createdEntity);

    /**
     * Called if there was an error while trying to create the entity.
     *
     * @param error an instance of {@link APYException} containing further information about the error
     */
    void onError(APYException error);

}

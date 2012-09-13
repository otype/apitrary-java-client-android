package com.apitrary.sdk;

/**
 * Callback interface used by the {@link APYUpdateTask} to inform the caller
 * about the operations outcome.
 */
public interface APYUpdateCallback {

    /**
     * Called if the entity was updated successfully.
     *
     * @param updatedEntity the updated entity
     */
    void onSuccess(APYEntity updatedEntity);

    /**
     * Called if there was an error while trying to update the entity.
     *
     * @param error an instance of {@link APYException} containing further information about the error
     */
    void onError(APYException error);

}

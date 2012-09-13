package com.apitrary.sdk;

/**
 * Callback interface used by the {@link APYDeleteTask} to inform the caller
 * about the operations outcome.
 */
public interface APYDeleteCallback {

    /**
     * Called if the entity was deleted successfully.
     *
     * @param deletedEntityId the ID of the deleted entity
     */
    void onSuccess(String deletedEntityId);

    /**
     * Called if there was an error while trying to delete the entity.
     *
     * @param error an instance of {@link APYException} containing further information about the error
     */
    void onError(APYException error);

}

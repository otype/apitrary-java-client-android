package com.apitrary.sdk;

import java.util.List;

/**
 * Callback interface used by the {@link APYFetchAllTask} to inform the caller
 * about the operations outcome.
 */
public interface APYFetchAllCallback {

    /**
     * Called if the entities were fetched successfully.
     *
     * @param fetchedEntities the list of fetched entities
     */
    void onSuccess(List<APYEntity> fetchedEntities);

    /**
     * Called if there was an error while trying to fetch all entities.
     *
     * @param error an instance of {@link APYException} containing further information about the error
     */
    void onError(APYException error);

}

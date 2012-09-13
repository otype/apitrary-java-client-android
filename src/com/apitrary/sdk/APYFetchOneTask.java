package com.apitrary.sdk;

import android.os.AsyncTask;

/**
 * {@link AsyncTask} implementation used to asynchronously fetch an entity
 * from the apitrary backend. Uses a {@link APYFetchOneCallback} to inform the
 * caller about the outcome.
 * <p>
 * <b>Notice:</b> The execute() method expects the entity name and the entity ID as String parameters.
 * </p>
 */
class APYFetchOneTask extends AsyncTask<String, Void, APYEntity> {

    /**
     * Callback used to inform the caller about the outcome.
     */
    private APYFetchOneCallback callback;

    /**
     * Invoker for the HTTP request.
     */
    private APYHttpRequestInvoker requestInvoker;

    /**
     * Exception that might have occurred during "doInBackground()" and is
     * evaluated in "onPostExecute()".
     */
    private Exception occuredException;


    /**
     * Constructs an instance of {@link APYFetchAllTask}.
     * 
     * @param requestInvoker
     *            the {@link APYHttpRequestInvoker} instance used to invoke the
     *            actual request.
     * @param callback
     *            the callback used to inform the caller about the outcome.
     */
    APYFetchOneTask(APYHttpRequestInvoker requestInvoker, APYFetchOneCallback callback) {
        // TODO Validate parameters
        this.requestInvoker = requestInvoker;
        this.callback = callback;
    }
    
    @Override
    protected APYEntity doInBackground(String... params) {
        String entityName = params[0];
        String entityId = params[1];

        APYEntity fetchedEntity = null;
        try {
            fetchedEntity = requestInvoker.fetchOne(entityName, entityId);
        } catch (IllegalArgumentException e) {
            occuredException = e;
        } catch (APYException e) {
            occuredException = e;
        }

        return fetchedEntity;
    }

    @Override
    protected void onPostExecute(APYEntity fetchedEntity) {
        if (occuredException == null) {
            callback.onSuccess(fetchedEntity);
        } else {
            callback.onError(new APYException(
                    "Entity could not be fetched. For further information about the actual problem, "
                    + "please inspect the 'cause' object.", occuredException));
        }
    }

}

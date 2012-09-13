package com.apitrary.sdk;

import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;

/**
 * {@link AsyncTask} implementation used to asynchronously fetch entities
 * from the apitrary backend. Uses a {@link APYFetchAllCallback} to inform the
 * caller about the outcome.
 * <p>
 * <b>Notice:</b> The execute() method expects the entity name as single String parameter.
 * </p>
 */
class APYFetchAllTask extends AsyncTask<String, Void, List<APYEntity>> {

    /**
     * Callback used to inform the caller about the outcome.
     */
    private APYFetchAllCallback callback;

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
    APYFetchAllTask(APYHttpRequestInvoker requestInvoker, APYFetchAllCallback callback) {
        // TODO Validate parameters
        this.requestInvoker = requestInvoker;
        this.callback = callback;
    }

    @Override
    protected List<APYEntity> doInBackground(String... params) {
        String entityName = params[0];
        List<APYEntity> resultEntities = new ArrayList<APYEntity>();

        try {
            resultEntities = requestInvoker.fetchAll(entityName);
        } catch (IllegalArgumentException e) {
            occuredException = e;
        } catch (APYException e) {
            occuredException = e;
        }

        return resultEntities;
    }

    @Override
    protected void onPostExecute(List<APYEntity> fetchedEntities) {
        if (occuredException != null) {
            callback.onSuccess(fetchedEntities);
        } else {
            callback.onError(new APYException(
                    "Entities could not be fetched. For further information about the actual problem, "
                    + "please inspect the 'cause' object.", occuredException));
        }
    }

}

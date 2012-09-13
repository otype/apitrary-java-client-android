package com.apitrary.sdk;

import android.os.AsyncTask;

/**
 * {@link AsyncTask} implementation used to asynchronously delete an APYEntity
 * on the apitrary backend. Uses a {@link APYDeleteCallback} to inform the
 * caller about the outcome.
 * <p>
 * <b>Notice:</b> The execute() method expects the entity to delete as single {@link APYEntity} parameter.
 * </p>
 */
class APYDeleteTask extends AsyncTask<APYEntity, Void, String> {

    /**
     * Callback used to inform the caller about the outcome.
     */
    private APYDeleteCallback callback;

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
     * Constructs an instance of {@link APYDeleteTask}.
     * 
     * @param requestInvoker
     *            the {@link APYHttpRequestInvoker} instance used to invoke the
     *            actual request.
     * @param callback
     *            the callback used to inform the caller about the outcome. May
     *            be null if the caller is not interested in the outcome.
     */
    APYDeleteTask(APYHttpRequestInvoker requestInvoker, APYDeleteCallback callback) {
        // TODO Validate parameters
        this.requestInvoker = requestInvoker;
        this.callback = callback;
    }

    @Override
    protected String doInBackground(APYEntity... params) {
        APYEntity entity = params[0];
        String deletedEntityId = null;

        try {
            deletedEntityId = requestInvoker.delete(entity);
        } catch (IllegalArgumentException e) {
            occuredException = e;
        } catch (APYException e) {
            occuredException = e;
        }

        return deletedEntityId;
    }

    @Override
    protected void onPostExecute(String deletedEntityId) {
        if (callback == null) {
            return;
        }

        if (occuredException != null) {
            callback.onSuccess(deletedEntityId);
        } else {
            callback.onError(new APYException(
                    "Entity could not be deleted. For further information about the actual problem, "
                    + "please inspect the 'cause' object.", occuredException));
        }
    }

}

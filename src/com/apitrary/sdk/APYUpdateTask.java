package com.apitrary.sdk;

import android.os.AsyncTask;

/**
 * {@link AsyncTask} implementation used to asynchronously update an APYEntity
 * on the apitrary backend. Uses a {@link APYUpdateCallback} to inform the
 * caller about the outcome.
 * <p>
 * <b>Notice:</b> The execute() method expects the entity to update as single {@link APYEntity} parameter.
 * </p>
 */
class APYUpdateTask extends AsyncTask<APYEntity, Void, APYEntity> {

    /**
     * Callback used to inform the caller about the outcome.
     */
    private APYUpdateCallback callback;

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
     * Constructs an instance of {@link APYUpdateTask}.
     * 
     * @param requestInvoker
     *            the {@link APYHttpRequestInvoker} instance used to invoke the
     *            actual request.
     * @param callback
     *            the callback used to inform the caller about the outcome. May
     *            be null if the caller is not interested in the outcome.
     */
    APYUpdateTask(APYHttpRequestInvoker requestInvoker, APYUpdateCallback callback) {
        // TODO Validate parameters
        this.requestInvoker = requestInvoker;
        this.callback = callback;
    }

    @Override
    protected APYEntity doInBackground(APYEntity... params) {
        APYEntity entity = params[0];

        try {
            entity = requestInvoker.update(entity);
        } catch (IllegalArgumentException e) {
            occuredException = e;
        } catch (APYException e) {
            occuredException = e;
        }

        return entity;
    }

    @Override
    protected void onPostExecute(APYEntity updatedEntity) {
        if (callback == null) {
            return;
        }

        if (occuredException != null) {
            callback.onSuccess(updatedEntity);
        } else {
            callback.onError(new APYException(
                    "Entity could not be updated. For further information about the actual problem, "
                    + "please inspect the 'cause' object.", occuredException));
        }
    }

}

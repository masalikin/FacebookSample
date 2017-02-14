package com.hsv.freeadblockerbrowser.networking;

import android.os.AsyncTask;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public abstract class MyAsyncTask<T> {

    private static final int N_THREADS = 3;
    private static final ThreadPoolExecutor sExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(N_THREADS);

    private final AsyncTask<Void, Void, T> asyncTask;

    private ResponseListener<T> listener;

    public MyAsyncTask() {
        this.asyncTask = getAsyncTask();
    }

    public MyAsyncTask setListener(ResponseListener<T> listener) {
        this.listener = listener;
        return this;
    }

    protected abstract T doInBackground();

    public T executeSync() {
        return doInBackground();
    }

    public void execute() {
        asyncTask.executeOnExecutor(sExecutor);
    }

    public void cancel() {
        listener = null;
        asyncTask.cancel(false);
    }

    public boolean isCancelled() {
        return asyncTask.isCancelled();
    }

    private AsyncTask<Void, Void, T> getAsyncTask() {
        return new AsyncTask<Void, Void, T>() {
            private Exception exceptionCaught = null;

            @Override
            protected T doInBackground(Void... params) {
                if (isCancelled())
                    return null;
                T result = null;
                try {
                    result = MyAsyncTask.this.doInBackground();
                } catch (final Exception e) {
                    exceptionCaught = e;
                }
                return result;
            }

            @Override
            protected void onPostExecute(T t) {
                if (!isCancelled() && listener != null) {
                    if (exceptionCaught != null)
                        listener.onError(exceptionCaught);
                    else {
                        listener.onSuccess(t);
                    }
                }
            }
        };
    }

}

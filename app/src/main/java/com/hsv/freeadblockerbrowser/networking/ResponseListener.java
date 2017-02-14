package com.hsv.freeadblockerbrowser.networking;

public interface ResponseListener<T> {
    void onSuccess(T result);

    void onError(Throwable t);
}

package me.caseload.knockbacksync.async;

import java.util.concurrent.CompletableFuture;

// Paper/Fabric implementation
public class SyncOperation<T> implements AsyncOperation<T> {
    private final T result;

    public SyncOperation(T result) {
        this.result = result;
    }

    @Override
    public CompletableFuture<T> asFuture() {
        return CompletableFuture.completedFuture(result);
    }
}
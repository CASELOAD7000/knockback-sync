package me.caseload.knockbacksync.async;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

// Async implementation for Folia
public class FoliaOperation<T> implements AsyncOperation<T> {
    private final CompletableFuture<T> future;

    public FoliaOperation(CompletableFuture<T> future) {
        this.future = future;
    }

    @Override
    public CompletableFuture<T> asFuture() {
        return future;
    }
}
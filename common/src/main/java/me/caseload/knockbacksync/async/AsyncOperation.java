package me.caseload.knockbacksync.async;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

// Platform-agnostic interface
public interface AsyncOperation<T> {
    CompletableFuture<T> asFuture();
}
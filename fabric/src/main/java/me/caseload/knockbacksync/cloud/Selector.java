package me.caseload.knockbacksync.cloud;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import java.util.Collection;
import java.util.Collections;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface Selector<V> {
    @NonNull String inputString();

    @NonNull Collection<V> values();

    public interface Single<V> extends Selector<V> {
        default @NonNull Collection<V> values() {
            return Collections.singletonList(this.single());
        }

        @NonNull V single();
    }
}

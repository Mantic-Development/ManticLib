package me.fullpage.manticlib.utils.despical;

import me.fullpage.manticlib.utils.ReflectionUtils;

import java.util.concurrent.Callable;

public final class VersionHandle<T> {

    private T handle;
    private int version, patch;

    public VersionHandle(int version, T handle) {
        this(version, 0, handle);
    }

    public VersionHandle(int version, int patch, T handle) {
        if (ReflectionUtils.supports(version, patch)) {
            this.version = version;
            this.patch = patch;
            this.handle = handle;
        }
    }

    public VersionHandle(int version, int patch, Callable<T> handle) {
        if (ReflectionUtils.supports(version, patch)) {
            this.version = version;
            this.patch = patch;

            try {
                this.handle = handle.call();
            } catch (Exception ignored) {
            }
        }
    }

    public VersionHandle(int version, Callable<T> handle) {
        this(version, 0, handle);
    }

    public VersionHandle<T> v(int version, T handle) {
        return v(version, 0, handle);
    }

    public VersionHandle<T> v(int version, int patch, T handle) {
        if (checkVersion(version, patch)) {
            this.version = version;
            this.patch = patch;
            this.handle = handle;
        }
        return this;
    }

    public VersionHandle<T> v(int version, int patch, Callable<T> handle) {
        if (!checkVersion(version, patch)) return this;

        try {
            this.handle = handle.call();
        } catch (Exception ignored) {
        }

        this.version = version;
        this.patch = patch;
        return this;
    }

    private boolean checkVersion(int version, int patch) {
        if (version == this.version && patch == this.patch) {
            throw new IllegalArgumentException("Cannot have duplicate version handles for version: " + version + '.' + patch);
        }
        return version > this.version && patch >= this.patch && ReflectionUtils.supports(version, patch);
    }

    public T orElse(T handle) {
        return this.version == 0 ? handle : this.handle;
    }

    public T orElse(Callable<T> handle) {
        if (this.version == 0) {
            try {
                return handle.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return this.handle;
    }
}

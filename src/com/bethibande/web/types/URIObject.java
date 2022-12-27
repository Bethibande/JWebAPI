package com.bethibande.web.types;

import com.bethibande.web.annotations.URI;
import org.jetbrains.annotations.NotNull;

public record URIObject(@NotNull String uri,
                        @NotNull com.bethibande.web.annotations.URI.URIType type,
                        @NotNull RequestMethod[] methods,
                        int priority) {

    public static URIObject of(URI uri) {
        return new URIObject(uri.value(), uri.type(), uri.methods(), uri.priority());
    }

    public boolean isApplicable(String path) {
        return type.filter.apply(uri, path);
    }
}

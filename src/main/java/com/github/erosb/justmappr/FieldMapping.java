package com.github.erosb.justmappr;

import lombok.RequiredArgsConstructor;

import java.util.function.BiFunction;
import java.util.function.Function;

@RequiredArgsConstructor
public class FieldMapping<E, PK> {
    private final String attributeName;
    private final BiFunction<E, PK, E> setter;
    private final Function<E, PK> getter;

    public String attributeName() {
        return attributeName;
    }
}

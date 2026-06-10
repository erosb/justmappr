package com.github.erosb.justmappr;

import lombok.NonNull;
import lombok.Value;

import java.util.function.BiFunction;
import java.util.function.Function;

@Value
public class FieldMapping<E, F> {
    @NonNull
    String attributeName;
    @NonNull
    Function<E, F> getter;
}

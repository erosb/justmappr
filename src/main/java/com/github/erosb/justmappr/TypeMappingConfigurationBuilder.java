package com.github.erosb.justmappr;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@RequiredArgsConstructor
public class TypeMappingConfigurationBuilder<T> {

    private final Class<T> type;

    private String relationName;

    private FieldMapping<T, ?> primaryKeyMapping;

    private final List<FieldMapping<T, ?>> fieldMappings = new ArrayList<>();

    private ReconstitutionFactory<T> reconstitutionFactory;

    public TypeMappingConfigurationBuilder<T> relationName(String relationName) {
        this.relationName = relationName;
        return this;
    }

    public <PK> TypeMappingConfigurationBuilder<T> primaryKeyMapping(Function<T, PK> getter, String attributeName) {
        this.primaryKeyMapping = new FieldMapping<T, PK>(attributeName, null, getter);
        return this;
    }

    public <F> TypeMappingConfigurationBuilder<T> fieldMapping(Function<T, F> getter, String attributeName) {
        this.fieldMappings.add(new FieldMapping<>(attributeName, null, getter));
        return this;
    }

    public TypeMappingConfigurationBuilder<T> reconstitutionFactory(ReconstitutionFactory<T> reconstitutionFactory) {
        this.reconstitutionFactory = reconstitutionFactory;
        return this;
    }

    public TypeMappingConfiguration build() {
        return new DefaultTypeMappingConfiguration<T>(relationName, type, fieldMappings, primaryKeyMapping,
                reconstitutionFactory);
    }
}

package com.github.erosb.justmappr;

import lombok.RequiredArgsConstructor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.github.erosb.justmappr.TrivialTypeMappingConfiguration.toDBName;
import static java.util.Collections.unmodifiableList;

public interface TypeMappingConfiguration<T> {

    static <T> TypeMappingConfigurationBuilder<T> builderFor(Class<T> type) {
        return new TypeMappingConfigurationBuilder<>(type);
    }

    static <E> BiFunction<E, Object, E> setterFor(Class<E> type, String fieldName) {
        return Arrays.stream(type.getDeclaredMethods())
                .filter(f -> f.getName().equalsIgnoreCase("set" + fieldName))
                .findFirst()
                .map(setterMethod -> {
                    setterMethod.setAccessible(true);
                    return (BiFunction<E, Object, E>) (entity, pk) -> {
                        try {
                            setterMethod.invoke(entity, pk);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        return entity;
                    };
                })
                .orElseThrow();
    }

    static <E> Function<E, Object> getterFor(Class<E> type, String fieldName) {
        return Arrays.stream(type.getDeclaredMethods())
                .filter(f -> f.getName().equalsIgnoreCase("get" + fieldName))
                .findFirst()
                .map(getterMethod -> {
                    getterMethod.setAccessible(true);
                    return (Function<E, Object>) (entity) -> {
                        try {
                            return getterMethod.invoke(entity);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    };
                })
                .orElseThrow();
    }

    static <E> TypeMappingConfiguration trivialMapping(Class<E> type, String primaryKeyProperty) {
        BiFunction<E, Object, E> setter = setterFor(type, primaryKeyProperty);
        Function<E, Object> getter = getterFor(type, primaryKeyProperty);
        return new TrivialTypeMappingConfiguration(type, new FieldMapping<E, Object>(
                toDBName(primaryKeyProperty), setter, getter
        ));
    }

    String getRelationName();

    String getAttributeName(String javaFieldName);

    String getJavaFieldName(String attributeName);

    ReconstitutionFactory<T> getReconstitutionFactory();

    Class<T> getType();

    List<String> getAttributeNames();

    FieldMapping<T, ?> getPrimaryKeyMapping();

    List<FieldMapping<T, ?>> getFieldMappings();
}

class TrivialTypeMappingConfiguration<T>
        implements TypeMappingConfiguration<T> {

    static String toDBName(String simpleName) {
        return simpleName.toLowerCase();
    }

    private final String relationName;
    private final Class<T> javaType;
    private final List<FieldMapping<T, ?>> fieldMappings;
    private final Map<String, String> javaFieldToAttribute;
    private final Map<String, String> attributeToJavaField;
    private final FieldMapping<T, ?> primaryKeyMapping;

    TrivialTypeMappingConfiguration(Class<T> javaType, FieldMapping<T, ?> primaryKeyMapping) {
        this.primaryKeyMapping = primaryKeyMapping;
        this.javaType = javaType;
        relationName = toDBName(javaType.getSimpleName());
        Field[] fields = javaType.getDeclaredFields();
        javaFieldToAttribute = new HashMap<>(fields.length);
        attributeToJavaField = new HashMap<>(fields.length);
        List<FieldMapping<T, ?>> fieldMappings = new ArrayList<>(javaType.getDeclaredFields().length);
        Arrays.stream(fields)
                .map(Field::getName)
                .forEach(fieldName -> {
                    String attributeName = toDBName(fieldName);
                    javaFieldToAttribute.put(fieldName, attributeName);
                    attributeToJavaField.put(attributeName, fieldName);
                    fieldMappings.add(new FieldMapping<T, Object>(
                            fieldName,
                            TypeMappingConfiguration.setterFor(javaType, fieldName),
                            TypeMappingConfiguration.getterFor(javaType, fieldName)
                    ));
                });
        this.fieldMappings = unmodifiableList(fieldMappings);
    }

    @Override
    public String getRelationName() {
        return relationName;
    }

    @Override
    public String getAttributeName(String javaFieldName) {
        return javaFieldToAttribute.get(javaFieldName);
    }

    @Override
    public String getJavaFieldName(String attributeName) {
        return attributeToJavaField.get(attributeName);
    }

    @Override
    public ReconstitutionFactory<T> getReconstitutionFactory() {
        return new SetterBasedReconstitutionFactory<>(javaType, fieldMappings);
    }

    @Override
    public Class<T> getType() {
        return javaType;
    }

    @Override
    public List<String> getAttributeNames() {
        return attributeToJavaField.keySet().stream().toList();
    }

    public FieldMapping<T, ?> getPrimaryKeyMapping() {
        return primaryKeyMapping;
    }

    @Override
    public List<FieldMapping<T, ?>> getFieldMappings() {
        return fieldMappings;
    }


}

@RequiredArgsConstructor
class DefaultTypeMappingConfiguration<T>
        implements TypeMappingConfiguration {

    private final String relationName;
    private final Class<T> javaType;
    private final List<FieldMapping<T, ?>> fieldMappings;
    private final FieldMapping<T, ?> primaryKeyMapping;
    private final ReconstitutionFactory<T> reconstitutionFactory;

    @Override
    public String getRelationName() {
        return relationName;
    }

    @Override
    public String getAttributeName(String javaFieldName) {
        return "";
    }

    @Override
    public String getJavaFieldName(String attributeName) {
        return "";
    }

    @Override
    public ReconstitutionFactory<?> getReconstitutionFactory() {
        return reconstitutionFactory;
    }

    @Override
    public Class<?> getType() {
        return javaType;
    }

    @Override
    public List<String> getAttributeNames() {
        ArrayList<String> attrNames = new ArrayList<>();
        attrNames.add(primaryKeyMapping.getAttributeName());
        attrNames.addAll(fieldMappings.stream().map(FieldMapping::getAttributeName).toList());
        return attrNames;
    }

    @Override
    public FieldMapping<?, ?> getPrimaryKeyMapping() {
        return primaryKeyMapping;
    }

    @Override
    public List<FieldMapping<?, ?>> getFieldMappings() {
        return List.of();
    }
}

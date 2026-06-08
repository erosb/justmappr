package com.github.erosb.justmappr;

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

public interface TypeMappingConfiguration {

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

    Class<?> getType();

    List<String> getAttributeNames();

    FieldMapping<?, ?> getPrimaryKeyMapping();

    List<FieldMapping<?, ?>> getFieldMappings();
}

class TrivialTypeMappingConfiguration<E>
        implements TypeMappingConfiguration {

    static String toDBName(String simpleName) {
        return simpleName.toLowerCase();
    }

    private final String relationName;
    private final Class<?> javaType;
    private final List<FieldMapping<?, ?>> fieldMappings;
    private final Map<String, String> javaFieldToAttribute;
    private final Map<String, String> attributeToJavaField;
    private final FieldMapping<?, ?> primaryKeyMapping;

    TrivialTypeMappingConfiguration(Class<E> javaType, FieldMapping<?, ?> primaryKeyMapping) {
        this.primaryKeyMapping = primaryKeyMapping;
        this.javaType = javaType;
        relationName = toDBName(javaType.getSimpleName());
        Field[] fields = javaType.getDeclaredFields();
        javaFieldToAttribute = new HashMap<>(fields.length);
        attributeToJavaField = new HashMap<>(fields.length);
        List<FieldMapping<?, ?>> fieldMappings = new ArrayList<>(javaType.getDeclaredFields().length);
        Arrays.stream(fields)
                .map(Field::getName)
                .forEach(fieldName -> {
                    String attributeName = toDBName(fieldName);
                    javaFieldToAttribute.put(fieldName, attributeName);
                    attributeToJavaField.put(attributeName, fieldName);
                    fieldMappings.add(new FieldMapping<E, Object>(
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
    public Class<?> getType() {
        return javaType;
    }

    @Override
    public List<String> getAttributeNames() {
        return attributeToJavaField.keySet().stream().toList();
    }

    public FieldMapping<?, ?> getPrimaryKeyMapping() {
        return primaryKeyMapping;
    }

    @Override
    public List<FieldMapping<?, ?>> getFieldMappings() {
        return fieldMappings;
    }
}

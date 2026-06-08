package com.github.erosb.justmappr;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.github.erosb.justmappr.TrivialTypeMappingConfiguration.toDBName;

public interface TypeMappingConfiguration {

    static <E> TypeMappingConfiguration trivialMapping(Class<E> type, String primaryKeyProperty) {
        BiFunction<E, Object, E> setter = Arrays.stream(type.getDeclaredMethods())
                .filter(f -> f.getName().equalsIgnoreCase("set" + primaryKeyProperty))
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
        Function<E, Object> getter = null;
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
}

class TrivialTypeMappingConfiguration
        implements TypeMappingConfiguration {

    static String toDBName(String simpleName) {
        return simpleName.toLowerCase();
    }

    private final String relationName;
    private final Class<?> javaType;
    private final Map<String, String> javaFieldToAttribute;
    private final Map<String, String> attributeToJavaField;
    private final FieldMapping<?, ?> primaryKeyMapping;

    TrivialTypeMappingConfiguration(Class<?> javaType, FieldMapping<?, ?> primaryKeyMapping) {
        this.primaryKeyMapping = primaryKeyMapping;
        this.javaType = javaType;
        relationName = toDBName(javaType.getSimpleName());
        Field[] fields = javaType.getDeclaredFields();
        javaFieldToAttribute = new HashMap<>(fields.length);
        attributeToJavaField = new HashMap<>(fields.length);
        Arrays.stream(fields)
                .map(Field::getName)
                .forEach(fieldName -> {
                    String attributeName = toDBName(fieldName);
                    javaFieldToAttribute.put(fieldName, attributeName);
                    attributeToJavaField.put(attributeName, fieldName);
                });
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
}

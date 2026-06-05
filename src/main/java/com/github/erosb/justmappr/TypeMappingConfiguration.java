package com.github.erosb.justmappr;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface TypeMappingConfiguration {

    static TypeMappingConfiguration trivialMapping(Class<?> type) {
        return new TrivialTypeMappingConfiguration(type);
    }

    String getRelationName();

    String getAttributeName(String javaFieldName);

    String getJavaFieldName(String attributeName);

    Class<?> getType();

    List<String> getAttributeNames();
}

class TrivialTypeMappingConfiguration
        implements TypeMappingConfiguration {

    private static String toDBName(String simpleName) {
        return simpleName.toLowerCase();
    }

    private final String relationName;
    private final Class<?> javaType;
    private final Map<String, String> javaFieldToAttribute;
    private final Map<String, String> attributeToJavaField;

    TrivialTypeMappingConfiguration(Class<?> javaType) {
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
}

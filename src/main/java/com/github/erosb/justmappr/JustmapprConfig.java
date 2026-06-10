package com.github.erosb.justmappr;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;

import java.util.HashMap;
import java.util.Map;

@Builder
public class JustmapprConfig {

    public static class JustmapprConfigBuilder {

        private Map<Class<?>, TypeMappingConfiguration> typeMappingConfig = new HashMap<>();

        JustmapprConfigBuilder typeMapping(TypeMappingConfiguration mappingConfig) {
            typeMappingConfig.put(mappingConfig.getType(), mappingConfig);
            return this;
        }

    }

    @Getter
    private final String connection;

    private final Map<Class<?>, TypeMappingConfiguration> typeMappingConfig;

    TypeMappingConfiguration mappingConfigOfType(Class<?> type) {
        TypeMappingConfiguration mappingConfig = typeMappingConfig.get(type);
        if (mappingConfig == null) {
            throw new UnknownEntityTypeException(type);
        }
        return mappingConfig;
    }




}

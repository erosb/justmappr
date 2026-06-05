package com.github.erosb.justmappr;

import lombok.Builder;
import lombok.Value;

import java.util.HashMap;
import java.util.Map;

@Builder
@Value
public class JustmapprConfig {

    public static class JustmapprConfigBuilder {

        private Map<Class<?>, TypeMappingConfiguration> typeMappingConfig = new HashMap<>();

        JustmapprConfigBuilder typeMapping(TypeMappingConfiguration mappingConfig) {
            typeMappingConfig.put(mappingConfig.getType(), mappingConfig);
            return this;
        }

    }

    String connection;

    Map<Class<?>, TypeMappingConfiguration> typeMappingConfig;
}

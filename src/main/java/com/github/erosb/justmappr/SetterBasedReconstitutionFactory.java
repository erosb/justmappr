package com.github.erosb.justmappr;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static com.github.erosb.justmappr.TypeMappingConfiguration.setterFor;

@RequiredArgsConstructor
class SetterBasedReconstitutionFactory<T>
        implements ReconstitutionFactory<T> {

    static String toJavaName(String attributeName) {
        StringBuilder rval = new StringBuilder();
        boolean nextUppercase = false;
        for (int i = 0; i < attributeName.length(); i++) {
            char c = attributeName.charAt(i);
            if (c == '_') {
                nextUppercase = true;
            } else {
                if (nextUppercase) {
                    rval.append((c + "").toUpperCase());
                    nextUppercase = false;
                } else rval.append(c);
            }
        }
        return rval.toString();
    }

    private final Class<T> javaType;

    private final List<FieldMapping<T, ?>> fieldMappings;

    @Override
    public T reconstitute(ResultSet rs)
            throws SQLException {
        try {
            T instance = (T) javaType.getConstructors()[0].newInstance();
            for (FieldMapping fieldMapping : fieldMappings) {
                setterFor(javaType, toJavaName(fieldMapping.getAttributeName()))
                        .apply(instance, rs.getObject(fieldMapping.getAttributeName()));
            }
            return instance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}

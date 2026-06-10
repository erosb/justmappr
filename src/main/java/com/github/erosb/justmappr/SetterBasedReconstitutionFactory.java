package com.github.erosb.justmappr;

import lombok.RequiredArgsConstructor;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@RequiredArgsConstructor
class SetterBasedReconstitutionFactory<T>
        implements ReconstitutionFactory<T> {

    private final Class<T> javaType;
    private final List<FieldMapping<T, ?>> fieldMappings;

    @Override
    public T reconstitute(ResultSet rs)
            throws SQLException {
        try {
            var instance = javaType.getConstructors()[0].newInstance();
            for (FieldMapping fieldMapping : fieldMappings) {
                fieldMapping.getSetter().apply(instance, rs.getObject(fieldMapping.getAttributeName()));
            }
            return (T) instance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}

package com.github.erosb.justmappr;

import lombok.RequiredArgsConstructor;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import static java.util.stream.Collectors.joining;

public interface Justmappr {

    static Justmappr create(JustmapprConfig config) {
        return new DefaultJustmappr(config);
    }

    static JustmapprConfig.JustmapprConfigBuilder config() {
        return JustmapprConfig.builder();
    }

    <E> E requireById(Class<E> clazz, Object id);
}

@RequiredArgsConstructor
class DefaultJustmappr
        implements Justmappr {

    private final JustmapprConfig config;

    @Override
    public <E> E requireById(Class<E> clazz, Object id) {
        try {
            var conn = DriverManager.getConnection(config.getConnection());
            TypeMappingConfiguration typeMappingConfiguration = config.getTypeMappingConfig().get(clazz);
            var stmt = conn.prepareStatement(baseQuery(clazz) + " WHERE " + typeMappingConfiguration.getPrimaryKeyMapping().attributeName() + " = ?");
            stmt.setString(1, id.toString());
            var rs = stmt.executeQuery();
            if (rs.next()) {
                return instantiate(clazz, rs);
            }
            throw new UnsupportedOperationException("Not supported yet.");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private <E> String baseQuery(Class<E> clazz) {
        var mappingConfig = config.getTypeMappingConfig().get(clazz);
        String selectClause = "SELECT " + mappingConfig.getAttributeNames().stream().collect(joining(", "));
        String fromClause = "FROM " + mappingConfig.getRelationName() + "s";
        return selectClause + " " + fromClause;
    }

    private <E> E instantiate(Class<E> clazz, ResultSet rs) {
        var constr = clazz.getConstructors()[0];
        constr.setAccessible(true);
        var mappingConfig = config.getTypeMappingConfig().get(clazz);
        try {
            var instance = constr.newInstance();
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                var fieldValue = rs.getObject(mappingConfig.getAttributeName(field.getName()));
                field.set(instance, fieldValue);
            }
            return (E) instance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

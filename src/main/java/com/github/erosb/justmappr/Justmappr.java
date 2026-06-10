package com.github.erosb.justmappr;

import lombok.RequiredArgsConstructor;

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

    <E> E requireByPK(Class<E> clazz, Object primaryKey);
}

@RequiredArgsConstructor
class DefaultJustmappr
        implements Justmappr {

    private final JustmapprConfig config;

    @Override
    public <E> E requireByPK(Class<E> clazz, Object primaryKey) {
        try {
            var conn = DriverManager.getConnection(config.getConnection());
            TypeMappingConfiguration typeMappingConfiguration = config.mappingConfigOfType(clazz);
            var stmt = conn.prepareStatement(baseQuery(clazz) + " WHERE " + typeMappingConfiguration.getPrimaryKeyMapping().getAttributeName() + " = ?");
            stmt.setString(1, primaryKey.toString());
            var rs = stmt.executeQuery();
            if (rs.next()) {
                return instantiate(clazz, rs);
            }
            throw new EntityNotFoundException(clazz.getSimpleName() + " not found by primary key " + primaryKey);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private <E> String baseQuery(Class<E> clazz) {
        var mappingConfig = config.mappingConfigOfType(clazz);
        String selectClause = "SELECT " + mappingConfig.getAttributeNames().stream().collect(joining(", "));
        String fromClause = "FROM `" + mappingConfig.getRelationName() + "`";
        return selectClause + " " + fromClause;
    }

    private <E> E instantiate(Class<E> clazz, ResultSet rs) {
        var constr = clazz.getConstructors()[0];
        constr.setAccessible(true);
        var mappingConfig = config.mappingConfigOfType(clazz);
        try {
            return (E) mappingConfig.getReconstitutionFactory().reconstitute(rs);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
//        try {
//            var instance = constr.newInstance();
//            for (FieldMapping fieldMapping : mappingConfig.getFieldMappings()) {
//                System.out.println("set " + fieldMapping.getAttributeName());
//                fieldMapping.getSetter().apply(instance, rs.getObject(fieldMapping.getAttributeName()));
//            }
//            return (E) instance;
//        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
//            throw new RuntimeException(e);
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
    }
}

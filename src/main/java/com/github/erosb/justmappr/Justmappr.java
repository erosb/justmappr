package com.github.erosb.justmappr;

import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

public interface Justmappr {

    static Justmappr create(JustmapprConfig config) {
        return new DefaultJustmappr(config);
    }

    static JustmapprConfig.JustmapprConfigBuilder config() {
        return JustmapprConfig.builder();
    }

    <E> E requireByPK(Class<E> clazz, Object primaryKey);

    <T> void save(T entity);
}

@RequiredArgsConstructor
class DefaultJustmappr
        implements Justmappr {

    private final JustmapprConfig config;

    @Override
    public <E> E requireByPK(Class<E> clazz, Object primaryKey) {
        try {
            var conn = getConnection();
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

    private Connection getConnection()
            throws SQLException {
        return DriverManager.getConnection(config.getConnection());
    }

    @Override
    public <E> void save(E entity) {
        TypeMappingConfiguration<E> mappingConfig = config.mappingConfigOfType(entity.getClass());
        List<FieldMapping<E, ?>> fieldMappings = mappingConfig.getFieldMappings();
        var sql = "INSERT INTO `" + mappingConfig.getRelationName() + "` (" +
        fieldMappings.stream()
                .map(f -> (FieldMapping<E, ?>) f)
                .map(FieldMapping::getAttributeName)
                .collect(joining(", ")) + ") VALUES (" +

        String.join(",", Collections.nCopies(fieldMappings.size(), "?")) + ")";

        try {
            var stmt = getConnection().prepareStatement(sql);
            for (int i = 0; i < fieldMappings.size(); i++) {
                stmt.setObject(i + 1, fieldMappings.get(i).getGetter().apply(entity));
            }
            stmt.execute();
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
    }
}

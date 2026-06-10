package com.github.erosb.justmappr;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReconstitutionFactoryTest {


    @BeforeEach
    @SneakyThrows
    void insertFixtures() {
        Class.forName("org.h2.Driver");
        Connection conn = DriverManager.getConnection("jdbc:h2:mem:test");
        Statement st = conn.createStatement();
        st.execute("create table users (id int primary key auto_increment, user_name text)");
        st.executeUpdate("insert into users (user_name) values ('asdasd'), ('bsdbsd')");
    }

    private Justmappr buildJustmappr() {
        return Justmappr.create(Justmappr.config()
                .connection("jdbc:h2:mem:test")
                .typeMapping(TypeMappingConfiguration.builderFor(User.class)
                        .relationName("users")
                        .primaryKeyMapping(User::getId, "id")
                        .fieldMapping(User::getName, "user_name")
                        .reconstitutionFactory(rs -> new User(rs.getInt("id"), rs.getString("user_name")))
                        .build()
                )
                .build());
    }

    @Test
    void reconstitutionFactoryTest() {
        var justmappr = buildJustmappr();

        var u = justmappr.requireByPK(User.class, 1);

        assertEquals(1, u.getId());
        assertEquals("asdasd", u.getName());
    }
}

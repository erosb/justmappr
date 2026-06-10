package com.github.erosb.justmappr;

import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.erosb.justmappr.TypeMappingConfiguration.trivialMapping;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JustmapprTest {

    @BeforeEach @SneakyThrows
    void insertFixtures() {
        Class.forName("org.h2.Driver");
        Connection conn = DriverManager.getConnection("jdbc:h2:mem:test");
        Statement st = conn.createStatement();
        st.execute("create table users (id int primary key auto_increment, name text)");
        st.executeUpdate("insert into users (name) values ('asdasd'), ('bsdbsd')");
    }

    private static Justmappr buildJustmappr() {
        return Justmappr.create(Justmappr.config()
                .connection("jdbc:h2:mem:test")
                .typeMapping(trivialMapping(User.class, "id"))
                .build());
    }

    @AfterEach
    void tearDown() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:mem:test");
        Statement st = conn.createStatement();
        st.execute("drop table users");
    }

    @Test
    public void requireByPK_success() {
        var justmappr = buildJustmappr();

        User u = justmappr.requireByPK(User.class, 1);

        assertEquals("asdasd", u.getName());
        assertEquals(1, u.getId());
    }

    @Test
    public void requireByPK_notFound() {
        assertThrows(EntityNotFoundException.class, () ->
                buildJustmappr().requireByPK(User.class, 10)
        );
    }

    @Test
    public void requireByPK_unhandledEntity() {
        assertThrows(UnknownEntityTypeException.class, () ->
                buildJustmappr().requireByPK(ConcurrentHashMap.class, 10));
    }

}

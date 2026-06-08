package com.github.erosb.justmappr;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static com.github.erosb.justmappr.TypeMappingConfiguration.trivialMapping;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JustmapprTest {

    @Test
    public void test()
            throws Exception {
        var justmappr = Justmappr.create(Justmappr.config()
                .connection("jdbc:h2:mem:test")
                .typeMapping(trivialMapping(User.class, "id"))
                .build());

        Class.forName("org.h2.Driver");
        Connection conn = DriverManager.getConnection("jdbc:h2:mem:test");
        Statement st = conn.createStatement();
        st.execute("create table users (id int primary key auto_increment, name text)");
        st.executeUpdate("insert into users (name) values ('asdasd'), ('bsdbsd')");

        User u = justmappr.requireById(User.class, 1);
        assertEquals("asdasd", u.getName());
        assertEquals(1, u.getId());
    }
}

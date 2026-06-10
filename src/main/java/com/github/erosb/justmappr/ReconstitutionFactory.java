package com.github.erosb.justmappr;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ReconstitutionFactory<T> {

    T reconstitute(ResultSet rs) throws SQLException;
}

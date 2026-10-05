package com.gastromind.infrastructure;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseCleaner {

    private DatabaseCleaner() {
    }

    //CASCADE vacía también las tablas que apuntan a estas (alérgenos, lotes, albaranes), en el orden correcto
    public static void clean(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("TRUNCATE TABLE product, supplier CASCADE");
        }
    }
}

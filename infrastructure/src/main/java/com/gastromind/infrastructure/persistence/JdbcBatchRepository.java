package com.gastromind.infrastructure.persistence;

import com.gastromind.application.stock.BatchRepository;
import com.gastromind.domain.entity.Batch;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class JdbcBatchRepository implements BatchRepository {

    private static final String SELECT_AVAILABLE = """
            SELECT id, product_id, lot_code, entry_date, expiration_date,
                   purchase_price, currency, unit, initial_quantity, current_quantity
            FROM batch
            WHERE current_quantity > 0
            ORDER BY expiration_date, entry_date, lot_code
            """;

    private final DataSource dataSource;

    public JdbcBatchRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<Batch> findAvailable() {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_AVAILABLE);
             ResultSet resultSet = statement.executeQuery()) {
            List<Batch> batches = new ArrayList<>();
            while (resultSet.next()) {
                batches.add(BatchRows.read(resultSet));
            }
            return batches;
        } catch (SQLException e) {
            throw new RepositoryException("Could not list available batches", e);
        }
    }
}

package com.gastromind.infrastructure.persistence;

import com.gastromind.application.supplier.SupplierRepository;
import com.gastromind.domain.entity.Supplier;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcSupplierRepository implements SupplierRepository {

    private static final String INSERT = """
            INSERT INTO supplier (id, name, tax_id, phone, email)
            VALUES (?, ?, ?, ?, ?)
            """;

    private static final String SELECT_BY_ID = """
            SELECT id, name, tax_id, phone, email
            FROM supplier
            WHERE id = ?
            """;

    private static final String SELECT_ALL = """
            SELECT id, name, tax_id, phone, email
            FROM supplier
            ORDER BY name
            """;

    private final DataSource dataSource;

    public JdbcSupplierRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(Supplier supplier) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT)) {
            statement.setObject(1, supplier.getId());
            statement.setString(2, supplier.getName());
            statement.setString(3, supplier.getTaxId());
            statement.setString(4, supplier.getPhone());
            statement.setString(5, supplier.getEmail());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Could not save supplier " + supplier.getId(), e);
        }
    }

    @Override
    public Optional<Supplier> findById(UUID id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID)) {
            statement.setObject(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(toSupplier(resultSet)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RepositoryException("Could not find supplier " + id, e);
        }
    }

    @Override
    public List<Supplier> findAll() {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL);
             ResultSet resultSet = statement.executeQuery()) {
            List<Supplier> suppliers = new ArrayList<>();
            while (resultSet.next()) {
                suppliers.add(toSupplier(resultSet));
            }
            return suppliers;
        } catch (SQLException e) {
            throw new RepositoryException("Could not list suppliers", e);
        }
    }

    private Supplier toSupplier(ResultSet resultSet) throws SQLException {
        return new Supplier(
                resultSet.getObject("id", UUID.class),
                resultSet.getString("name"),
                resultSet.getString("tax_id"),
                resultSet.getString("phone"),
                resultSet.getString("email")
        );
    }
}

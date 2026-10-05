package com.gastromind.infrastructure.persistence;

import com.gastromind.application.product.ProductRepository;
import com.gastromind.domain.entity.Product;
import com.gastromind.domain.valueobject.Allergen;
import com.gastromind.domain.valueobject.Category;
import com.gastromind.domain.valueobject.UnitOfMeasure;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class JdbcProductRepository implements ProductRepository {

    private static final String INSERT_PRODUCT = """
            INSERT INTO product (id, name, description, category, unit)
            VALUES (?, ?, ?, ?, ?)
            """;

    private static final String INSERT_ALLERGEN = """
            INSERT INTO product_allergen (product_id, allergen)
            VALUES (?, ?)
            """;

    private static final String SELECT_BY_ID = """
            SELECT p.id, p.name, p.description, p.category, p.unit,
                   STRING_AGG(a.allergen, ',') AS allergens
            FROM product p
            LEFT JOIN product_allergen a ON a.product_id = p.id
            WHERE p.id = ?
            GROUP BY p.id, p.name, p.description, p.category, p.unit
            """;

    private static final String SELECT_ALL = """
            SELECT p.id, p.name, p.description, p.category, p.unit,
                   STRING_AGG(a.allergen, ',') AS allergens
            FROM product p
            LEFT JOIN product_allergen a ON a.product_id = p.id
            GROUP BY p.id, p.name, p.description, p.category, p.unit
            ORDER BY p.name
            """;

    private static final String SELECT_BY_IDS = """
            SELECT p.id, p.name, p.description, p.category, p.unit,
                   STRING_AGG(a.allergen, ',') AS allergens
            FROM product p
            LEFT JOIN product_allergen a ON a.product_id = p.id
            WHERE p.id = ANY(?)
            GROUP BY p.id, p.name, p.description, p.category, p.unit
            ORDER BY p.name
            """;

    private final DataSource dataSource;

    public JdbcProductRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(Product product) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                insertProduct(connection, product);
                insertAllergens(connection, product);
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RepositoryException("Could not save product " + product.getId(), e);
        }
    }

    @Override
    public Optional<Product> findById(UUID id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID)) {
            statement.setObject(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(toProduct(resultSet)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RepositoryException("Could not find product " + id, e);
        }
    }

    @Override
    public List<Product> findAll() {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL);
             ResultSet resultSet = statement.executeQuery()) {
            List<Product> products = new ArrayList<>();
            while (resultSet.next()) {
                products.add(toProduct(resultSet));
            }
            return products;
        } catch (SQLException e) {
            throw new RepositoryException("Could not list products", e);
        }
    }

    @Override
    public List<Product> findAllById(Collection<UUID> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_IDS)) {
            statement.setArray(1, connection.createArrayOf("uuid", ids.toArray()));
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Product> products = new ArrayList<>();
                while (resultSet.next()) {
                    products.add(toProduct(resultSet));
                }
                return products;
            }
        } catch (SQLException e) {
            throw new RepositoryException("Could not find products " + ids, e);
        }
    }

    private void insertProduct(Connection connection, Product product) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_PRODUCT)) {
            statement.setObject(1, product.getId());
            statement.setString(2, product.getName());
            statement.setString(3, product.getDescription());
            statement.setString(4, product.getCategory().name());
            statement.setString(5, product.getUnit().name());
            statement.executeUpdate();
        }
    }

    private void insertAllergens(Connection connection, Product product) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_ALLERGEN)) {
            for (Allergen allergen : product.getAllergens()) {
                statement.setObject(1, product.getId());
                statement.setString(2, allergen.name());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private Product toProduct(ResultSet resultSet) throws SQLException {
        return new Product(
                resultSet.getObject("id", UUID.class),
                resultSet.getString("name"),
                resultSet.getString("description"),
                Category.valueOf(resultSet.getString("category")),
                UnitOfMeasure.valueOf(resultSet.getString("unit")),
                toAllergens(resultSet.getString("allergens"))
        );
    }

    private Set<Allergen> toAllergens(String allergens) {
        if (allergens == null) {
            return Set.of();
        }
        return Arrays.stream(allergens.split(","))
                .map(Allergen::valueOf)
                .collect(Collectors.toSet());
    }
}

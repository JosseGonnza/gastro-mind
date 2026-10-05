package com.gastromind.infrastructure.persistence;

import com.gastromind.application.stock.BatchRepository;
import com.gastromind.domain.entity.Batch;
import com.gastromind.domain.valueobject.Money;
import com.gastromind.domain.valueobject.PurchasePrice;
import com.gastromind.domain.valueobject.Quantity;
import com.gastromind.domain.valueobject.UnitOfMeasure;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public class JdbcBatchRepository implements BatchRepository {

    private static final String SELECT_AVAILABLE = """
            SELECT id, product_id, lot_code, entry_date, expiration_date,
                   purchase_price, currency, unit, initial_quantity, current_quantity
            FROM batch
            WHERE current_quantity > 0
            ORDER BY expiration_date, entry_date, lot_code
            """;

    //Numera los lotes de cada producto del más reciente al más antiguo y se queda con el primero.
    //Si dos entraron el mismo día, va delante el más caro por unidad: mejor que el escandallo se pase que se quede corto
    private static final String SELECT_LAST_PURCHASES = """
            SELECT product_id, purchase_price, currency, unit, initial_quantity
            FROM (
                SELECT product_id, purchase_price, currency, unit, initial_quantity,
                       ROW_NUMBER() OVER (
                           PARTITION BY product_id
                           ORDER BY entry_date DESC, purchase_price / initial_quantity DESC
                       ) AS position
                FROM batch
                WHERE product_id = ANY(?)
            ) numbered
            WHERE position = 1
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

    @Override
    public Map<UUID, PurchasePrice> findLastPurchases(Collection<UUID> productIds) {
        if (productIds.isEmpty()) {
            return Map.of();
        }
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_LAST_PURCHASES)) {
            statement.setArray(1, connection.createArrayOf("uuid", productIds.toArray()));
            try (ResultSet resultSet = statement.executeQuery()) {
                Map<UUID, PurchasePrice> lastPurchases = new HashMap<>();
                while (resultSet.next()) {
                    Money paid = new Money(resultSet.getBigDecimal("purchase_price"),
                            Currency.getInstance(resultSet.getString("currency")));
                    Quantity quantity = Quantity.of(resultSet.getBigDecimal("initial_quantity"),
                            UnitOfMeasure.valueOf(resultSet.getString("unit")));
                    lastPurchases.put(resultSet.getObject("product_id", UUID.class), new PurchasePrice(paid, quantity));
                }
                return lastPurchases;
            }
        } catch (SQLException e) {
            throw new RepositoryException("Could not find last purchases", e);
        }
    }
}

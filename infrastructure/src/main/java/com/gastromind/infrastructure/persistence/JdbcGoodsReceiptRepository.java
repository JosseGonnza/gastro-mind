package com.gastromind.infrastructure.persistence;

import com.gastromind.application.receipt.DuplicateDeliveryNoteException;
import com.gastromind.application.receipt.GoodsReceiptRepository;
import com.gastromind.application.receipt.GoodsReceiptSummary;
import com.gastromind.application.receipt.ReceiptFilter;
import com.gastromind.domain.entity.Batch;
import com.gastromind.domain.entity.GoodsReceipt;
import com.gastromind.domain.valueobject.ReceivedGoods;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcGoodsReceiptRepository implements GoodsReceiptRepository {

    private static final String INSERT_RECEIPT = """
            INSERT INTO goods_receipt (id, supplier_id, delivery_note_number, received_on)
            VALUES (?, ?, ?, ?)
            """;

    private static final String INSERT_BATCH = """
            INSERT INTO batch (id, product_id, goods_receipt_id, receipt_line, lot_code, entry_date, expiration_date,
                               purchase_price, currency, unit, initial_quantity, current_quantity)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String SELECT_BY_ID = """
            SELECT r.id AS receipt_id, r.supplier_id, r.delivery_note_number, r.received_on,
                   b.id, b.product_id, b.lot_code, b.entry_date, b.expiration_date,
                   b.purchase_price, b.currency, b.unit, b.initial_quantity, b.current_quantity
            FROM goods_receipt r
            JOIN batch b ON b.goods_receipt_id = r.id
            WHERE r.id = ?
            ORDER BY b.receipt_line
            """;

    private static final String EXISTS_BY_SUPPLIER_AND_NUMBER = """
            SELECT EXISTS (
                SELECT 1
                FROM goods_receipt
                WHERE supplier_id = ? AND delivery_note_number = ?
            )
            """;

    private static final String SELECT_SUMMARIES = """
            SELECT r.id, r.supplier_id, s.name AS supplier_name, r.delivery_note_number, r.received_on,
                   COUNT(b.id) AS line_count, SUM(b.purchase_price) AS total
            FROM goods_receipt r
            JOIN supplier s ON s.id = r.supplier_id
            JOIN batch b ON b.goods_receipt_id = r.id
            WHERE (CAST(? AS UUID) IS NULL OR r.supplier_id = ?)
              AND (CAST(? AS DATE) IS NULL OR r.received_on >= ?)
              AND (CAST(? AS DATE) IS NULL OR r.received_on <= ?)
            GROUP BY r.id, s.id
            ORDER BY r.received_on DESC, r.delivery_note_number DESC
            """;

    private static final String UNIQUE_VIOLATION = "23505";

    private final DataSource dataSource;

    public JdbcGoodsReceiptRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(ReceivedGoods receivedGoods) {
        GoodsReceipt receipt = receivedGoods.receipt();
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                insertReceipt(connection, receipt);
                insertBatches(connection, receipt.getId(), receivedGoods.batches());
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RepositoryException("Could not save goods receipt " + receipt.getId(), e);
        }
    }

    @Override
    public Optional<GoodsReceipt> findById(UUID id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID)) {
            statement.setObject(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                UUID supplierId = resultSet.getObject("supplier_id", UUID.class);
                String deliveryNoteNumber = resultSet.getString("delivery_note_number");
                LocalDate receivedOn = resultSet.getObject("received_on", LocalDate.class);
                List<Batch> batches = new ArrayList<>();
                do {
                    batches.add(BatchRows.read(resultSet));
                } while (resultSet.next());
                return Optional.of(GoodsReceipt.restore(id, supplierId, deliveryNoteNumber, receivedOn, batches));
            }
        } catch (SQLException e) {
            throw new RepositoryException("Could not find goods receipt " + id, e);
        }
    }

    @Override
    public boolean existsBySupplierAndDeliveryNoteNumber(UUID supplierId, String deliveryNoteNumber) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(EXISTS_BY_SUPPLIER_AND_NUMBER)) {
            statement.setObject(1, supplierId);
            statement.setString(2, deliveryNoteNumber);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getBoolean(1);
            }
        } catch (SQLException e) {
            throw new RepositoryException("Could not check delivery note " + deliveryNoteNumber, e);
        }
    }

    @Override
    public List<GoodsReceiptSummary> findSummaries(ReceiptFilter filter) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_SUMMARIES)) {
            statement.setObject(1, filter.supplierId());
            statement.setObject(2, filter.supplierId());
            statement.setObject(3, filter.from());
            statement.setObject(4, filter.from());
            statement.setObject(5, filter.to());
            statement.setObject(6, filter.to());
            try (ResultSet resultSet = statement.executeQuery()) {
                List<GoodsReceiptSummary> summaries = new ArrayList<>();
                while (resultSet.next()) {
                    summaries.add(new GoodsReceiptSummary(
                            resultSet.getObject("id", UUID.class),
                            resultSet.getObject("supplier_id", UUID.class),
                            resultSet.getString("supplier_name"),
                            resultSet.getString("delivery_note_number"),
                            resultSet.getObject("received_on", LocalDate.class),
                            resultSet.getInt("line_count"),
                            resultSet.getBigDecimal("total")));
                }
                return summaries;
            }
        } catch (SQLException e) {
            throw new RepositoryException("Could not list goods receipts", e);
        }
    }

    private void insertReceipt(Connection connection, GoodsReceipt receipt) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_RECEIPT)) {
            statement.setObject(1, receipt.getId());
            statement.setObject(2, receipt.getSupplierId());
            statement.setString(3, receipt.getDeliveryNoteNumber());
            statement.setObject(4, receipt.getReceivedOn());
            statement.executeUpdate();
        } catch (SQLException e) {
            //23505 = clave única repetida; el id es un UUID nuevo, así que solo puede ser proveedor + número de albarán
            if (UNIQUE_VIOLATION.equals(e.getSQLState())) {
                throw new DuplicateDeliveryNoteException(receipt.getDeliveryNoteNumber());
            }
            throw e;
        }
    }

    private void insertBatches(Connection connection, UUID receiptId, List<Batch> batches) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_BATCH)) {
            int line = 1;
            for (Batch batch : batches) {
                statement.setObject(1, batch.getId());
                statement.setObject(2, batch.getProductId());
                statement.setObject(3, receiptId);
                statement.setInt(4, line++);
                statement.setString(5, batch.getSku());
                statement.setObject(6, batch.getEntryDate());
                statement.setObject(7, batch.getExpirationDate());
                statement.setBigDecimal(8, batch.getPurchasePrice().amount());
                statement.setString(9, batch.getPurchasePrice().currency().getCurrencyCode());
                statement.setString(10, batch.getUnit().name());
                statement.setBigDecimal(11, batch.getInitialQuantity().amount());
                statement.setBigDecimal(12, batch.getCurrentQuantity().amount());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }
}

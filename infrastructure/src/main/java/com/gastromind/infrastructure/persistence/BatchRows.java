package com.gastromind.infrastructure.persistence;

import com.gastromind.domain.entity.Batch;
import com.gastromind.domain.valueobject.Money;
import com.gastromind.domain.valueobject.Quantity;
import com.gastromind.domain.valueobject.UnitOfMeasure;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Currency;
import java.util.UUID;

final class BatchRows {

    private BatchRows() {
    }

    static Batch read(ResultSet resultSet) throws SQLException {
        UnitOfMeasure unit = UnitOfMeasure.valueOf(resultSet.getString("unit"));
        return Batch.restore(
                resultSet.getObject("id", UUID.class),
                resultSet.getObject("product_id", UUID.class),
                unit,
                resultSet.getString("lot_code"),
                resultSet.getObject("entry_date", LocalDate.class),
                resultSet.getObject("expiration_date", LocalDate.class),
                new Money(resultSet.getBigDecimal("purchase_price"), Currency.getInstance(resultSet.getString("currency"))),
                Quantity.of(resultSet.getBigDecimal("initial_quantity"), unit),
                Quantity.of(resultSet.getBigDecimal("current_quantity"), unit)
        );
    }
}

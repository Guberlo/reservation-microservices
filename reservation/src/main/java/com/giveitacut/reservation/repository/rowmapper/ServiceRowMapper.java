package com.giveitacut.reservation.repository.rowmapper;

import com.giveitacut.reservation.domain.Service;
import com.giveitacut.reservation.service.ColumnConverter;
import io.r2dbc.spi.Row;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Service}, with proper type conversions.
 */
@Service
public class ServiceRowMapper implements BiFunction<Row, String, Service> {

    private final ColumnConverter converter;

    public ServiceRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Service} stored in the database.
     */
    @Override
    public Service apply(Row row, String prefix) {
        Service entity = new Service();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setName(converter.fromRow(row, prefix + "_name", String.class));
        entity.setDescription(converter.fromRow(row, prefix + "_description", String.class));
        entity.setDuration(converter.fromRow(row, prefix + "_duration", Long.class));
        entity.setPrice(converter.fromRow(row, prefix + "_price", Float.class));
        return entity;
    }
}

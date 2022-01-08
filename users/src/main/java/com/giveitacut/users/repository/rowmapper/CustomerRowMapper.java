package com.giveitacut.users.repository.rowmapper;

import com.giveitacut.users.domain.Customer;
import com.giveitacut.users.service.ColumnConverter;
import io.r2dbc.spi.Row;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Customer}, with proper type conversions.
 */
@Service
public class CustomerRowMapper implements BiFunction<Row, String, Customer> {

    private final ColumnConverter converter;

    public CustomerRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Customer} stored in the database.
     */
    @Override
    public Customer apply(Row row, String prefix) {
        Customer entity = new Customer();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setName(converter.fromRow(row, prefix + "_name", String.class));
        entity.setSurname(converter.fromRow(row, prefix + "_surname", String.class));
        entity.setPhone(converter.fromRow(row, prefix + "_phone", String.class));
        entity.setEmail(converter.fromRow(row, prefix + "_email", String.class));
        entity.setGender(converter.fromRow(row, prefix + "_gender", String.class));
        entity.setUserId(converter.fromRow(row, prefix + "_user_id", String.class));
        return entity;
    }
}

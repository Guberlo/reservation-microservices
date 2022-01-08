package com.giveitacut.users.repository.rowmapper;

import com.giveitacut.users.domain.Employee;
import com.giveitacut.users.service.ColumnConverter;
import io.r2dbc.spi.Row;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Employee}, with proper type conversions.
 */
@Service
public class EmployeeRowMapper implements BiFunction<Row, String, Employee> {

    private final ColumnConverter converter;

    public EmployeeRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Employee} stored in the database.
     */
    @Override
    public Employee apply(Row row, String prefix) {
        Employee entity = new Employee();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setName(converter.fromRow(row, prefix + "_name", String.class));
        entity.setSurname(converter.fromRow(row, prefix + "_surname", String.class));
        entity.setPhone(converter.fromRow(row, prefix + "_phone", String.class));
        entity.setUserId(converter.fromRow(row, prefix + "_user_id", String.class));
        return entity;
    }
}

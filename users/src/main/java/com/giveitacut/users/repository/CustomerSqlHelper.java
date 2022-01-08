package com.giveitacut.users.repository;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Table;

public class CustomerSqlHelper {

    public static List<Expression> getColumns(Table table, String columnPrefix) {
        List<Expression> columns = new ArrayList<>();
        columns.add(Column.aliased("id", table, columnPrefix + "_id"));
        columns.add(Column.aliased("name", table, columnPrefix + "_name"));
        columns.add(Column.aliased("surname", table, columnPrefix + "_surname"));
        columns.add(Column.aliased("phone", table, columnPrefix + "_phone"));
        columns.add(Column.aliased("email", table, columnPrefix + "_email"));
        columns.add(Column.aliased("gender", table, columnPrefix + "_gender"));

        columns.add(Column.aliased("user_id", table, columnPrefix + "_user_id"));
        return columns;
    }
}

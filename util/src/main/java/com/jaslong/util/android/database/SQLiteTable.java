package com.jaslong.util.android.database;

import android.content.ContentValues;

import com.jaslong.util.android.log.Logger;

import java.util.Arrays;
import java.util.List;

/**
 * Base class that can create and drop a SQLite table.
  */
public class SQLiteTable {

    private static final Logger sLog = new Logger("jUtil", "SQLiteTable");

    private final String mName;
    private final List<Column> mColumns;
    private final List<Column> mPrimaryKeys;

    public SQLiteTable(String name, Column[] columns, Column... primaryKeys) {
        mName = name;
        mColumns = Arrays.asList(columns);
        mPrimaryKeys = Arrays.asList(primaryKeys);
    }

    public String getName() {
        return mName;
    }

    public String getCreate() {
        StringBuilder s = new StringBuilder();
        String sep = "";
        for (Column column : mColumns) {
            Column.Description desc = column.getDescription();
            s.append(sep).append(desc.getName()).append(' ').append(desc.getType());
            for (Column.Modifier modifier : desc.getModifiers()) {
                switch (modifier) {
                    case PRIMARY_KEY:
                        s.append(" PRIMARY KEY");
                        break;
                    case NOT_NULL:
                        s.append(" NOT NULL");
                        break;
                    case UNIQUE:
                        s.append(" UNIQUE");
                        break;
                }
            }
            sep = ",";
        }
        if (!mPrimaryKeys.isEmpty()) {
            sep = "";
            s.append(",PRIMARY KEY (");
            for (Column primaryKey : mPrimaryKeys) {
                s.append(sep).append(primaryKey.getDescription().getName());
                sep = ",";
            }
            s.append(")");
        }
        return String.format(
                "CREATE TABLE IF NOT EXISTS %s (%s)",
                mName,
                s.toString());
    }

    public String getDrop() {
        return String.format("DROP TABLE IF EXISTS %s", mName);
    }

    public boolean validateContentValues(ContentValues values) {
        for (Column column : mColumns) {
            Column.Description desc = column.getDescription();
            if (desc.is(Column.Modifier.NOT_NULL) && values.get(desc.getName()) == null) {
                sLog.i(String.format("Validation failed. deck: %s, column: %s", mName, desc));
                return false;
            }
        }
        return true;
    }

}

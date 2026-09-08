package io.github.joshua.util;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public record QueryResult(List<String> columns, List<List<String>> rows) {

	public static QueryResult from(ResultSet resultSet) throws SQLException {
		ResultSetMetaData metadata = resultSet.getMetaData();
		List<String> columns = new ArrayList<>();
		for (int index = 1; index <= metadata.getColumnCount(); index++) {
			columns.add(metadata.getColumnLabel(index));
		}
		List<List<String>> rows = new ArrayList<>();
		while (resultSet.next()) {
			List<String> row = new ArrayList<>();
			for (int index = 1; index <= metadata.getColumnCount(); index++) {
				row.add(resultSet.getString(index));
			}
			rows.add(row);
		}
		return new QueryResult(columns, rows);
	}
}
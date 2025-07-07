// Package declaration
package org.example.daos;

// Required imports
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.example.models.EntryGroup;                       // EntryGroup model class
import org.example.models.EntryGroup.Visibility;           // Visibility enum for EntryGroup
import org.springframework.jdbc.core.JdbcTemplate;         // JDBC helper class
import org.springframework.jdbc.core.RowMapper;            // Interface to map rows of ResultSet
import org.springframework.jdbc.support.GeneratedKeyHolder; // Captures auto-generated keys (e.g., IDs)
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

/**
 * DAO (Data Access Object) for performing CRUD operations on entry_groups table.
 */
@Component
public class EntryGroupDao {

	// JdbcTemplate for executing SQL queries
	private final JdbcTemplate jdbcTemplate;

	// Constructor that initializes JdbcTemplate using a DataSource
	public EntryGroupDao(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	// RowMapper to convert ResultSet rows into EntryGroup objects
	private final RowMapper<EntryGroup> rowMapper = (rs, rowNum) -> {
		EntryGroup group = new EntryGroup();
		group.setId(rs.getLong("id"));
		group.setName(rs.getString("name"));
		group.setDescription(rs.getString("description"));
		group.setVisibility(Visibility.valueOf(rs.getString("visibility")));
		group.setCreatedBy(rs.getString("created_by"));
		return group;
	};

	/**
	 * Returns all entry groups, optionally filtered by a search term (case-insensitive).
	 *
	 * @param search Optional search keyword to filter by group name.
	 * @return List of EntryGroup objects.
	 */
	public List<EntryGroup> getAll(String search) {
		StringBuilder sql = new StringBuilder("SELECT * FROM entry_groups");
		List<Object> params = new ArrayList<>();

		// If search term is provided, filter by group name using LIKE
		if (search != null && !search.trim().isEmpty()) {
			sql.append(" WHERE LOWER(name) LIKE ?");
			params.add("%" + search.toLowerCase() + "%");
		}

		// Execute query and map result
		return jdbcTemplate.query(sql.toString(), rowMapper, params.toArray());
	}

	/**
	 * Returns entry groups created by a specific user, optionally filtered by a search term.
	 *
	 * @param userName The username of the creator.
	 * @param search   Optional search keyword.
	 * @return List of EntryGroup objects.
	 */
	public List<EntryGroup> getByUserId(String userName, String search) {
		StringBuilder sql = new StringBuilder("SELECT * FROM entry_groups WHERE created_by = ?");
		List<Object> params = new ArrayList<>();
		params.add(userName);

		// Apply search filter if present
		if (search != null && !search.trim().isEmpty()) {
			sql.append(" AND LOWER(name) LIKE ?");
			params.add("%" + search.toLowerCase() + "%");
		}

		// Execute query and map result
		return jdbcTemplate.query(sql.toString(), rowMapper, params.toArray());
	}

	/**
	 * Saves a new entry group and returns the persisted object with generated ID.
	 *
	 * @param group EntryGroup object to be saved.
	 * @return The saved EntryGroup object.
	 */
	public EntryGroup save(EntryGroup group) {
		KeyHolder keyHolder = new GeneratedKeyHolder();

		// Execute insert query and capture generated ID
		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(
					"INSERT INTO entry_groups (name, description, visibility, created_by) VALUES (?, ?, ?, ?)",
					Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, group.getName());
			ps.setString(2, group.getDescription());
			ps.setString(3, group.getVisibility().name());
			ps.setString(4, group.getCreatedBy());
			return ps;
		}, keyHolder);

		// Retrieve and return the inserted group
		Long id = keyHolder.getKey().longValue();
		return jdbcTemplate.queryForObject("SELECT * FROM entry_groups WHERE id = ?", rowMapper, id);
	}

	/**
	 * Deletes an entry group by its ID.
	 *
	 * @param id The ID of the group to be deleted.
	 */
	public void deleteById(Long id) {
		jdbcTemplate.update("DELETE FROM entry_groups WHERE id = ?", id);
	}

	/**
	 * Updates an existing entry group and returns the updated object.
	 *
	 * @param entry EntryGroup object with updated values.
	 * @return The updated EntryGroup.
	 */
	public EntryGroup update(EntryGroup entry) {
		// Update the row in the database
		jdbcTemplate.update("UPDATE entry_groups SET name = ?, description = ?, visibility = ? WHERE id = ?",
				entry.getName(), entry.getDescription(), entry.getVisibility().name(), entry.getId());

		// Fetch and return the updated group
		return jdbcTemplate.queryForObject("SELECT * FROM entry_groups WHERE id = ?", rowMapper, entry.getId());
	}
}

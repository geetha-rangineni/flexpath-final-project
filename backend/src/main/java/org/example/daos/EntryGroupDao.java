package org.example.daos;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.example.models.EntryGroup;
import org.example.models.EntryGroup.Visibility;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

@Component
public class EntryGroupDao {

	private final JdbcTemplate jdbcTemplate;

	public EntryGroupDao(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	private final RowMapper<EntryGroup> rowMapper = (rs, rowNum) -> {
		EntryGroup group = new EntryGroup();
		group.setId(rs.getLong("id"));
		group.setName(rs.getString("name"));
		group.setDescription(rs.getString("description"));
		group.setVisibility(Visibility.valueOf(rs.getString("visibility")));
		group.setCreatedBy(rs.getString("created_by"));
		return group;
	};

	public List<EntryGroup> getAll(String search) {
		StringBuilder sql = new StringBuilder("SELECT * FROM entry_groups");
		List<Object> params = new ArrayList<>();

		if (search != null && !search.trim().isEmpty()) {
			sql.append(" WHERE LOWER(name) LIKE ?");
			params.add("%" + search.toLowerCase() + "%");
		}

		return jdbcTemplate.query(sql.toString(), rowMapper, params.toArray());
	}

	public List<EntryGroup> getByUserId(String userName, String search) {
		StringBuilder sql = new StringBuilder("SELECT * FROM entry_groups WHERE created_by = ?");
		List<Object> params = new ArrayList<>();
		params.add(userName);

		if (search != null && !search.trim().isEmpty()) {
			sql.append(" AND LOWER(name) LIKE ?");
			params.add("%" + search.toLowerCase() + "%");
		}

		return jdbcTemplate.query(sql.toString(), rowMapper, params.toArray());
	}

	public EntryGroup save(EntryGroup group) {
		KeyHolder keyHolder = new GeneratedKeyHolder();

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

		Long id = keyHolder.getKey().longValue();
		return jdbcTemplate.queryForObject("SELECT * FROM entry_groups WHERE id = ?", rowMapper, id);
	}

	public void deleteById(Long id) {
		jdbcTemplate.update("DELETE FROM entry_groups WHERE id = ?", id);

	}

	public EntryGroup update(EntryGroup entry) {
		jdbcTemplate.update("UPDATE entry_groups SET name = ?, description = ?, visibility = ? WHERE id = ?",
				entry.getName(), entry.getDescription(), entry.getVisibility().name(), entry.getId());

		return jdbcTemplate.queryForObject("SELECT * FROM entry_groups WHERE id = ?", rowMapper, entry.getId());
	}
}

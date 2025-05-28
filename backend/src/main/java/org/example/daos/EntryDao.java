package org.example.daos;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.example.models.Entry;
import org.example.models.Entry.EntryType;
import org.example.models.EntryGroup;
import org.example.models.EntryGroup.Visibility;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

@Component
public class EntryDao {
	private final JdbcTemplate jdbcTemplate;

	public EntryDao(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	private final RowMapper<Entry> rowMapper = (rs, rowNum) -> {
		Entry entry = new Entry();
		entry.setId(rs.getLong("id"));
		entry.setTitle(rs.getString("title"));
		entry.setType(EntryType.valueOf(rs.getString("type")));
		entry.setDescription(rs.getString("description"));
		entry.setVisibility(Visibility.valueOf(rs.getString("visibility")));
		entry.setDate(rs.getDate("date"));
		entry.setCreatedBy(rs.getString("created_by"));
		return entry;
	};

	public List<Entry> getAll() {
		String sql = """
				SELECT
				    e.id AS entry_id, e.title, e.type, e.description, e.visibility, e.date, e.created_by,
				    g.id AS group_id, g.name AS group_name, g.description AS group_description, g.visibility AS group_visibility, g.created_by AS group_created_by
				FROM entries e
				JOIN entry_groups g ON e.group_id = g.id
				""";

		return jdbcTemplate.query(sql, (rs, rowNum) -> {
			Entry entry = new Entry();
			entry.setId(rs.getLong("entry_id"));
			entry.setTitle(rs.getString("title"));
			entry.setType(Entry.EntryType.valueOf(rs.getString("type")));
			entry.setDescription(rs.getString("description"));
			entry.setVisibility(EntryGroup.Visibility.valueOf(rs.getString("visibility")));
			entry.setDate(rs.getDate("date"));
			entry.setCreatedBy(rs.getString("created_by"));

			EntryGroup group = new EntryGroup();
			group.setId(rs.getLong("group_id"));
			group.setName(rs.getString("group_name"));
			group.setDescription(rs.getString("group_description"));
			group.setVisibility(EntryGroup.Visibility.valueOf(rs.getString("group_visibility")));
			group.setCreatedBy(rs.getString("group_created_by"));

			entry.setGroup(group);
			return entry;
		});
	}

	public List<Entry> getAllByUser(String username) {
		String sql = """
				SELECT
				    e.id AS entry_id, e.title, e.type, e.description, e.visibility, e.date, e.created_by,
				    g.id AS group_id, g.name AS group_name, g.description AS group_description, g.visibility AS group_visibility, g.created_by AS group_created_by
				FROM entries e
				JOIN entry_groups g ON e.group_id = g.id  WHERE e.created_by = ? OR e.visibility = 'PUBLIC'
				""";

		return jdbcTemplate.query(sql, (rs, rowNum) -> {
			Entry entry = new Entry();
			entry.setId(rs.getLong("entry_id"));
			entry.setTitle(rs.getString("title"));
			entry.setType(Entry.EntryType.valueOf(rs.getString("type")));
			entry.setDescription(rs.getString("description"));
			entry.setVisibility(EntryGroup.Visibility.valueOf(rs.getString("visibility")));
			entry.setDate(rs.getDate("date"));
			entry.setCreatedBy(rs.getString("created_by"));

			EntryGroup group = new EntryGroup();
			group.setId(rs.getLong("group_id"));
			group.setName(rs.getString("group_name"));
			group.setDescription(rs.getString("group_description"));
			group.setVisibility(EntryGroup.Visibility.valueOf(rs.getString("group_visibility")));
			group.setCreatedBy(rs.getString("group_created_by"));

			entry.setGroup(group);
			return entry;
		}, username);
	}

	public List<Entry> getByUserId(String userName) {
		return jdbcTemplate.query("SELECT * FROM entries WHERE created_by = ?", rowMapper, userName);
	}
	
	public Optional<Entry> findById(Long id) {
	    String sql = "SELECT * FROM entries WHERE id = ?";
	    List<Entry> list = jdbcTemplate.query(sql, rowMapper, id);
	    return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
	}

	public Entry save(Entry entry) {
		KeyHolder keyHolder = new GeneratedKeyHolder();

		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(
					"INSERT INTO entries (title, type, description, visibility, date, created_by, group_id) VALUES (?, ?, ?, ?, ?, ?, ?)",
					Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, entry.getTitle());
			ps.setString(2, entry.getType().name());
			ps.setString(3, entry.getDescription());
			ps.setString(4, entry.getVisibility().name());
			ps.setDate(5, new java.sql.Date(entry.getDate().getTime()));
			ps.setString(6, entry.getCreatedBy());
			ps.setLong(7, entry.getGroup().getId());
			return ps;
		}, keyHolder);

		Long id = keyHolder.getKey().longValue();
		return jdbcTemplate.queryForObject("SELECT * FROM entries WHERE id = ?", rowMapper, id);
	}

	public Entry update(Entry entry) {
		jdbcTemplate.update(
				"UPDATE entries SET title = ?, type = ?, description = ?, visibility = ?, date = ?, group_id = ? WHERE id = ?",
				entry.getTitle(), entry.getType().name(), entry.getDescription(), entry.getVisibility().name(),
				new java.sql.Date(entry.getDate().getTime()), entry.getGroup().getId(), entry.getId());

		return jdbcTemplate.queryForObject("SELECT * FROM entries WHERE id = ?", rowMapper, entry.getId());
	}

	public void deleteById(Long id) {
		jdbcTemplate.update("DELETE FROM entries WHERE id = ?", id);
	}

	public List<Entry> search(String field, String query, String username, boolean isAdmin) {
		List<String> allowedFields = List.of("title", "description", "type");
		if (!allowedFields.contains(field)) {
			throw new IllegalArgumentException("Invalid search field: " + field);
		}

		StringBuilder sql = new StringBuilder("""
				    SELECT
				        e.id AS entry_id, e.title, e.type, e.description, e.visibility, e.date, e.created_by,
				        g.id AS group_id, g.name AS group_name, g.description AS group_description,
				        g.visibility AS group_visibility, g.created_by AS group_created_by
				    FROM entries e
				    JOIN entry_groups g ON e.group_id = g.id
				    WHERE
				""");

		List<Object> params = new ArrayList<>();

		if (isAdmin) {
			sql.append(" 1=1 ");
		} else {
			sql.append(" (e.created_by = ? OR e.visibility = 'PUBLIC') ");
			params.add(username);
		}

		sql.append(" AND LOWER(e.").append(field).append(") LIKE ? ");
		params.add("%" + query.toLowerCase() + "%");

		return jdbcTemplate.query(sql.toString(), params.toArray(), (rs, rowNum) -> {
			Entry entry = new Entry();
			entry.setId(rs.getLong("entry_id"));
			entry.setTitle(rs.getString("title"));
			entry.setType(Entry.EntryType.valueOf(rs.getString("type")));
			entry.setDescription(rs.getString("description"));
			entry.setVisibility(EntryGroup.Visibility.valueOf(rs.getString("visibility")));
			entry.setDate(rs.getDate("date"));
			entry.setCreatedBy(rs.getString("created_by"));

			EntryGroup group = new EntryGroup();
			group.setId(rs.getLong("group_id"));
			group.setName(rs.getString("group_name"));
			group.setDescription(rs.getString("group_description"));
			group.setVisibility(EntryGroup.Visibility.valueOf(rs.getString("group_visibility")));
			group.setCreatedBy(rs.getString("group_created_by"));

			entry.setGroup(group);
			return entry;
		});
	}

}

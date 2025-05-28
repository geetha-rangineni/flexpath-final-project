package org.example.controllers;

import java.util.List;

import org.example.daos.EntryDao;
import org.example.models.Entry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/entries")
@PreAuthorize("isAuthenticated()")
public class EntryController {

	@Autowired
	private EntryDao entryDao;

	@GetMapping
	public List<Entry> getAll() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = authentication.getName();

		boolean isAdmin = authentication.getAuthorities().stream()
				.anyMatch(auth -> auth.getAuthority().equals("ADMIN"));

		if (isAdmin) {
			return entryDao.getAll();
		}

		return entryDao.getAllByUser(username);
	}

	@GetMapping("/search")
	public List<Entry> searchEntry(@RequestParam String field, @RequestParam String query) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = authentication.getName();
		boolean isAdmin = authentication.getAuthorities().stream()
				.anyMatch(auth -> auth.getAuthority().equals("ADMIN"));
		return entryDao.search(field, query, username, isAdmin);
	}

	@GetMapping("/user/{userName}")
	public List<Entry> getByUser(@PathVariable String userName) {
		return entryDao.getByUserId(userName);
	}

	@PostMapping
	public Entry create(@RequestBody Entry entry) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		entry.setCreatedBy(authentication.getName());
		return entryDao.save(entry);
	}

	@PutMapping("/{id}")
	public Entry update(@PathVariable Long id, @RequestBody Entry entry) {
		entry.setId(id);

		if (entryDao.findById(id).isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry not found with id " + id);
		}

		return entryDao.update(entry);
	}

	@DeleteMapping("/{id}")
	public void delete(@PathVariable Long id) {
		if (entryDao.findById(id).isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry not found with id " + id);
		}
		entryDao.deleteById(id);

	}
}

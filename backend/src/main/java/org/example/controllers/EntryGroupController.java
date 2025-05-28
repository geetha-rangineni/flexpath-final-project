package org.example.controllers;

import java.util.List;

import org.example.daos.EntryGroupDao;
import org.example.models.EntryGroup;
import org.springframework.beans.factory.annotation.Autowired;
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

@RestController
@RequestMapping("/api/groups")
@PreAuthorize("isAuthenticated()")
public class EntryGroupController {
	@Autowired
	private EntryGroupDao groupDao;

	@GetMapping
	public List<EntryGroup> getAll(@RequestParam(required = false) String search) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = authentication.getName();

		boolean isAdmin = authentication.getAuthorities().stream()
				.anyMatch(auth -> auth.getAuthority().equals("ADMIN"));
		if (isAdmin) {
			return groupDao.getAll(search);
		}
		return groupDao.getByUserId(username,search);
	}

//	@GetMapping("/search")
//	public List<Entry> searchEntry(@RequestParam String field, @RequestParam String query) {
//		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//		String username = authentication.getName();
//		boolean isAdmin = authentication.getAuthorities().stream()
//				.anyMatch(auth -> auth.getAuthority().equals("ADMIN"));
//		return groupDao.search(field, query, username, isAdmin);
//	}
	@GetMapping("/user/{username}")
	public List<EntryGroup> getByUser(@PathVariable String username) {
		return groupDao.getByUserId(username,null);
	}

	@PostMapping
	public EntryGroup create(@RequestBody EntryGroup group) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		group.setCreatedBy(authentication.getName());
		return groupDao.save(group);
	}

	@PutMapping("/{id}")
	public EntryGroup update(@PathVariable Long id, @RequestBody EntryGroup entry) {
		entry.setId(id);
		return groupDao.update(entry);
	}

	@DeleteMapping("/{id}")
	public void delete(@PathVariable Long id) {
		groupDao.deleteById(id);

	}
}

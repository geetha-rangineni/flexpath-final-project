// Import static assertion methods from JUnit
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.Date;
import java.util.Arrays;

import org.example.SpringBootApplication;
import org.example.models.Entry;
import org.example.models.Entry.EntryType;
import org.example.models.EntryGroup;
import org.example.models.EntryGroup.Visibility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.databind.ObjectMapper;

import support.FinalTestConfiguration;
import support.WebStoreTest;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, // Launches a web environment with a random port
    classes = SpringBootApplication.class // Runs the actual Spring Boot app for integration testing
)
@Import(FinalTestConfiguration.class) // Injects test-specific config (e.g. mock beans or test data setup)
public class EntryEndpointTests extends WebStoreTest {

    // Used to serialize/deserialize JSON (not actively used in this class but initialized)
    private final ObjectMapper mapper = new ObjectMapper();

    // Test fetching all entries as admin
    @Test
    @DisplayName("GET /api/entries as ADMIN returns all entries")
    public void getAllEntriesAsAdmin() {
        var request = GetAuthEntity("admin", "admin"); // Authenticate as admin
        var result = restTemplate.exchange(
            getBaseUrl() + "/api/entries",
            HttpMethod.GET,
            request,
            Entry[].class
        );

        assertEquals(HttpStatus.OK, result.getStatusCode());
        Entry[] entries = result.getBody();
        assertNotNull(entries);
        assertEquals(7, entries.length); // Expecting 7 entries total
    }

    // Test fetching only "alice"'s entries
    @Test
    @DisplayName("GET /api/entries as user 'alice' returns only alice's entries")
    public void getAllEntriesAsAlice() {
        var request = GetAuthEntity("alice", "alice");
        var result = restTemplate.exchange(
            getBaseUrl() + "/api/entries",
            HttpMethod.GET,
            request,
            Entry[].class
        );

        assertEquals(HttpStatus.OK, result.getStatusCode());
        Entry[] entries = result.getBody();
        assertNotNull(entries);
        assertEquals(3, entries.length);
        Arrays.stream(entries).forEach(e -> assertEquals("alice", e.getCreatedBy()));
    }

    // Test searching for entries by type
    @Test
    @DisplayName("GET /api/entries/search?field=type&query=Workout as alice")
    public void searchEntriesByType() {
        var request = GetAuthEntity("alice", "alice");
        var result = restTemplate.exchange(
            getBaseUrl() + "/api/entries/search?field=type&query=Workout",
            HttpMethod.GET,
            request,
            Entry[].class
        );

        assertEquals(HttpStatus.OK, result.getStatusCode());
        Entry[] entries = result.getBody();
        assertNotNull(entries);
        assertEquals(3, entries.length);
        Arrays.stream(entries).forEach(e -> assertEquals(EntryType.Workout, e.getType()));
    }

    // Test fetching entries for user "bob"
    @Test
    @DisplayName("GET /api/entries/user/bob returns bob's entries")
    public void getEntriesByUserBob() {
        var request = GetAuthEntity("bob", "bob");
        var result = restTemplate.exchange(
            getBaseUrl() + "/api/entries/user/bob",
            HttpMethod.GET,
            request,
            Entry[].class
        );

        assertEquals(HttpStatus.OK, result.getStatusCode());
        Entry[] entries = result.getBody();
        assertNotNull(entries);
        assertEquals(2, entries.length);
        Arrays.stream(entries).forEach(e -> assertEquals("bob", e.getCreatedBy()));
    }

    // Test creating a new entry for "alice"
    @Test
    @DisplayName("POST /api/entries creates a new entry for alice")
    public void postEntryCreatesNew() throws Exception {
        Entry newEntry = new Entry();
        newEntry.setTitle("Yoga");
        newEntry.setType(EntryType.Workout);
        newEntry.setDescription("Morning yoga");
        newEntry.setVisibility(Visibility.PUBLIC);
        newEntry.setDate(Date.valueOf("2025-05-26"));
        EntryGroup grp = new EntryGroup();
        grp.setId(1L);
        newEntry.setGroup(grp);

        var request = GetAuthEntity("alice", "alice", newEntry);
        var result = restTemplate.exchange(
            getBaseUrl() + "/api/entries",
            HttpMethod.POST,
            request,
            Entry.class
        );

        assertEquals(HttpStatus.OK, result.getStatusCode());
        Entry created = result.getBody();
        assertNotNull(created);
        assertEquals("alice", created.getCreatedBy());
        assertEquals("Yoga", created.getTitle());
        assertEquals(8, created.getId()); // Assuming entry ID 8 is the next
    }

    // Test updating an existing entry
    @Test
    @DisplayName("PUT /api/entries/3 updates entry 3")
    public void putEntryUpdates() throws Exception {
        Entry update = new Entry();
        update.setTitle("Updated Diet");
        update.setType(EntryType.Diet);
        update.setDescription("Low carb");
        update.setVisibility(Visibility.PRIVATE);
        update.setDate(Date.valueOf("2025-05-26"));
        EntryGroup grp = new EntryGroup();
        grp.setId(2L);
        update.setGroup(grp);

        var request = GetAuthEntity("alice", "alice", update);
        var result = restTemplate.exchange(
            getBaseUrl() + "/api/entries/3",
            HttpMethod.PUT,
            request,
            Entry.class
        );

        assertEquals(HttpStatus.OK, result.getStatusCode());
        Entry updated = result.getBody();
        assertNotNull(updated);
        assertEquals("Updated Diet", updated.getTitle());
    }

    // Test updating a non-existing entry (ID 999)
    @Test
    @DisplayName("PUT /api/entries/999 returns 404")
    public void putNonexistentReturns404() {
        Entry dummy = new Entry();
        var request = GetAuthEntity("alice", "alice", dummy);
        var result = restTemplate.exchange(
            getBaseUrl() + "/api/entries/999",
            HttpMethod.PUT,
            request,
            Entry.class
        );

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    // Test deleting an entry
    @Test
    @DisplayName("DELETE /api/entries/4 deletes entry 4")
    public void deleteEntry() {
        var request = GetAuthEntity("admin", "admin");
        var result = restTemplate.exchange(
            getBaseUrl() + "/api/entries/4",
            HttpMethod.DELETE,
            request,
            Integer.class
        );

        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    // Test deleting a non-existent entry (ID 999)
    @Test
    @DisplayName("DELETE /api/entries/999 returns 404")
    public void deleteNonexistentReturns404() {
        var request = GetAuthEntity("alice", "alice");
        var result = restTemplate.exchange(
            getBaseUrl() + "/api/entries/999",
            HttpMethod.DELETE,
            request,
            HttpEntity.class
        );

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }
}

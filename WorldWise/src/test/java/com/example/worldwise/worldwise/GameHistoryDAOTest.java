package com.example.worldwise.worldwise;

import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GameHistoryDAOTest {

    private GameHistoryDAO historyDAO;
    private UserDAO userDAO;

    @BeforeAll
    void setupDatabase() throws SQLException {
        Database.initializeDatabase();
        historyDAO = GameHistoryDAO.getInstance();
        userDAO = UserDAO.getInstance();
    }

    @BeforeEach
    void cleanBeforeEach() throws SQLException {
        // wipe tables before every test (keeps tests independent)
        try (Connection conn = Database.connect(); Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM game_attempt");
            stmt.execute("DELETE FROM game_session");
            stmt.execute("DELETE FROM users");
        }
    }

    // used to keep tests simpler
    private void createUser(String email) {
        userDAO.registerUser(new User("Test", "User", email, "pw"));
    }

    @Test
    void testListEmptyForNonExistingUser() {
        // user isn't created - empty user row
        List<GameResult> results = historyDAO.listByUser("idontexist@test.com");
        assertNotNull(results);
        assertTrue(results.isEmpty(), "User should have no history");
    }
    @Test
    void testUserExistsButNoGames() {
        createUser("idontplaygames@test.com");

        List<GameResult> results = historyDAO.listByUser("idontplaygames@test.com");

        assertNotNull(results, "Should return an empty list, not null");
        assertTrue(results.isEmpty(), "Existing user with no sessions should have empty history");
    }

    @Test
    void testShowLatestGameFirstInList() {
        createUser("javabeans@test.com");

        long t1 = System.currentTimeMillis() - 2_000;
        long t2 = System.currentTimeMillis() - 1_000;

        long s1 = historyDAO.startSession("javabeans@test.com", "Map Memory", "Asia", t1 - 500);
        assertTrue(s1 > 0, "Session 1 should be created");
        assertTrue(historyDAO.addAttempt(s1, "Q1", true, 4, 800));
        assertTrue(historyDAO.addAttempt(s1, "Q2", true, 4, 700));
        assertTrue(historyDAO.endSession(s1, t1)); // total 8

        long s2 = historyDAO.startSession("javabeans@test.com", "Map Memory", "Asia", t2 - 500);
        assertTrue(s2 > 0, "Session 2 should be created");
        assertTrue(historyDAO.addAttempt(s2, "Q3", true, 5, 600));
        assertTrue(historyDAO.addAttempt(s2, "Q4", true, 4, 500));
        assertTrue(historyDAO.addAttempt(s2, "Q5", false, 0, 400));
        assertTrue(historyDAO.endSession(s2, t2)); // total 9

        List<GameResult> results = historyDAO.listByUser("javabeans@test.com");
        assertEquals(2, results.size(), "Should return two rows");
        assertTrue(results.get(0).timestampMs() >= results.get(1).timestampMs(), "Newest first");
        assertEquals(9, results.get(0).score());
        assertEquals(8, results.get(1).score());
    }

    @Test
    void testOnlyShowThatUsersGames() {
        createUser("userA@test.com");
        createUser("userB@test.com");

        long now = System.currentTimeMillis();

        long a = historyDAO.startSession("userA@test.com", "Map Memory", "Europe", now - 1000);
        assertTrue(a > 0);
        assertTrue(historyDAO.addAttempt(a, "Q1", true, 5, 700));
        assertTrue(historyDAO.endSession(a, now - 500));

        long b = historyDAO.startSession("userB@test.com", "Map Memory", "Europe", now - 1000);
        assertTrue(b > 0);
        assertTrue(historyDAO.addAttempt(b, "Q1", true, 9, 700));
        assertTrue(historyDAO.endSession(b, now - 500));

        var listA = historyDAO.listByUser("userA@test.com");
        var listB = historyDAO.listByUser("userB@test.com");

        assertEquals(1, listA.size());
        assertEquals("userA@test.com", listA.get(0).userEmail());
        assertEquals(1, listB.size());
        assertEquals("userB@test.com", listB.get(0).userEmail());
    }

    @Test
    void testInvalidHistoryInputFails() {
        createUser("xyz@test.com");
        long now = System.currentTimeMillis();

        // startSession invalid
        assertEquals(-1L, historyDAO.startSession("", "Map Memory", "Asia", now));
        assertEquals(-1L, historyDAO.startSession(null, "Map Memory", "Asia", now));
        assertEquals(-1L, historyDAO.startSession("xyz@test.com", "", "Asia", now));
        assertEquals(-1L, historyDAO.startSession("xyz@test.com", null, "Asia", now));

        long s = historyDAO.startSession("xyz@test.com", "Map Memory", "Asia", now);
        assertTrue(s > 0);

        // addAttempt invalid
        assertFalse(historyDAO.addAttempt(s, "Q", true, -1, 500),
                "Negative points should not be allowed");

        // valid end
        assertTrue(historyDAO.endSession(s, now + 500));
    }

    @Test
    void testSupportsDifferentGameModes() {
        createUser("modes@example.com");
        long now = System.currentTimeMillis();

        long mm = historyDAO.startSession("modes@example.com", "Map Memory", "Africa", now - 1000);
        assertTrue(mm > 0);
        assertTrue(historyDAO.addAttempt(mm, "Q1", true, 7, 600));
        assertTrue(historyDAO.endSession(mm, now - 500));

        long fc = historyDAO.startSession("modes@example.com", "Flashcard Fun", "Flags", now);
        assertTrue(fc > 0);
        assertTrue(historyDAO.addAttempt(fc, "Q2", true, 12, 400));
        assertTrue(historyDAO.addAttempt(fc, "Q3", false, 0, 300));
        assertTrue(historyDAO.endSession(fc, now + 500));

        var results = historyDAO.listByUser("modes@example.com");
        assertEquals(2, results.size());
        assertEquals("Flashcard Fun", results.get(0).mode()); // newest first
        assertEquals("Map Memory", results.get(1).mode());
    }

    @Test
    void testAddAttemptFailsWithInvalidSession() {
        assertFalse(historyDAO.addAttempt(-999, "QX", true, 5, 400),
                "Should not allow adding attempt to invalid session");
    }

    @Test
    void testBoundaryPointsValues() {
        createUser("boundary@test.com");
        long now = System.currentTimeMillis();

        long sid = historyDAO.startSession("boundary@test.com", "Map Memory", "Asia", now);
        assertTrue(sid > 0, "Session should be created successfully");

        // zero points should be accepted
        assertTrue(historyDAO.addAttempt(sid, "Q1", true, 0, 500),
                "Zero points should be allowed");

        // very large value should be accepted
        assertTrue(historyDAO.addAttempt(sid, "Q2", true, Integer.MAX_VALUE, 500),
                "Max int points should be allowed");

        assertTrue(historyDAO.endSession(sid, now + 1000));

        var results = historyDAO.listByUser("boundary@test.com");
        assertEquals(1, results.size());
        assertEquals(Integer.MAX_VALUE, results.get(0).score(),
                "Total score should include large value correctly");
    }

    @Test
    void testSessionWithoutAttempts() {
        createUser("noattempts@example.com");
        long now = System.currentTimeMillis();

        long sid = historyDAO.startSession("noattempts@example.com", "Map Memory", "Oceania", now);
        assertTrue(sid > 0, "Session should be created");
        assertTrue(historyDAO.endSession(sid, now + 500));

        var results = historyDAO.listByUser("noattempts@example.com");
        assertEquals(1, results.size(), "User should have one session in history");
        assertEquals(0, results.get(0).score(), "Score should be zero if no attempts recorded");
    }

    @Test
    void testDirectDatabaseStateMatchesDAO() throws SQLException {
        createUser("verify@example.com");
        long now = System.currentTimeMillis();

        long sid = historyDAO.startSession("verify@example.com", "Map Memory", "Africa", now);
        assertTrue(historyDAO.addAttempt(sid, "Q1", true, 5, 700));
        assertTrue(historyDAO.endSession(sid, now + 500));

        // Query DB directly
        try (Connection conn = Database.connect(); Statement stmt = conn.createStatement()) {
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM game_session WHERE id = " + sid);
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1), "game_session row should exist");

            rs = stmt.executeQuery("SELECT SUM(points) FROM game_attempt WHERE session_id = " + sid);
            assertTrue(rs.next());
            assertEquals(5, rs.getInt(1), "Persisted attempts should sum to correct score");
        }
    }
}

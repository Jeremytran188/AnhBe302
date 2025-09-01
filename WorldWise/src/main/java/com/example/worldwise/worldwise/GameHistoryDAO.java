package com.example.worldwise.worldwise;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GameHistoryDAO {

    private static GameHistoryDAO instance;

    private GameHistoryDAO() {}

    public static GameHistoryDAO getInstance() {
        if (instance == null) {
            instance = new GameHistoryDAO();
        }
        return instance;
    }


    public long startSession(String email, String mode, String topic, long startedAt) {
        if (email == null || email.isBlank() || mode == null || mode.isBlank()) {
            return -1L;
        }

        try (Connection conn = Database.connect()) {
            // find user id using email
            long userId = -1;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id FROM users WHERE email = ?")) {
                ps.setString(1, email);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    userId = rs.getLong("id");
                } else {
                    return -1L; // user not found
                }
            }

            // insert into game_session
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO game_session (user_id, mode, topic, started_at) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, userId);
                ps.setString(2, mode);
                ps.setString(3, topic);
                ps.setTimestamp(4, new Timestamp(startedAt));

                int rows = ps.executeUpdate();
                if (rows == 0) return -1L;

                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1L;
    }

    public boolean addAttempt(long sessionId, String questionId, boolean isCorrect, int points, long timeTakenMs) {
        if (sessionId <= 0 || points < 0) {
            return false;
        }

        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO game_attempt (session_id, question_id, is_correct, points, time_taken_ms) VALUES (?, ?, ?, ?, ?)")) {
            ps.setLong(1, sessionId);
            ps.setString(2, questionId);
            ps.setInt(3, isCorrect ? 1 : 0);
            ps.setInt(4, points);
            ps.setLong(5, timeTakenMs);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean endSession(long sessionId, long endedAt) {
        if (sessionId <= 0) return false;

        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE game_session SET ended_at = ? WHERE id = ?")) {
            ps.setTimestamp(1, new Timestamp(endedAt));
            ps.setLong(2, sessionId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<GameResult> listByUser(String email) {
        List<GameResult> results = new ArrayList<>();
        if (email == null || email.isBlank()) return results;

        String sql = """
            SELECT u.email, gs.id as session_id, gs.mode, gs.topic, gs.ended_at,
                   COALESCE(SUM(ga.points), 0) as total_score
            FROM users u
            LEFT JOIN game_session gs ON u.id = gs.user_id
            LEFT JOIN game_attempt ga ON gs.id = ga.session_id
            WHERE u.email = ? AND gs.id IS NOT NULL
            GROUP BY gs.id
            ORDER BY gs.ended_at DESC
""";


        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Timestamp endedAt = rs.getTimestamp("ended_at");
                long ts = (endedAt != null) ? endedAt.getTime() : 0L;

                results.add(new GameResult(
                        rs.getString("email"),
                        rs.getString("mode"),
                        rs.getString("topic"),
                        rs.getInt("total_score"),
                        ts
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return results;
    }
}

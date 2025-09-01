package com.example.worldwise.worldwise;

public record GameResult(
        String userEmail,
        String mode,
        String topic,
        int score,
        long timestampMs
) {}

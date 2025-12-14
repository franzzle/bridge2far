package com.pimpedpixel.games.gameprogress;

public final class PasswordCodec {

    private static final char[] ALPHABET =
            "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private static final int BASE = 32;

    private PasswordCodec() {
    }

    public static String encode(GameProgress progress) {
        int level = progress.getLevel();
        int attempts = progress.getAttempts();

        int packed = (level << 17) | attempts;
        int checksum = (level + attempts) & 0b111;
        packed = (packed << 3) | checksum;

        StringBuilder sb = new StringBuilder();
        while (packed > 0) {
            sb.append(ALPHABET[packed & 31]);
            packed >>= 5;
        }
        return sb.reverse().toString();
    }

    public static GameProgress decode(String code) {
        int packed = 0;

        for (char c : code.toUpperCase().toCharArray()) {
            packed = (packed << 5) | charToValue(c);
        }

        int checksum = packed & 0b111;
        packed >>= 3;

        int attempts = packed & ((1 << 17) - 1);
        int level = packed >> 17;

        int expectedChecksum = (level + attempts) & 0b111;
        if (checksum != expectedChecksum) {
            throw new IllegalArgumentException("Invalid password");
        }

        return new GameProgress(level, attempts);
    }

    private static int charToValue(char c) {
        for (int i = 0; i < ALPHABET.length; i++) {
            if (ALPHABET[i] == c) {
                return i;
            }
        }
        throw new IllegalArgumentException("Invalid character: " + c);
    }
}

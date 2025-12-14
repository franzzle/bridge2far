package com.pimpedpixel.games.gameprogress;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PasswordCodecTest {

    @Test
    public void encodeDecodeRoundTrip() {
        GameProgress[] samples = new GameProgress[] {
            new GameProgress(1, 0),
            new GameProgress(10, 42),
            new GameProgress(128, 12345),
            new GameProgress(255, 100_000)
        };

        for (GameProgress progress : samples) {
            String code = PasswordCodec.encode(progress);
            GameProgress decoded = PasswordCodec.decode(code);
            assertEquals("Level should survive encode/decode", progress.getLevel(), decoded.getLevel());
            assertEquals("Attempts should survive encode/decode", progress.getAttempts(), decoded.getAttempts());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void decodeRejectsInvalidCharacters() {
        PasswordCodec.decode("HELLO0"); // '0' is not part of the alphabet
    }

    @Test(expected = IllegalArgumentException.class)
    public void decodeRejectsWrongChecksum() {
        GameProgress progress = new GameProgress(12, 3456);
        String tampered = encodeWithWrongChecksum(progress);
        PasswordCodec.decode(tampered);
    }

    @Test
    public void encodeLevel3() {
        GameProgress progress = new GameProgress(3, 3456);
        String cheat = PasswordCodec.encode(progress);
        System.out.println(cheat);

        GameProgress da5AE = PasswordCodec.decode(cheat);
        System.out.printf(Integer.toString(da5AE.getLevel()));
    }

    private static String encodeWithWrongChecksum(GameProgress progress) {
        final char[] alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
        int level = progress.getLevel();
        int attempts = progress.getAttempts();

        int packed = (level << 17) | attempts;
        int checksum = (level + attempts) & 0b111;
        checksum = (checksum + 1) & 0b111; // force incorrect checksum
        packed = (packed << 3) | checksum;

        StringBuilder sb = new StringBuilder();
        while (packed > 0) {
            sb.append(alphabet[packed & 31]);
            packed >>= 5;
        }
        return sb.reverse().toString();
    }
}

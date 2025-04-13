package io.cdap.wrangler.api.parser;

import io.cdap.wrangler.api.annotations.PublicEvolving;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A Token that represents byte size values with units (e.g., 10KB, 5MB).
 */
@PublicEvolving
public class ByteSize extends Token {
    private static final Pattern BYTE_PATTERN = Pattern.compile("^(\\d+\\.?\\d*)\\s*([KMGTP]?B)$", Pattern.CASE_INSENSITIVE);
    private final long bytes;

    public ByteSize(String value) {
        super(TokenType.BYTE_SIZE, value);
        Matcher matcher = BYTE_PATTERN.matcher(value.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException(String.format(
              "'%s' is not a valid byte size. Expected format is '<number><unit>' where unit is B, KB, MB, GB, etc.", value));
        }

        double size = Double.parseDouble(matcher.group(1));
        String unit = matcher.group(2).toUpperCase();

        switch (unit) {
            case "B":
                bytes = (long) size;
                break;
            case "KB":
                bytes = (long) (size * 1024);
                break;
            case "MB":
                bytes = (long) (size * 1024 * 1024);
                break;
            case "GB":
                bytes = (long) (size * 1024 * 1024 * 1024);
                break;
            case "TB":
                bytes = (long) (size * 1024 * 1024 * 1024 * 1024);
                break;
            case "PB":
                bytes = (long) (size * 1024 * 1024 * 1024 * 1024 * 1024);
                break;
            default:
                throw new IllegalArgumentException("Unsupported byte size unit: " + unit);
        }
    }

    /**
     * @return the size in bytes
     */
    public long getBytes() {
        return bytes;
    }

    /**
     * @return the size in kilobytes
     */
    public double getKB() {
        return bytes / 1024.0;
    }

    /**
     * @return the size in megabytes
     */
    public double getMB() {
        return bytes / (1024.0 * 1024.0);
    }

    /**
     * @return the size in gigabytes
     */
    public double getGB() {
        return bytes / (1024.0 * 1024.0 * 1024.0);
    }

    /**
     * @return the size in terabytes
     */
    public double getTB() {
        return bytes / (1024.0 * 1024.0 * 1024.0 * 1024.0);
    }

    /**
     * @return the size in petabytes
     */
    public double getPB() {
        return bytes / (1024.0 * 1024.0 * 1024.0 * 1024.0 * 1024.0);
    }
}
package io.cdap.wrangler.api.parser;

import io.cdap.wrangler.api.annotations.PublicEvolving;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A Token that represents time duration values with units (e.g., 10ms, 5s).
 */
@PublicEvolving
public class TimeDuration extends Token {
    private static final Pattern TIME_PATTERN = Pattern.compile("^(\\d+\\.?\\d*)\\s*(ns|us|ms|s|m|h|d)$", Pattern.CASE_INSENSITIVE);
    private final long nanoseconds;

    public TimeDuration(String value) {
        super(TokenType.TIME_DURATION, value);
        Matcher matcher = TIME_PATTERN.matcher(value.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException(String.format(
              "'%s' is not a valid time duration. Expected format is '<number><unit>' where unit is ns, us, ms, s, m, h, d", value));
        }

        double duration = Double.parseDouble(matcher.group(1));
        String unit = matcher.group(2).toLowerCase();

        switch (unit) {
            case "ns":
                nanoseconds = (long) duration;
                break;
            case "us":
                nanoseconds = (long) (duration * 1000);
                break;
            case "ms":
                nanoseconds = (long) (duration * 1000 * 1000);
                break;
            case "s":
                nanoseconds = (long) (duration * 1000 * 1000 * 1000);
                break;
            case "m":
                nanoseconds = (long) (duration * 1000 * 1000 * 1000 * 60);
                break;
            case "h":
                nanoseconds = (long) (duration * 1000 * 1000 * 1000 * 60 * 60);
                break;
            case "d":
                nanoseconds = (long) (duration * 1000 * 1000 * 1000 * 60 * 60 * 24);
                break;
            default:
                throw new IllegalArgumentException("Unsupported time unit: " + unit);
        }
    }

    /**
     * @return the duration in nanoseconds
     */
    public long getNanoseconds() {
        return nanoseconds;
    }

    /**
     * @return the duration in microseconds
     */
    public double getMicroseconds() {
        return nanoseconds / 1000.0;
    }

    /**
     * @return the duration in milliseconds
     */
    public double getMilliseconds() {
        return nanoseconds / (1000.0 * 1000.0);
    }

    /**
     * @return the duration in seconds
     */
    public double getSeconds() {
        return nanoseconds / (1000.0 * 1000.0 * 1000.0);
    }

    /**
     * @return the duration in minutes
     */
    public double getMinutes() {
        return nanoseconds / (1000.0 * 1000.0 * 1000.0 * 60.0);
    }

    /**
     * @return the duration in hours
     */
    public double getHours() {
        return nanoseconds / (1000.0 * 1000.0 * 1000.0 * 60.0 * 60.0);
    }

    /**
     * @return the duration in days
     */
    public double getDays() {
        return nanoseconds / (1000.0 * 1000.0 * 1000.0 * 60.0 * 60.0 * 24.0);
    }
}
package br.dhsrocha.coordinate;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.Value;
import lombok.val;

/**
 * @author <a href="mailto:dhsrocha@gmail.com">Diego Rocha</a>
 * @since 1.0.0
 */
@Value
@NonNull
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Coordinate {

  public static final Coordinate ORIGIN = of(0, 0);

  double latitude;
  double longitude;

  public static Coordinate of(final double latitude, final double longitude) {
    val c = new Coordinate(latitude, longitude);
    if (Math.abs(c.latitude) > 90 || Math.abs(c.longitude) > 180) {
      throw new IllegalArgumentException("Invalid coordinate parameters.");
    }
    return c;
  }

  public static Coordinate of(final String notation) {
    val m = DMS_PATTERN.matcher(Objects.requireNonNull(notation).trim());
    if (!m.matches()) {
      throw new IllegalArgumentException("Malformed notation: " + notation);
    }
    return of(fromNotation(m, 0), fromNotation(m, 5));
  }

  private static final String NE = "NE";
  private static final Pattern DMS_PATTERN = Pattern.compile(""
      + "(-?)([0-9]{1,2})°([0-5]?[0-9])'([0-5]?[0-9])\"([NS])\\s*"
      + "(-?)([0-1]?[0-9]{1,2})°([0-5]?[0-9])'([0-5]?[0-9])\"([EW])",
    Pattern.CASE_INSENSITIVE);

  private static double fromNotation(final Matcher m, final int offset) {
    return (m.group(1 + offset).isEmpty() ? 1 : -1)
      * (NE.contains(m.group(5 + offset)) ? 1 : -1)
      * (toDoubleOrZero(m.group(2 + offset))
      + (toDoubleOrZero(m.group(3 + offset)) / 60)
      + (toDoubleOrZero(m.group(4 + offset)) / 3600));
  }

  private static double toDoubleOrZero(final String number) {
    return Optional.of(number).filter(s -> !s.isEmpty())
      .map(Double::parseDouble).orElse(0.0);
  }

  // Features

  // ::: Distance from

  @NonNull
  @AllArgsConstructor
  private enum Calculation { // Output should result in meters
    // https://rosettacode.org/wiki/Haversine_formula#Java
    HAVERSINE(src -> tgt -> 2 * 6366707.0195 // Earth's radius
      * Math.asin(Math.sqrt(Math.pow(1
      * Math.sin(Math.toRadians(tgt.latitude - src.latitude) / 2), 2)
      + Math.pow(Math.sin(Math.toRadians(tgt.longitude - src.longitude) / 2), 2)
      * Math.cos(Math.toRadians(src.latitude))
      * Math.cos(Math.toRadians(tgt.latitude))))),
    // TODO
    VICENTY(HAVERSINE.fun),
    ;
    private final Function<Coordinate, Function<Coordinate, Double>> fun;
  }

  public final double distanceTo(final Coordinate destination) {
    val c = Math.abs(latitude - destination.latitude) < 1e-3
      && Math.abs(longitude - destination.longitude) < 1e-3
      ? Calculation.HAVERSINE : Calculation.VICENTY;
    return c.fun.apply(this).apply(destination);
  }
}

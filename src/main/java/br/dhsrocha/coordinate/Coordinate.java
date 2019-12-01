package br.dhsrocha.coordinate;

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
}

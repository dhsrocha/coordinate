package br.dhsrocha.coordinate;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.Value;

/**
 * @author <a href="mailto:dhsrocha@gmail.com">Diego Rocha</a>
 * @since 1.0.0
 */
@Value
@NonNull
@ToString
@AllArgsConstructor(staticName = "of")
public final class Coordinate {

  public static final Coordinate ORIGIN = of(0, 0);

  double latitude;
  double longitude;
}
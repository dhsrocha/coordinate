package br.dhsrocha.coordinate;

import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.BiFunction;
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

  // ::: Closest / farthest

  public final Coordinate farthest(final Coordinate... others) {
    return sort(0, others.length - 1).apply(others, find(this, Boolean.FALSE));
  }

  public final Coordinate closest(final Coordinate... others) {
    return sort(0, others.length - 1).apply(others, find(this, Boolean.TRUE));
  }

  // https://en.wikipedia.org/wiki/Closest_pair_of_points_problem
  private static BiFunction<Coordinate[], BiFunction<Coordinate, Coordinate, Coordinate>, Coordinate>
  sort(final int left, final int right) {
    // Divide-and-conquer strategy, with the merging logic as a function parameter
    return (array, operation) -> left == right ? array[left] : operation.apply(
      sort(left, (left + right) / 2).apply(array, operation), // left
      sort((left + right) / 2 + 1, right).apply(array, operation)); // right
  }

  private static <C extends Coordinate>
  BiFunction<C, C, C> find(final C c, final boolean close) {
    return (c1, c2) -> close ^ c.distanceTo(c1) > c.distanceTo(c2) ? c1 : c2;
  }

  // ::: Route to

  // https://www.baeldung.com/java-simulated-annealing-for-traveling-salesman
  public final Route routeTo(final Coordinate... others) {
    // TODO: should be with the instance in the first position

    // set initial  temp
    var temp = 100000;

    // create random initial solution
    var currentSolution = Route.of(others);

    // We would like to keep track if the best solution
    // Assume best solution is the current solution
    var best = Route.of(currentSolution.coordinates);

    // Loop until system has cooled
    while (temp > 1) {
      // Create new neighbour tour
      val newSolution = Route.of(currentSolution.coordinates);

      // Get random positions in the tour
      val tourPos1 = Route.SEED.nextInt(newSolution.coordinates.length);
      var tourPos2 = Route.SEED.nextInt(newSolution.coordinates.length);

      //to make sure that tourPos1 and tourPos2 are different
      while (tourPos1 == tourPos2) {
        tourPos2 = Route.SEED.nextInt(newSolution.coordinates.length);
      }

      // Get the cities at selected positions in the tour
      val c1 = newSolution.coordinates[tourPos1];
      val c2 = newSolution.coordinates[tourPos2];

      // Swap them
      newSolution.coordinates[tourPos2] = c1;
      newSolution.coordinates[tourPos1] = c2;

      // Get energy of solutions
      val current = currentSolution.distance();
      val neighbour = newSolution.distance();

      // Decide if we should accept the neighbour
      if (Route.acceptance(current, neighbour, temp) > Route.SEED.nextInt()) {
        currentSolution = Route.of(newSolution.coordinates);
      }

      // Keep track of the best solution found
      if (currentSolution.distance() < best.distance()) {
        best = Route.of(currentSolution.coordinates);
      }

      // Cool system
      temp *= 1 - Route.COOLING_RATE;
    }
    return best;
  }

  @NonNull
  @AllArgsConstructor(staticName = "of", access = AccessLevel.PACKAGE)
  public static class Route {

    private static final Random SEED = new Random();
    private static final double COOLING_RATE = 0.003;

    private final Coordinate[] coordinates;

    // https://en.wikipedia.org/wiki/Centroid#Of_a_finite_set_of_points
    public final double distance() {
      var sumX = 0.0;
      var sumY = 0.0;
      for (val c : coordinates) {
        sumX += c.latitude;
        sumY += c.longitude;
      }
      return Coordinate.ORIGIN.distanceTo(Coordinate.of(
        sumX / coordinates.length,
        sumY / coordinates.length));
    }

    private static double acceptance
      (final double current, final double newTemp, final double temp) {
      return newTemp < current ? 1.0 : Math.exp((current - newTemp) / temp);
    }
  }
}

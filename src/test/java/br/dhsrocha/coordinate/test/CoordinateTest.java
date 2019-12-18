package br.dhsrocha.coordinate.test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;

import br.dhsrocha.coordinate.Coordinate;
import java.time.Duration;
import java.util.Set;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:dhsrocha@gmail.com">Diego Rocha</a>
 * @since 1.0.0
 */
@DisplayName("Coordinate test case suite.")
final class CoordinateTest {

  private static final Duration TIMEOUT_LIMIT = Duration.ofMillis(20);

  @Test
  @DisplayName("Should create a coordinate before 20 milliseconds.")
  final void shouldCreate_aCoordinate_before_20Millis() {
    // Assert
    assertTimeout(TIMEOUT_LIMIT, () -> {
      // Act
      val result = Coordinate.of(45, 45);
      // Assert
      assertEquals(45, result.getLatitude());

      assertEquals(45, result.getLongitude());
    });
  }

  @Test
  @DisplayName("should create Origin with latitude longitude as double zero.")
  final void shouldCreate_origin_withLatitudeLongitude_asDoubleZero() {
    // Act / assert
    assertEquals(0, Coordinate.ORIGIN.getLatitude());

    assertEquals(0, Coordinate.ORIGIN.getLongitude());
  }

  @Test
  @DisplayName("Should throw InvalidCoordinateException when latitude passes beyond 90 degrees.")
  final void shouldThrow_invalidCoordinateException_whenLatitude_passesBeyond_90Degrees() {
    // Assert / Act
    assertDoesNotThrow(() -> Coordinate.of(-90, 0));

    assertDoesNotThrow(() -> Coordinate.of(90, 0));

    assertThrows(IllegalArgumentException.class, () -> Coordinate.of(-91, 0));

    assertThrows(IllegalArgumentException.class, () -> Coordinate.of(91, 0));
  }

  @Test
  @DisplayName("Should throw InvalidCoordinateException when longitude passes beyond 180 degrees.")
  final void shouldThrow_invalidCoordinateException_whenLongitude_passesBeyond_180Degrees() {
    // Assert / Act
    assertDoesNotThrow(() -> Coordinate.of(0, -180));

    assertDoesNotThrow(() -> Coordinate.of(0, 180));

    assertThrows(IllegalArgumentException.class, () -> Coordinate.of(0, -181));

    assertThrows(IllegalArgumentException.class, () -> Coordinate.of(0, 181));
  }

  @Test
  @DisplayName("Should parse 45.0125 degrees from valid string notation before 20 milliseconds.")
  final void shouldParse_45_0125degrees_from_validStringNotation_before_20Millis() {
    // Assert
    assertTimeout(TIMEOUT_LIMIT, () -> {
      // Act
      val result = Coordinate.of("45°0'45\"N -45°45'0\"E");
      // Assert
      assertEquals(45.0125, result.getLatitude());

      assertEquals(-45.75, result.getLongitude());
    });
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when passing malformed coordinate notation.")
  final void shouldThrow_illegalArgumentException_whenPassing_malformedCoordinateNotation() {
    // Arrange
    Set.of(""
      , "45°0'0\"N -45°0'0\""
      , "45°0'0\" -45°0'0\"E"
      , "45°0'0\"N,-45°0'0\"E"
      , "45°0'\"N -45°0'0\"E"
      , "45°'0\"N -45°0'0\"E"
      , "°0'0\"N -45°0'0\"E"
      , "45°0'0\"N -45°0'\"E"
      , "45°0'0\"N -45°'0\"E"
      , "45°0'0\"N -°0'0\"E"
      // Act / Assert
    ).parallelStream().forEach(input -> {

      val ex = assertThrows(IllegalArgumentException.class,
        () -> Coordinate.of(input));

      assertEquals("Malformed notation: " + input, ex.getMessage());
    });
  }

  @Test
  @DisplayName("Should measure distance from coordinates.")
  final void shouldMeasure_distanceFrom_coordinates() {
    // Assert
    assertTimeout(TIMEOUT_LIMIT, () -> {
      // Arrange
      val result = Coordinate.of(45, 45)
        // Act
        .distanceTo(Coordinate.of(25, 25));
      // Assert
      assertEquals(2856265.061602412, result);
    });
  }

  @Test
  @DisplayName("Should find the closest and the farthest.")
  final void shouldFind_theNearest_and_theFarthest() {
    // Arrange
    val beijing = Coordinate.of(39.9075, 116.39723);

    val paris = Coordinate.of(48.85341, 2.3488);

    val ottawa = Coordinate.of(45.41117, -75.69812);

    val canberra = Coordinate.of(-35.28346, 149.12807);

    val brasilia = Coordinate.of(-15.77972, -47.92972);
    // Act
    val closest = brasilia.closest(beijing, paris, ottawa, canberra);

    val farthest = brasilia.farthest(beijing, paris, ottawa, canberra);
    // Assert
    assertEquals(ottawa, closest);

    assertEquals(beijing, farthest);
  }

  @Test
  @DisplayName("Should return route sum.")
  final void shouldReturn_routSum() {
    // Arrange
    val c1 = Coordinate.of(5, 5);

    val c2 = Coordinate.of(10, 10);

    val c3 = Coordinate.of(15, 15);
    // Act / Assert
    assertEquals(Coordinate.
        ORIGIN.distanceTo(c1)
        + c1.distanceTo(c2)
        + c2.distanceTo(c3)
      , Coordinate.ORIGIN.routeTo(c3, c1, c2).distance());
  }
}

package br.dhsrocha.coordinate.test;

import br.dhsrocha.coordinate.Coordinate;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

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

      val ex = assertThrows(IllegalArgumentException.class, () -> Coordinate.of(input));

      assertEquals("Malformed notation: " + input, ex.getMessage());
    });
  }

}

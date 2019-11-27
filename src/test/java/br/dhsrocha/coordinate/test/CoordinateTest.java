package br.dhsrocha.coordinate.test;

import br.dhsrocha.coordinate.Coordinate;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeout;

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
      val coordinate = Coordinate.of(45, 45);
      // Assert
      assertEquals(45, coordinate.getLatitude());

      assertEquals(45, coordinate.getLongitude());
    });
  }
}

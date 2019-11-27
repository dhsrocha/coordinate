/**
 * @author <a href="mailto:dhsrocha@gmail.com">Diego Rocha</a>
 * @since 1.0.0
 */
module coordinate.test {

  requires coordinate.core;

  requires lombok;
  requires org.junit.jupiter.api;

  opens br.dhsrocha.coordinate.test to org.junit.jupiter.api;
}


import utest.asserts._
import scala.concurrent.duration._

/**
 * Created by haoyi on 1/24/14.
 */
package object utest extends utest.asserts.Asserts{

  implicit val retryInterval: RetryInterval = new RetryInterval(100.millis)
  implicit val retryMax: RetryMax = new RetryMax(1.second)

  type Show = asserts.Show

  /**
   * Extension methods to allow you to create tests via the "omg"-{ ... }
   * syntax.
   */
  @annotation.compileTimeOnly("String#- method should only be used directly inside a Tests{} macro")
  implicit final class TestableString(private val s: String) extends AnyVal {
    /**
     * Used to demarcate tests with the `TestSuite{ ... }` block. Has no
     * meaning outside that block
     */
    @annotation.compileTimeOnly("String#- method should only be used directly inside a Tests{} macro")
    def -(x: => Any) = ()
  }

}


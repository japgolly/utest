package utest
package runner

//import acyclic.file
import java.util.concurrent.atomic.{AtomicInteger, AtomicReference}
import utest.framework.{DefaultFormatters, DurationAndName}
import scala.annotation.tailrec

object MasterRunner {
  val DurAndName = "^.(\\d+?)\\|(.*)$".r
}

final class MasterRunner(args: Array[String],
                         remoteArgs: Array[String],
                         testClassLoader: ClassLoader,
                         setup: () => Unit,
                         teardown: () => Unit,
                         showSummaryThreshold: Int,
                         startHeader: String => String,
                         resultsHeader: String,
                         failureHeader: String,
                         useSbtLoggers: Boolean,
                         formatter: utest.framework.Formatter)
                         extends BaseRunner(args, remoteArgs, testClassLoader, useSbtLoggers, formatter, Some(startHeader)){
  import MasterRunner._

  setup()
  val summaryOutputLines = new AtomicReference[List[String]](Nil)
  val success = new AtomicInteger(0)
  val failure = new AtomicInteger(0)
  val failureOutputLines = new AtomicReference[List[String]](Nil)

  val durations = new AtomicReference[List[DurationAndName]](Nil)

  override def logDuration(ms: Long, name: Array[String]): Unit = {
    val r = DurationAndName(ms, name)
    @tailrec def go(): Unit = {
      val old = durations.get()
      if (!durations.compareAndSet(old, r :: old)) go()
    }
    go()
  }

  @tailrec def addResult(r: String): Unit = {
    val old = summaryOutputLines.get()
    if (!summaryOutputLines.compareAndSet(old, r :: old)) addResult(r)
  }

  @tailrec final def addFailure(r: String): Unit = {
    val old = failureOutputLines.get()
    if (!failureOutputLines.compareAndSet(old, r :: old)) addFailure(r)
  }

  def incSuccess(): Unit = success.incrementAndGet()
  def incFailure(): Unit = failure.incrementAndGet()

  def successCount: Int = success.get
  def failureCount: Int = failure.get

  def done(): String = {
    teardown()
    val total = success.get() + failure.get()

    if (total > 0) {
      val summary = DefaultFormatters.formatSummary(
        resultsHeader = resultsHeader,
        body = summaryOutputLines.get.mkString("\n"),
        failureMsg =
          if (failureOutputLines.get() == Nil) ""
          else ufansi.Str(failureHeader) ++ ufansi.Str.join(
            // reverse, because the list gets accumulated backwards
            failureOutputLines.get().reverse.flatMap(Seq[ufansi.Str]("\n", _)): _*
          ),
        successCount = success.get(),
        failureCount = failure.get(),
        durations = durations.get(),
        reportSlowest = reportSlowest,
        showSummaryThreshold = showSummaryThreshold
      )
      if (useSbtLoggers) {
        /**
          * Print out the results summary ourselves rather than returning it from
          * `done`, to work around https://github.com/sbt/sbt/issues/3510
          */
        println(summary)
        // Don't print anything, but also don't print the default message it
        // normally prints if you return an empty string, and don't print the
        // [info] gutter it prints if you return " "
        "\n"
      }else{
        summary.toString
      }
    }else{
      "\n"
    }

  }

  def receiveMessage(msg: String): Option[String] = {
    def badMessage = sys.error("bad message: " + msg)
    msg(0) match {
      case 'h' => // hello message. nothing special to do
      case 'r' => addResult(msg.tail)
      case 'f' => addFailure(msg.tail)
      case 'i' => msg(1) match {
        case 's' => incSuccess()
        case 'f' => incFailure()
        case _ => badMessage
      }
      case 'd' => msg match {
        case DurAndName(ms, name) =>
          val a = new Array[String](1)
          a(0) = name
          logDuration(ms.toLong, a)
        case _ => badMessage
      }
      case _ => badMessage
    }

    // We have nothing to send back to the sender slave
    // It is important not to send back anything because of #176
    None
  }

}


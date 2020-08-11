package utest
package framework

//import acyclic.file

object DefaultFormatters{
  def formatSummary(resultsHeader: ufansi.Str,
                    body: ufansi.Str,
                    failureMsg: ufansi.Str,
                    successCount: Int,
                    failureCount: Int,
                    durations: List[DurationAndName],
                    reportSlowest: Option[Int],
                    showSummaryThreshold: Int): ufansi.Str = {

    val totalCount = successCount + failureCount

    var summary: ufansi.Str =
      if (totalCount < showSummaryThreshold) ""
      else ufansi.Str.join(
        resultsHeader, "\n",
        body, "\n",
        failureMsg, "\n"
      )

    if (reportSlowest.isDefined || summary.plainText.getBytes("UTF-8").length > 30000)
      summary = "\n"

    var output =
      ufansi.Str.join(
        summary,
        s"Tests: ", totalCount.toString, ", ",
        s"Passed: ", successCount.toString, ", ",
        s"Failed: ", failureCount.toString)

    for (n <- reportSlowest)
      if (durations.nonEmpty) {
        val sb = new StringBuilder
        @inline def eol(): Unit = sb append '\n'
        val array = durations.toArray
        val totalMs = array.iterator.map(_.durMs).sum.toDouble
        scala.util.Sorting.quickSort(array)(DurationAndName.slowestFirst)
        def slowestN = array.iterator.take(n)
        val nWidth = (n min array.length).toString.length
        val msWidth = "%,d".format(array.head.durMs).length
        val fmt = s"\n  #%${nWidth}d: (%,${msWidth}d ms =%3d%%) %s"
        eol()
        sb append s"Slowest $n tests:"
        var i = 0
        for (x <- slowestN) {
          i += 1
          val pct = x.durMs.toDouble / totalMs * 100
          sb append fmt.format(i, x.durMs, math.round(pct), x.name)
        }
        output ++= sb.toString
      }

    output.render
  }

  def resultsHeader = renderBanner("Results")
  def failureHeader = renderBanner("Failures")
  def renderBanner(s: String) = {
    val dashes = "-" * ((78 - s.length) / 2)
    dashes + " " + s + " " + dashes
  }
}
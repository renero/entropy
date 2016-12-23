/**
  * Created by J Renero on 23/12/16.
  */
package org.renero

import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, SparkSession}

/**
  The entropy H(U) quantifies the uncertainty about an observation.
  The conditional entropy H(U|Fi) quantifies the remaining
  uncertainty if the value of feature i is known. The difference
  of H(U) and H(U|Fi) becomes maximal if the feature fully
  determines the user ID.
*/
class Entropy(spark: SparkSession, argTable: DataFrame) extends Serializable {

  import spark.sqlContext.implicits._
  val table = argTable

  def entropy(X: String): Double = {
    val probs = table.select(table(X)).groupBy(X).count
    val total = table.select(table(X)).count.toDouble
    val result = probs.select(probs("count"), (probs("count") / total) as "prob").
      select('prob, 'prob * log2('prob) as "sigma").
      select(sum('sigma)).
      rdd.map(r => r.getDouble(0)).collect.head
    -1 * result
  }

  def conditionalEntropy(Y: String, X: String): Double = {
    val combs = table.groupBy(Y, X).count
    val totals = table.select(table(Y) as "Y_field_total").
      groupBy("Y_field_total").count.withColumnRenamed("count","count_total")
    val summary = combs.join(totals, combs(Y) === totals("Y_field_total")).drop("Y_field_total")
    val info = summary.select("*").withColumn("frac", summary("count")/summary("count_total")).
      withColumn("info", 'frac*log2('frac))
    val condEntropy = info.groupBy(table(Y)).agg(sum(info("info")) as "H").
      withColumnRenamed(Y, "targetColumn")
    val numOccurrences = table.select(Y).count
    val finalTable = info.select(table(Y), info("count_total")).
      join(condEntropy, info(Y) === condEntropy("targetColumn")).
      drop("targetColumn").distinct
    val result = finalTable.select(finalTable("count_total"), finalTable("H"),
      (finalTable("count_total") / numOccurrences) * finalTable("H") as "Entropy").
      agg(sum('Entropy) as "result").collect.head.getDouble(0)
    -1 * result
  }

  def mRI(Y: String, X: String): Double = {
    val Hcond = conditionalEntropy(Y, X)
    val H = entropy(Y)
    val result = 1.0 - (Hcond / H)
    result
  }

}

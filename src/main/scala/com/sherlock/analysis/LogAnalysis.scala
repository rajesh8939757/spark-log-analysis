package com.sherlock.analysis

import com.typesafe.scalalogging.LazyLogging
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.apache.spark.SparkContext


case class LogRecord(timestamp: String, elb: String, client: String, backend: String, req_process_time: String,
                     back_process_time: String, res_process_time: String, elb_status_cd: String, back_status_cd: String, received_bytes: String,
                    sent_bytes: String, req: String, user_agent: String, ssl_cipher: String, ssl_protocol: String)

object LogAnalysis extends Serializable {

  def main(args: Array[String]): Unit = {

    val spark: SparkSession = SparkSession
      .builder()
      .appName("Spark Log Analysis")
      .master("local[*]")
      .enableHiveSupport().getOrCreate()

    val pattern= """"ˆ(\S+) (\S+) (\S+) (\S+) (\S+) (\S+) (\S+) (\S+) (\S+) (\S+) (\S+) \"([^\"])\" \"([^\"])\" (\S+) (\S+)"""

    import spark.implicits._

    val elb_logs = spark.read.textFile("/Users/rajesh/realtimeprojects/spark-log-Analysis/data/elb_logs.log").map(parseLogRecord).cache()

    val elb_req_cube = elb_logs.withColumn("client_ip", split($"client",":").getItem(0))
      .withColumn("date_time", $"timestamp".cast(TimestampType))
      .sort($"timestamp")
      .select($"client_ip",$"req",$"date_time")


    // 1. sessionize the data using 15 mins as max session time.

    val window_of_cleint_ip = Window.partitionBy($"client_ip").orderBy($"date_time")

    val lag_timestamp = elb_req_cube.withColumn("prev_date_time", lag($"date_time", 1).over(window_of_cleint_ip))

    val web_sessions = lag_timestamp
      .withColumn("session_flag", when((unix_timestamp($"date_time") - unix_timestamp($"prev_date_time") >= (60*15)) or isnull($"prev_date_time"), 1 )
      otherwise 0)

    val session_id = web_sessions.withColumn("session_id",sum($"session_flag").over(window_of_cleint_ip)).cache()

    session_id.show()


    // 2. average session time

    val session_tm_win = session_id.groupBy($"client_ip", $"session_id")
        .agg((unix_timestamp(max($"date_time")) - unix_timestamp(min($"date_time"))).as("session_time"))
        .agg(avg($"session_time").as("avg_session_time"))
    session_tm_win.show()



    // 3. unique url visits per session
    val unq_url_visits = session_id.groupBy($"client_ip", $"session_id")
        .agg(countDistinct($"req").as("distinct_url"))

    unq_url_visits.show()

    // 4.Top 10  most engaged user
    val engaged_users = session_id.groupBy($"client_ip",$"session_id").agg((unix_timestamp(max($"date_time")) - unix_timestamp(min($"date_time"))).as("session_time"))

    val most_engaged_users = engaged_users.groupBy($"client_ip").agg(avg($"session_time")
        .as("avg_session_time"))
        .orderBy($"avg_session_time".desc)

    most_engaged_users.show(10)

  }


  def parseLogRecord(raw_record: String): LogRecord = {

    //"^(\\\\S+) (\\\\S+) (\\\\S+):(\\\\S+) (\\\\S+):(\\\\S+) (\\\\S+) (\\\\S+) (\\\\S+) (\\\\S+) (\\\\S+) (\\\\S+) (\\\\S+) \\\"([^\\\"]*)\\\" \\\"([^\\\"]*)\\\" (\\\\S+) (\\\\S+)$".r

    val pattern=
      """([^ ]*) ([^ ]*) ([^ ]*) ([^ ]*) ([^ ]*) ([^ ]*) ([^ ]*) ([^ ]*) ([^ ]*) ([^ ]*) ([^ ]*) \"([^\"]*)\" \"([^\"]*)\" ([^ ]*) ([^ ]*)""".r



      val line = pattern.findFirstMatchIn(raw_record)

      if (line.isEmpty) {
        throw new RuntimeException("Cannot parse log line: " + raw_record)
      }
      val words = line.get

      LogRecord(words.group(1), words.group(2), words.group(3), words.group(4),
                words.group(5), words.group(6), words.group(7), words.group(8), words.group(9), words.group(10),
                words.group(11), words.group(12), words.group(13), words.group(14), words.group(15))

  }




}

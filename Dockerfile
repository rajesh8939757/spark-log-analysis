FROM bde2020/spark-submit:2.1.0-hadoop2.8-hive-java8

RUN mkdir -p /app
COPY examples/target/original-spark-examples_2.11-2.3.0-SNAPSHOT.jar /app/spark-examples.jar

ENV SPARK_MASTER_NAME spark-master
ENV SPARK_MASTER_PORT 7077
ENV SPARK_APPLICATION_JAR_LOCATION /app/spark-examples.jar
ENV SPARK_APPLICATION_MAIN_CLASS org.apache.spark.examples.SparkPi
ENV SPARK_APPLICATION_ARGS ""
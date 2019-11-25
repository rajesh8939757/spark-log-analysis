# spark-log-analysis

This application has build using following technologies
  * HDFS
  * Hive
  * Hue
  * Spark
  * Docker (Containerization)

Run:

Firstly, makesure to create the hadoop base env by building the below docker image

```bash
  docker build -f sparklog/scala-spark:2.4.4
```

### Build and inject the jars to a container

Prerequisites

1. Docker

Execute the following

```bash
make container
```

The container will be tagged with `spark.log/analysis:1.0`
And then, run the spark application

### To execute it in the conatiner

By container we are talking about `spark.log/analysis:1.0`

1. You have to mount hadoop configurations to `/opt/hadoop/conf`

2. You have to mount spark configurations to `/opt/spark/conf`

3. Example execution command

    ```bash
    $ docker run --rm -it \
        -v $PWD/conf/hadoop:/opt/hadoop/conf \
        -v $PWD/conf/spark/hive-site.xml:/opt/spark/conf/hive-site.xml \
        -v $PWD/conf/spark/spark-defaults.conf:/opt/spark/conf/spark-defaults.conf \
        -v $PWD/conf/spark/java-opts:/opt/spark/conf/java-opts \
        -e HADOOP_USER_NAME='hadoop' \
        -e USER='hadoop' \
        -e GROUP='hdfs' \
        -w /app \
        spark.log/analysis:1.0 \
        spark-submit --class com.sherlock.analysis.LogAnalysis \
                    --verbose \
                    --master yarn \
                    --deploy-mode cluster \
                    --executor-memory 30G \
                    --executor-cores 8 \
                    --driver-memory 30G \
                    spark-log-analysis_2_12.jar
    ```
    
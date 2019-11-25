DIR_SPARK_LOG_ANALYSIS=spark-log-analysis
TAG=spark.log/analysis:1.0

build:
    rm -rf $(DIR_SPARK_LOG_ANALYSIS)/project/target
  	rm -rf $(DIR_SPARK_LOG_ANALYSIS)/target

container
    rm -rf $(DIR_SPARK_LOG_ANALYSIS)/project/target
    rm -rf $(DIR_SPARK_LOG_ANALYSIS)/target
    docker build -t $(TAG) .
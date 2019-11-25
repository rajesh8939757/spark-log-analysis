FROM sparklog/scala-spark:2.4.4 as builder
WORKDIR /app
COPY . .
RUN make build

FROM sparklog/scala-spark:2.4.4
COPY --from=builder /app/spark-log-analysis/target/scala-2.12/spark-log-analysis_2_12.jar /app/spark-log-analysis_2_12.jar
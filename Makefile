build:
  build/mvn -DskipTests clean package
up:
  docker network create spark-net
  docker-compose -f docker-compose-hive.yml up -d
down:
  docker-compose -f docker-compose-hive.yml down
  docker-compose -f docker-compose-spark-app.yml down
  docker network rm spark-net
app:
  docker-compose -f docker-compose-spark-app.yml build
  docker-compose -f docker-compose-spark-app.yml up
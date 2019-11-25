FROM python:3.6

# Never prompts the user for choices on installation/configuration of packages
ENV DEBIAN_FRONTEND noninteractive
ENV TERM linux

ARG SPARK_VERSION=2.1.2
ARG SCALA_VERSION=2.11.8
ARG SBT_VERSION=1.1.5
ARG HADOOP_VERSION=2.7.6
ARG SPARK_PY4J="python/lib/py4j-0.10.4-src.zip"
ARG LZO_VERSION=2.10
ARG HADOOP_LZO_VERSION=0.4.20

RUN echo 'deb [check-valid-until=no] http://archive.debian.org/debian jessie-backports main' > /etc/apt/sources.list.d/backports.list \
    && apt-get update \
    && apt-get install -t jessie-backports --no-install-recommends -y vim-tiny openjdk-8-jdk-headless build-essential maven lzop liblzo2-2 \
    && update-java-alternatives -s java-1.8.0-openjdk-amd64

ENV JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64

ENV HADOOP_HOME=/opt/hadoop
ENV HADOOP_CONF_DIR=${HADOOP_HOME}/conf
ENV HDFS_CONF_DIR=${HADOOP_CONF_DIR}
ENV HIVE_CONF_DIR=${HADOOP_CONF_DIR}
ENV PATH=${PATH}:${HADOOP_HOME}/bin:${HADOOP_HOME}/sbin
RUN (curl http://ftp.tsukuba.wide.ad.jp/software/apache/hadoop/common/hadoop-${HADOOP_VERSION}/hadoop-${HADOOP_VERSION}.tar.gz | \
    tar -xz -C /opt/) \
    && mv /opt/hadoop-${HADOOP_VERSION} ${HADOOP_HOME}

ARG LZO_EXTRACT_LOC=/lzo-bin
ARG HADOOP_LZO_EXTRACT_LOC=/hadoop-lzo-bin
ARG HADOOP_LZO_NATIVES=/hadoop-lzo-natives
ENV LZO_HOME=/opt/lzo
ENV LD_LIBRARY_PATH=${LZO_HOME}/lib
RUN mkdir -p ${LZO_EXTRACT_LOC} \
    && (curl http://www.oberhumer.com/opensource/lzo/download/lzo-${LZO_VERSION}.tar.gz | \
    tar -xz -C ${LZO_EXTRACT_LOC}) \
    && mkdir -p ${LZO_HOME} \
    && cd ${LZO_EXTRACT_LOC}/lzo-${LZO_VERSION} \
    && ./configure --enable-shared --prefix ${LZO_HOME} \
    && make && make install \
    && rm -rf ${LZO_EXTRACT_LOC} \
    && mkdir -p ${HADOOP_LZO_EXTRACT_LOC} && mkdir -p ${HADOOP_LZO_NATIVES} \
    && cd ${HADOOP_LZO_EXTRACT_LOC} \
    && git clone https://github.com/twitter/hadoop-lzo.git \
    && cd hadoop-lzo \
    && git checkout release-${HADOOP_LZO_VERSION} \
    && C_INCLUDE_PATH=${LZO_HOME}/include LIBRARY_PATH=${LZO_HOME}/lib mvn clean package \
    && cd target/native/Linux-amd64-64 \
    && tar -cBf - -C lib . | tar -xBvf - -C ${HADOOP_LZO_NATIVES} \
    && cp ${HADOOP_LZO_NATIVES}/libgplcompression* ${HADOOP_HOME}/lib/native/ \
    && cp ${HADOOP_LZO_EXTRACT_LOC}/hadoop-lzo/target/hadoop-lzo-${HADOOP_LZO_VERSION}.jar ${HADOOP_HOME}/share/hadoop/common/ \
    && rm -rf ${HADOOP_LZO_EXTRACT_LOC} ${HADOOP_LZO_NATIVES}

ARG SPARK_EXTRACT_LOC=/spark-bin
ENV SPARK_HOME=/opt/spark
ENV SPARK_CONF_DIR=${SPARK_HOME}/conf
ENV PATH=$PATH:${SPARK_HOME}/bin
ENV PYTHONPATH=${SPARK_HOME}/${SPARK_PY4J}:${SPARK_HOME}/python
ENV PYSPARK_SUBMIT_ARGS="--driver-memory 8g --py-files ${SPARK_HOME}/python/lib/pyspark.zip pyspark-shell"
RUN /bin/bash -c 'mkdir -p ${SPARK_EXTRACT_LOC} \
    && (curl http://ftp.tsukuba.wide.ad.jp/software/apache/spark/spark-${SPARK_VERSION}/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION:0:3}.tgz | \
    tar -xz -C ${SPARK_EXTRACT_LOC}) \
    && mkdir -p ${SPARK_HOME} \
    && mv ${SPARK_EXTRACT_LOC}/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION:0:3}/* ${SPARK_HOME} \
    && rm -rf ${SPARK_EXTRACT_LOC} \
    && wget http://repo1.maven.org/maven2/com/sun/jersey/jersey-bundle/1.19.4/jersey-bundle-1.19.4.jar -P ${SPARK_HOME}/jars'

ENV SCALA_HOME /opt/scala
ENV PATH=$PATH:${SCALA_VERSION}/bin
RUN (curl https://downloads.lightbend.com/scala/${SCALA_VERSION}/scala-${SCALA_VERSION}.tgz | \
    tar -xz -C /opt/) \
    && mv /opt/scala-${SCALA_VERSION} ${SCALA_HOME}

RUN curl -L -o sbt-${SBT_VERSION}.deb https://dl.bintray.com/sbt/debian/sbt-${SBT_VERSION}.deb \
    && dpkg -i sbt-${SBT_VERSION}.deb \
    && rm sbt-${SBT_VERSION}.deb \
    && apt-get -y update \
    && apt-get -y install sbt \
    && sbt sbtVersion

RUN rm -rf /var/lib/apt/lists/* \
    && apt-get purge --auto-remove -y build-essential maven \
    && apt-get clean autoclean \
    && apt-get autoremove -y --purge \
    && rm -rf \
        /var/lib/apt/lists/* \
        /tmp/* \
        /var/tmp/* \
        /usr/share/man \
        /usr/share/doc \
        /usr/share/doc-base


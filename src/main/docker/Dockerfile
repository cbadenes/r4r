FROM anapsix/alpine-java:8u162b12_server-jre
MAINTAINER Carlos Badenes-Olmedo <cbadenes@fi.upm.es>
VOLUME /tmp
ADD r4r.jar /app.jar
ADD bin /resources
ENTRYPOINT exec java $JAVA_OPTS -server -jar /app.jar

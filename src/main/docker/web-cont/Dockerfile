FROM tomcat:8.5-jre8
MAINTAINER Mikhail Ashurov <ashurov.mf@gmail.com>
RUN mkdir /data
VOLUME /data
COPY webstream.war /usr/local/tomcat/webapps/
CMD ["catalina.sh", "run"]
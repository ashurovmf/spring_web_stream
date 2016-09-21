FROM postgres:9.3
MAINTAINER Mikhail Ashurov <ashurov.mf@gmail.com>
ENV POSTGRES_PASSWORD hotCave13

USER root

# Add web security data
ADD init-db.sh /docker-entrypoint-initdb.d/init-db.sh

ADD mktstore.sql /tmp/mktstore.sql

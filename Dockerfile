# Deploy war to Tomcat
# presumes that the war file has been generated locally.
FROM tomcat:9-jre8
ARG env=dev
LABEL maintainer="ramaswamy.ramanathan@anz.com"
EXPOSE 8080

ENV TOMCAT_HOME "/usr/local/tomcat"
ENV APP_HOME "${TOMCAT_HOME}/app_home"
ENV CATALINA_OPTS="-Dapp.home=${APP_HOME}"

COPY ./build/distributions/war ${TOMCAT_HOME}/webapps

COPY config/${env}/app-core.properties ${APP_HOME}/app-core/local.app.properties
COPY config/${env}/app-web.properties ${APP_HOME}/app/local.app.properties

# start
CMD ["catalina.sh", "run"]

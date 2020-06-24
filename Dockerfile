FROM tomcat:9-jdk11-openjdk-slim
COPY core-bc/modules/web/target/bestallning20.war /usr/local/tomcat/webapps/
COPY dockerfiles/setenv.sh /usr/local/tomcat/bin/
COPY dockerfiles/log4j2/ /usr/local/tomcat/log4j2/
RUN chmod +x /usr/local/tomcat/bin/setenv.sh

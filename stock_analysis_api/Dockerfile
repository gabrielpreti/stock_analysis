FROM java:8
ARG app_jar

EXPOSE 8080
COPY $app_jar /opt/stock_analysis_api.jar

CMD ["java", "-Xdebug", "-Xrunjdwp:server=y,transport=dt_socket,address=8000,suspend=n", "-jar", "/opt/stock_analysis_api.jar"]

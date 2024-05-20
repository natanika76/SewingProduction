FROM eclipse-temurin:17.0.8_7-jre
ARG JAR_FILE=target/Sewing.jar
WORKDIR /opt/app
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","app.jar"]
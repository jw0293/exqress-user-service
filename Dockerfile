FROM gradle:8.0.2-jdk17 AS mbuilder 
COPY ./user-service /usr/src/
WORKDIR /usr/src/
RUN gradle wrapper --gradle-version 8.0.2
RUN ./gradlew build 

FROM openjdk:11-ea-17-jre-slim
COPY --from=mbuilder /usr/src/build/libs/user-service-0.0.1-SNAPSHOT.jar /usr/src/
CMD ["java","-jar","/usr/src/user-service-0.0.1-SNAPSHOT.jar"] 
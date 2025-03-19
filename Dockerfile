#model
FROM eclipse-temurin:23.0.2_7-jdk-alpine

#container port detail
expose 8080

#container root dir
workdir /root

#imported
copy ./pom.xml /root
copy ./.mvn /root/.mvn
copy ./mvnw /root

#download dependencies
run ./mvnw dependency:go-offline

#source code
copy ./src /root/src

#build
run ./mvnw clean install

#run when container starts
#entrypoint ["java", "-jar", "/root/target/challenge-0.0.1-SNAPSHOT.jar"]
entrypoint ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-jar", "/root/target/challenge-0.0.1-SNAPSHOT.jar"]

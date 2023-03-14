FROM openjdk:20-ea-11
VOLUME /tmp
COPY build/libs/websocket-0.0.1-SNAPSHOT.jar comeon-websocket.jar
ENTRYPOINT ["java", "-jar", "comeon-websocket.jar"]

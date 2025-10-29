FROM gradle:8.12-jdk21

WORKDIR .

COPY . .

RUN ./gradlew build

CMD ["java", "-jar", "build/libs/app-0.0.1-SNAPSHOT.jar"]
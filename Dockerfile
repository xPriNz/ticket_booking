from ibm-semeru-runtimes:open-26-jdk as build-stage

workdir /build

copy src /build/src/
copy pom.xml /build
copy mvnw /build
copy .mvn /build/.mvn

run /build/mvnw clean compile package
cmd ["java", "-jar", "/build/target/booking-0.0.1-SNAPSHOT.jar"]



from ibm-semeru-runtimes:open-26-jdk as deploy-stage

workdir /deploy

copy --from=build-stage /build/target/booking-0.0.1-SNAPSHOT.jar ./
cmd ["java", "-jar", "./booking-0.0.1-SNAPSHOT.jar"]
expose 8080


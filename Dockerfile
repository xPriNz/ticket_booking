from ibm-semeru-runtimes:open-26-jdk as build-stage

workdir /build

# Build dependencies - cacheable
copy pom.xml /build
copy mvnw /build
copy .mvn /build/.mvn
run -- /build/mvnw dependency:go-offline

# Build package
copy src /build/src/
run -- /build/mvnw package

cmd ["java", "-jar", "/build/target/booking-0.0.1-SNAPSHOT.jar"]



from ibm-semeru-runtimes:open-26-jre as deploy-stage

workdir /deploy

copy --from=build-stage /build/target/booking-0.0.1-SNAPSHOT.jar ./
cmd ["java", "-jar", "./booking-0.0.1-SNAPSHOT.jar"]
expose 8080


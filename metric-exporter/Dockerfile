FROM eclipse-temurin:17-jre-noble

ADD ./target/*.jar /app/app.jar

CMD java -jar /app/app.jar --spring.profiles.active="production"
# Compila o WAR com JDK 21 e Maven.
FROM maven:3.9.16-eclipse-temurin-21-noble AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

# Executa o WAR no Tomcat 11 com Java 21.
FROM tomcat:11.0.27-jre21-temurin
ENV CATALINA_HOME=/usr/local/tomcat
RUN rm -rf "$CATALINA_HOME/webapps/"*
COPY --from=build /build/target/ACTA-1.0-SNAPSHOT.war "$CATALINA_HOME/webapps/ROOT.war"
COPY docker-entrypoint.sh /usr/local/bin/acta-entrypoint
RUN chmod +x /usr/local/bin/acta-entrypoint
EXPOSE 10000
ENTRYPOINT ["acta-entrypoint"]

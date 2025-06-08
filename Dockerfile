# Usar imagem base do Java
FROM eclipse-temurin:17-jre

# Diretório dentro do container
WORKDIR /app

# Copiar o jar gerado para dentro do container
COPY target/cooperativa-voting-0.0.1-SNAPSHOT.jar app.jar

# Expor a porta que o Spring Boot roda (ajuste se for diferente)
EXPOSE 8080

# Comando para rodar o jar
ENTRYPOINT ["java", "-jar", "app.jar"]
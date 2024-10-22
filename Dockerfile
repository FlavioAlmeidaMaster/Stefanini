# Etapa 1: Compilação do código
FROM maven:3.8.5-openjdk-17-slim AS build
WORKDIR /app

# Copia o pom.xml e baixa as dependências
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia o código fonte e compila o projeto
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Criação da imagem final para execução
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copia o JAR gerado na etapa de build
COPY --from=build /app/target/Teste-0.0.1-SNAPSHOT.jar app.jar

# Exponha a porta que a aplicação usa
EXPOSE 8080

# Comando para rodar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]

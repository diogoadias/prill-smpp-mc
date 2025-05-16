# Etapa 1: Construção
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app
LABEL name="smpp-mc"

# Instalação do Maven
RUN apt-get update && apt-get install -y maven iputils-ping netcat curl

# Copia o arquivo pom.xml e baixa as dependências
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o código-fonte do projeto
COPY src ./src

# Compila o projeto
RUN mvn clean package -DskipTests --batch-mode

# Etapa 2: Execução
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
LABEL name="smpp-mc"

# Copia o JAR gerado para o contêiner final
COPY --from=build /app/target/*.jar app.jar

# Expõe a porta do aplicativo
EXPOSE 2775

# Define o comando de entrada
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-jar", "app.jar"]
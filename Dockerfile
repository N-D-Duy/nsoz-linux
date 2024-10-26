# Bước 1: Sử dụng Maven để build ứng dụng
FROM maven:3.8.5-openjdk-17 AS build

# Thiết lập thư mục làm việc
WORKDIR /app

# Sao chép pom.xml và mã nguồn vào container
COPY . .

# Build ứng dụng với Maven
RUN mvn clean package

# Bước 2: Chạy ứng dụng Java
FROM openjdk:17-jdk-slim

# Thiết lập thư mục làm việc
WORKDIR /app

# Sao chép file JAR đã được build từ bước trước
COPY --from=build /app/target/Nso-jar-with-dependencies.jar app.jar

# Chạy ứng dụng
ENTRYPOINT ["java", "-jar", "/app/target/Nso-jar-with-dependencies.jar"]

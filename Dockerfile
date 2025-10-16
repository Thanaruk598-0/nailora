# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# ก็อปไฟล์ pom ก่อน เพื่อ cache dependency
COPY pom.xml .
RUN mvn -q -e -DskipTests dependency:go-offline

# ก็อปโค้ดที่เหลือ แล้ว build jar
COPY src ./src
RUN mvn -q -DskipTests package

# ---------- Run stage ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# ก็อปไฟล์ jar จากสเตจ build มาเป็น app.jar
COPY --from=build /app/target/*.jar /app/app.jar

# Render จะส่งพอร์ตผ่านตัวแปร $PORT
# เราบังคับ Spring Boot ให้ใช้พอร์ตนี้ (กันพลาดแม้ลืมตั้งใน properties)
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -jar /app/app.jar"]

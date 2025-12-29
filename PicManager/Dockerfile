# 构建阶段
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# 复制pom.xml并下载依赖
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源代码并构建
COPY src ./src
RUN mvn clean package -DskipTests

# 运行阶段
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# 从构建阶段复制jar文件
COPY --from=build /app/target/PicManager-*.jar app.jar

# 暴露端口
EXPOSE 8088

# 设置JVM参数
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]


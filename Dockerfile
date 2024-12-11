FROM openjdk:25-slim

WORKDIR /app
COPY target/myapp.jar /app/myapp.jar
EXPOSE 9090

# 使用环境变量传递 JVM 参数
ENV JAVA_OPTS=""

# 启动应用时动态传递环境变量
CMD ["sh", "-c", "java $JAVA_OPTS -jar myapp.jar"]
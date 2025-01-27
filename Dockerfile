FROM openjdk:25-slim

WORKDIR /app

# 设置时区为东八区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

COPY target/HumanVerificationSystem-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 9090

# 使用环境变量传递 JVM 参数
ENV JAVA_OPTS=""

# 启动应用时动态传递环境变量
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
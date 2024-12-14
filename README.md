```shell
docker run -d --name hvs -p 50001:9090 -v /root/hvs/logs:/app/logs -e JAVA_OPTS="-Xmx128m -Xms128m" -e REDIRECT_URL="http://www.baidu.com" --memory="320m"  jeckchen/human-verification-system
```
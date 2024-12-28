# HumanVerificationSystem —— 人机校验门户

一个用于部署在公网的人机检测页。
众所周期，公网IP地址会不断有人试图访问，本系统会记录下访问者IP地址，并记录下访问者的IP、访问时间以及访问设备的UserAgent。

```shell
docker run -d --name hvs -p 50001:9090 -v /root/hvs/logs:/app/logs -v /root/hvs/sqlite.db:/app/sqlite.db -e JAVA_OPTS="-Xmx128m -Xms128m" -e REDIRECT_URL="http://www.baidu.com" --memory="320m" --restart=always jeckchen/human-verification-system
```
![](https://raw.githubusercontent.com/JeckChen666/Jc-Private-Repository/main/image/202420241228200754.png)
![](https://raw.githubusercontent.com/JeckChen666/Jc-Private-Repository/main/image/202420241228200630.png)
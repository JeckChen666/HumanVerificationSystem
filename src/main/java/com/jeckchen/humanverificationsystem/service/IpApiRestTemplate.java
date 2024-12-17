package com.jeckchen.humanverificationsystem.service;

import com.dtflys.forest.annotation.Get;
import com.dtflys.forest.annotation.Var;
import com.jeckchen.humanverificationsystem.pojo.IpApiResp;

public interface IpApiRestTemplate {
    @Get(
            url = "https://ipapi.co/{ip}/json/",
            dataType = "json",
            headers = {
                    "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36 Edg/131.0.0.0",
                    "Content-Type: text/plain",
                    "Accept: application/json"
            }
    )
    IpApiResp getLocationOfIp(@Var("ip") String ip);
}

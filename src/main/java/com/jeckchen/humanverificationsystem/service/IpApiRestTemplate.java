package com.jeckchen.humanverificationsystem.service;

import com.dtflys.forest.annotation.Get;
import com.dtflys.forest.annotation.Var;
import com.jeckchen.humanverificationsystem.pojo.IpApiResp;

public interface IpApiRestTemplate {
    @Get(
            url = "https://ipapi.co/{ip}/json/",
            dataType = "json"
    )
    IpApiResp getLocationOfIp(@Var("ip") String ip);
}

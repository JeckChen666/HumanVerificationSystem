package com.jeckchen.humanverificationsystem.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class IpApiResp {
    private String ip;
    private String version;
    private String city;
    private String region;
    private String region_code;
    private String country_code;
    private String country_code_iso3;
    private String country_name;
    private String country_capital;
    private String country_tld;
    private String continent_code;
    private Boolean in_eu;
    private String postal;
    private Double latitude;
    private Double longitude;
    private String timezone;
    private String utc_offset;
    private String country_calling_code;
    private String currency;
    private String currency_name;
    private String languages;
    private Long country_area;
    private Long country_population;
    private String asn;
    private String org;
    private String hostname;

    public static IpApiResp getUnknown() {
        return new IpApiResp()
                .setIp("unknown")
                .setVersion("unknown")
                .setCity("unknown")
                .setRegion("unknown")
                .setRegion_code("unknown")
                .setCountry_code("unknown")
                .setCountry_code_iso3("unknown")
                .setCountry_name("unknown")
                .setCountry_capital("unknown")
                .setCountry_tld("unknown")
                .setContinent_code("unknown")
                .setIn_eu(false)
                .setPostal("unknown")
                .setLatitude(0.0)
                .setLongitude(0.0)
                .setTimezone("unknown")
                .setUtc_offset("unknown")
                .setCountry_calling_code("unknown")
                .setCurrency("unknown")
                .setCurrency_name("unknown")
                .setLanguages("unknown")
                .setCountry_area(0L)
                .setCountry_population(0L)
                .setAsn("unknown")
                .setOrg("unknown")
                .setHostname("unknown");
    }
}

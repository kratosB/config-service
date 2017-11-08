package com.iqunxing.config.enums;

/**
 * Created on 2017/10/18.
 * @author zhiqiang bao
 */
public enum EurekaAddress {

    /**
     * 各个环境注册中心地址
     */
    test("http://${ENV}.dcfservice.com:23001/eureka"),
    uat("http://uat01-2.dcfservice.com:23001/eureka,http://uat02-2.dcfservice.com:23001/eureka"),
    hotfix("http://hotfix01-2.dcfservice.com:23001/eureka"),
    demo("http://bs-demo-app01.dcfservice.com:23001/eureka,http://bs-demo-app02.dcfservice.com:23001/eureka"),
    prd("${EUREKA_ZONE}");

    EurekaAddress(String address){
        this.address = address;
    }

    private String address;

    public String getValue(){
        return address;
    }

}

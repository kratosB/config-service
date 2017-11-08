package com.iqunxing.config;

import com.iqunxing.config.enums.EurekaAddress;
import org.apache.commons.lang.StringUtils;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.config.server.EnableConfigServer;


/**
 * Created on 2017/10/18.
 *
 * @author zhiqiang bao
 */
@SpringBootApplication
@EnableConfigServer
@EnableDiscoveryClient
@RestController
public class ConfigServiceApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(ConfigServiceApplication.class).profiles(getEnv());
    }

    private static String getEnv() {
        String env = System.getenv("ENV");
        if (StringUtils.equalsIgnoreCase(env, EurekaAddress.prd.name())) {
            System.setProperty("eureka.client.serviceUrl.defaultZone", EurekaAddress.prd.getValue());
            return env;
        } else if (StringUtils.equalsIgnoreCase(env, EurekaAddress.demo.name())) {
            System.setProperty("eureka.client.serviceUrl.defaultZone", EurekaAddress.demo.getValue());
        } else if (StringUtils.equalsIgnoreCase(env, EurekaAddress.uat.name())) {
            System.setProperty("eureka.client.serviceUrl.defaultZone", EurekaAddress.uat.getValue());
        } else if (StringUtils.equalsIgnoreCase(env, EurekaAddress.hotfix.name())) {
            System.setProperty("eureka.client.serviceUrl.defaultZone", EurekaAddress.hotfix.getValue());
        } else {
            System.setProperty("eureka.client.serviceUrl.defaultZone", EurekaAddress.test.getValue());
        }
        return "dev";
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder(ConfigServiceApplication.class).profiles(getEnv()).run(args);
    }

}

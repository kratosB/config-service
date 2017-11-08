package com.iqunxing.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iqunxing.config.enums.EurekaAddress;
import com.iqunxing.config.util.JsonMapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.cloud.config.server.environment.MultipleJGitEnvironmentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Autowired
    private MultipleJGitEnvironmentRepository multipleJGitEnvironmentRepository;

    @PostMapping("/getDetail")
    public String get(@RequestParam(name = "serviceName", required = false) String serviceName, @RequestParam(name = "profile", required = false) String profile) {
        List<String> serviceNames;
        if (StringUtils.isBlank(serviceName)) {
            File dir = multipleJGitEnvironmentRepository.getBasedir();
            File[] files = dir.listFiles();
            serviceNames = Arrays.stream(files).filter(file -> file.getName().contains("service")
                    || file.getName().contains("rest") || file.getName().contains("dashBoard")).map(File::getName)
                    .collect(Collectors.toList());
        } else {
            serviceNames = new ArrayList<>();
            serviceNames.add(serviceName);
        }
//        serviceNames.
//        HashMap<String, String> testMap = getProps("http://127.0.0.1:23006/" + serviceName + "/test");
//        HashMap<String, String> uatMap = getProps("http://127.0.0.1:23006/" + serviceName + "/uat");
//        HashMap<String, String> hotFixMap = getProps("http://127.0.0.1:23006/" + serviceName + "/hotfix");
//        List<HashMap<String, String>> maps = new ArrayList<>();
//        maps.add(testMap);
//        maps.add(uatMap);
//        maps.add(hotFixMap);
//        List<String> keys = getDifference(maps);
//        HashMap<String, Object> returnMap = new HashMap<>();
//        returnMap = collect(serviceName, "test", keys, testMap, returnMap);
//        returnMap = collect(serviceName, "uat", keys, uatMap, returnMap);
//        returnMap = collect(serviceName, "hotfix", keys, hotFixMap, returnMap);
        return null;
//        return JsonMapper.nonDefaultMapper().toJson(returnMap);
    }

    private HashMap<String, String> getProps(String url) {
        RestTemplate restTemplate = new RestTemplate();
        HashMap map = null;
        try {
            ResponseEntity<String> result = restTemplate.getForEntity(url, String.class);
            if (result.getStatusCode().value() == 200) {
                JsonNode jsonObject = JsonMapper.nonDefaultMapper().fromJson(result.getBody(), JsonNode.class);
                JsonNode sources = jsonObject.findValue("source");
                ObjectMapper mapper = new ObjectMapper();
                map = mapper.convertValue(sources, HashMap.class);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return map;
    }

    private List<String> getDifference(List<HashMap<String, String>> maps) {
        Set<String> keys = new HashSet<>();
        maps.forEach(map -> {
            keys.addAll(map.keySet());
        });
        return keys.stream().filter(key -> {
            boolean matching = true;
            String value = null;
            // 如果maps中有一个map不包含这个key，则这一项会被过滤出来
            for (HashMap<String, String> map : maps) {
                if (!map.containsKey(key)) {
                    matching = false;
                }
            }
            // 如果所有map都包括这个key，则根据value继续筛选，value不同的情况会被帅选出来
            if (matching) {
                for (HashMap<String, String> map : maps) {
                    String value1 = String.valueOf(map.get(key));
                    if (StringUtils.isBlank(value)) {
                        value = value1;
                    } else if (!StringUtils.equals(value, value1)) {
                        matching = false;
                        break;
                    }
                }
            }
            return !matching;
        }).collect(Collectors.toList());
    }

    private HashMap<String, Object> collect(String serviceName, String env, List<String> keys, HashMap<String, String> props, HashMap<String, Object> result) {
        HashMap<String, Object> envMap;
        HashMap<String, String> propMap;
        if (result.containsKey(serviceName)) {
            envMap = (HashMap<String, Object>) result.get(serviceName);
        } else {
            envMap = new HashMap<>();
            result.put(serviceName, envMap);
        }
        if (envMap.containsKey(env)) {
            propMap = (HashMap<String, String>) envMap.get(env);
        } else {
            propMap = new HashMap<>();
            envMap.put(env, propMap);
        }
        keys.forEach(key -> {
            propMap.put(key, props.get(key));
        });
        return result;
    }

}

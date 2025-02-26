package cn.kiko.netmonitoranalysissystemcollector.config;

import com.alibaba.nacos.api.naming.pojo.Instance;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "nacos.discovery")
@Data
public class NacosServerDiscoveryConfig {
    private String ip;
    private Integer port;
    private String serverAddr;
    private String serviceName;
    private String namespace;
    private String groupName;

    public Instance getInstance() {
        Instance instance = new Instance();
        instance.setEnabled(true);
        instance.setIp(ip);
        instance.setPort(port);
        instance.setServiceName(serviceName);
        instance.setHealthy(true);
        return instance;
    }
}

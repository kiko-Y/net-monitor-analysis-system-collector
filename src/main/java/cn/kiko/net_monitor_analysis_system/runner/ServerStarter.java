package cn.kiko.net_monitor_analysis_system.runner;

import cn.kiko.net_monitor_analysis_system.collector.MonitorDataCollector;
import cn.kiko.net_monitor_analysis_system.config.NacosServerDiscoveryConfig;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
public class ServerStarter implements CommandLineRunner {
    private final MonitorDataCollector collector;
    private final NacosServerDiscoveryConfig nacosServerDiscoveryConfig;

    private static final Logger logger = LoggerFactory.getLogger(ServerStarter.class);
    @Autowired
    public ServerStarter(MonitorDataCollector collector, NacosServerDiscoveryConfig nacosServerDiscoveryConfig) {
        this.collector = collector;
        this.nacosServerDiscoveryConfig = nacosServerDiscoveryConfig;
    }


    @PostConstruct
    public void serverRegister() {
        NamingService namingService;
        try {
            namingService = NamingFactory.createNamingService(nacosServerDiscoveryConfig.getServerAddr());
        } catch (NacosException e) {
            logger.error("naming service fetch error: {}, serverAddr: {}", e, nacosServerDiscoveryConfig.getServerAddr());
            return;
        }
        try {
            namingService.registerInstance(
                    nacosServerDiscoveryConfig.getServiceName(),
                    nacosServerDiscoveryConfig.getGroupName(),
                    nacosServerDiscoveryConfig.getInstance()
            );
        } catch (NacosException e) {
            logger.error("register service error: {}", e.toString());
        }
        logger.info("successfully register service, instance: {}", nacosServerDiscoveryConfig.getInstance());
    }
    @PreDestroy
    public void deregisterService() {
        NamingService namingService;
        try {
            namingService = NamingFactory.createNamingService(nacosServerDiscoveryConfig.getServerAddr());
        } catch (NacosException e) {
            logger.error("naming service fetch error when deregister service: {}, server: {}", e, nacosServerDiscoveryConfig.getServerAddr());
            throw new RuntimeException(e);
        }
        try {
            namingService.deregisterInstance(nacosServerDiscoveryConfig.getServiceName(), nacosServerDiscoveryConfig.getInstance());
        } catch (NacosException e) {
            logger.error("deregister service fetch error: {}", e.toString());
        }

    }
    @Override
    public void run(String... args) {
        collector.startCollectorServer();
    }
}

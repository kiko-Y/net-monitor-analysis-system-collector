package cn.kiko.netmonitoranalysissystemcollector.runner;

import cn.kiko.netmonitoranalysissystemcollector.collector.MonitorDataCollector;
import cn.kiko.netmonitoranalysissystemcollector.config.NacosServerDiscoveryConfig;
import cn.kiko.switch_sdk.algo.IFlowKey;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

public class ServerStarter<Key extends IFlowKey> implements CommandLineRunner {

    @Autowired
    private MonitorDataCollector<Key> monitorDataCollector;

    @Autowired
    private NacosServerDiscoveryConfig nacosServerDiscoveryConfig;

    private static final Logger logger = LoggerFactory.getLogger(ServerStarter.class);

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
        monitorDataCollector.startCollectorServer();
    }
}

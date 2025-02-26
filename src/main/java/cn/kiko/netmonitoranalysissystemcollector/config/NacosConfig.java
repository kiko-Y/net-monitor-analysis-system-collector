package cn.kiko.netmonitoranalysissystemcollector.config;


import com.alibaba.nacos.api.annotation.NacosProperties;
import com.alibaba.nacos.spring.context.annotation.config.EnableNacosConfig;
import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableNacosConfig(globalProperties =
@NacosProperties(
        serverAddr = "162.105.146.22:8848",
        namespace = "769cc9ea-bddb-4e3c-8ec2-5790b73376ed"
))
@NacosPropertySource(dataId = "net_monitor_analysis_system.yaml", groupId = "dev", autoRefreshed = true)
public class NacosConfig {
}

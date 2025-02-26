package cn.kiko.netmonitoranalysissystemcollector.nacos;

import cn.kiko.netmonitoranalysissystemcollector.runner.ServerStarter;
import com.alibaba.nacos.api.config.annotation.NacosValue;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
public class NacosTest {

    // 不要实例化这个 bean，否则会启动服务阻塞测试
    @MockBean
    private ServerStarter starter;

    @NacosValue(value = "${switch.count}", autoRefreshed = true)
    private String switchCount;

    @Test
    public void nacosSimpleTest() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            System.out.println(switchCount);
            Thread.sleep(2000);
        }
    }
}

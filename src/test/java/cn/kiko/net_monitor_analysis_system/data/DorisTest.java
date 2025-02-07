package cn.kiko.net_monitor_analysis_system.data;

import cn.kiko.net_monitor_analysis_system.runner.ServerStarter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

@SpringBootTest
public class DorisTest {
    // 不要实例化这个 bean，否则会启动服务阻塞测试
    @MockBean
    private ServerStarter starter;

    @Autowired
    @Qualifier("dorisTemplate")
    private JdbcTemplate dorisTemplate;

    @Test
    public void dorisTest() {
        String sql = "SELECT * FROM `array_2d_test`";
        List<Map<String, Object>> maps = dorisTemplate.queryForList(sql);
        // 是 String，需要转回去，mysql 不支持 array
        Object array2d = maps.get(0).get("c_array_2d");
        System.out.println(array2d.getClass().getName());
    }
}

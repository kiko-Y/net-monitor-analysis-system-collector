package cn.kiko.netmonitoranalysissystemcollector.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class DorisDataSourceConfig {
    private static final Logger logger = LoggerFactory.getLogger(DorisDataSourceConfig.class);
    @Bean(name = "dorisDataSource")
    @ConfigurationProperties(prefix="datasource.doris")
    public DataSource prestoDataSource() {
        return DataSourceBuilder.create().build();
    }
    @Bean(name = "dorisTemplate")
    public JdbcTemplate dorisJdbcTemplate(@Qualifier("dorisDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}

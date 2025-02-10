流量数据集文件需要放到 resources 下才能读取
130000.dat


## Nacos

nacos 的依赖用这个：
```xml
<!-- https://mvnrepository.com/artifact/com.alibaba.nacos/nacos-spring-context -->
<dependency>
    <groupId>com.alibaba.nacos</groupId>
    <artifactId>nacos-spring-context</artifactId>
    <version>2.1.1</version>
</dependency>
```
其他可能会和 springboot3.x 有依赖冲突

用法可以看这个: https://nacos.io/blog/faq/nacos-user-question-history15797/
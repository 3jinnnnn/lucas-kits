# lucas-orm-redis
## 1. 简介
使用Jedis方式连接Redis工具类, 支持Shard模式, 集群模式暂未适配

## 2. 配置
### 2.1 实际开发工程的pom.xml中引入当前工具
``` xml
<dependency>
    <groupId>me.lucas</groupId>
    <artifactId>lucas-orm-redis</artifactId>
    <version>1.2.0-SNAPSHOT</version>
</dependency>
```

### 2.2 配置Spring Bean
```xml
<bean id="redisClientConfig" class="me.lucas.kits.orm.redis.jedis.config.RedisClientConfig" />
```

### 2.3 新增Redis配置文件
文件名: redis.properties  
位置: resources根目录
```properties
# 根据以下的redis名称进行匹配, 使用','分隔
redis.root=main,other

# main redis
redis.main.redisType=main
# 若为Sharding模式, 则用';'分隔
redis.main.hostNames=127.0.0.1:6379
redis.main.maxTotal=100
redis.main.maxIdle=10
redis.main.timeOut=5000
redis.main.testOnBorrow=false
redis.main.expireTime=3600

# other redis
redis.other.redisType=other
redis.other.hostNames=127.0.0.1:6379
redis.other.maxTotal=100
redis.other.maxIdle=10
redis.other.timeOut=5000
redis.other.testOnBorrow=false
redis.other.expireTime=3600

# 哨兵模式
// 该配置项为masterName
redis.sentinel.redisType=masterName
redis.sentinel.hostNames=127.0.0.1:6379;127.0.0.1:6380;127.0.0.1:6381
redis.sentinel.maxTotal=20
redis.sentinel.maxIdle=5
redis.sentinel.timeOut=2000
redis.sentinel.testOnBorrow=true
redis.sentinel.expireTime=3600
// 该配置项声明其为哨兵模式
redis.sentinel.sentinel=true
```

## 3. 使用
在需要的地方注入即可, 注意要加前缀"reids:"
```java
@Autowired
@Qualifier("redis:main")
private RedisClient redis;
```
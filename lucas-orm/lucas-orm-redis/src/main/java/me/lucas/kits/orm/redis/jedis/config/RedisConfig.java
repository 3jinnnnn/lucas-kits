/*
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.lucas.kits.orm.redis.jedis.config;

import java.util.Properties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.lucas.kits.commons.entity.BaseEntity;
import me.lucas.kits.commons.loader.PropertiesLoader;
import me.lucas.kits.commons.utils.StringUtils;
import me.lucas.kits.orm.redis.jedis.exception.NotFoundExtendException;

/**
 * RedisClient的连接配置.
 *
 * @author yanghe
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedisConfig extends BaseEntity {

    private static final long serialVersionUID = -6957473106901833919L;

    public static final String SEPARATOR = ".";
    public static final String REDIS = "redis.";
    public static final String ROOT = "redis.root";
    public static final String REDIS_TYPE = "redisType";
    public static final String HOST_NAMES = "hostNames";
    public static final String MAX_TOTAL = "maxTotal";
    public static final String MAX_IDLE = "maxIdle";
    public static final String MIN_IDLE = "minIdle";
    public static final String TIME_OUT = "timeOut";
    public static final String TEST_ON_BORROW = "testOnBorrow";
    public static final String EXPIRE_TIME = "expireTime";
    public static final String EXTEND = "extend";
    public static final String EXTEND_RESOURCE = "extendResource";
    public static final String EXTEND_PROPERTIES = "extendProperties";
    public static final String CLUSTER = "cluster";
    public static final String MAX_REDIRECTIONS = "maxRedirections";
    public static final String LOCK_TIMEOUT = "lockTimeout";
    public static final String LOCK_HASH = "lockGroup";

    private String redisType;
    private String hostNames;
    private Integer maxTotal;
    private Integer maxIdle;
    private Integer minIdle;
    private Integer timeOut;
    private Boolean testOnBorrow;
    private Integer expireTime;

    // Jedis Sentinel模式.
    private Boolean sentinel;
    // Jedis Sentinel模式 Mater Name.
    private String sentinelMasterName;

    /**
     * RedisClient扩展.
     *
     * @since 1.3.10
     */
    private String extend;

    /**
     * RedisClient扩展属性路径.
     *
     * @since 1.3.10
     */
    private String extendResource;

    /**
     * RedisClient扩展属性.
     *
     * @since 1.3.10
     */
    private Properties extendProperties;

    /**
     * JedisCluster模式.
     *
     * @since 1.3.12
     */
    private Boolean cluster;

    /**
     * @since 1.3.12
     */
    private Integer maxRedirections;

    /**
     * @since 1.4.9
     */
    private Integer lockTimeout;

    /**
     * @since 1.4.9
     */
    private String lockGroup;

    public static final RedisConfig newInstance() {
        return new RedisConfig();
    }

    public void setExtend(final String extend) {
        if (StringUtils.isNotBlank(extend)) {
            try {
                Class.forName(extend);
            } catch (final ClassNotFoundException e) {
                throw new NotFoundExtendException(e.getMessage(), e);
            }
        }

        this.extend = extend;
    }

    public void setExtendResource(final String extendResource) {
        this.extendResource = extendResource;
        if (StringUtils.isNotBlank(extendResource)) {
            Properties extendProperties = PropertiesLoader.PROPERTIES.get(extendResource);
            if (extendProperties == null) {
                extendProperties = PropertiesLoader.load(extendResource);
            }

            if (extendProperties != null) {
                setExtendProperties(extendProperties);
            }
        }
    }
}

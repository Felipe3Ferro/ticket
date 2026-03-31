package br.ferro.ticket.catalog.infra.config;

import java.time.Duration;
import java.util.Map;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class CacheConfig {

  private RedisCacheConfiguration defaultConfig() {
    return RedisCacheConfiguration.defaultCacheConfig()
        .serializeKeysWith(
            RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
        .serializeValuesWith(
            RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.json()))
        .disableCachingNullValues();
  }

  @Bean
  public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
    return RedisCacheManager.builder(connectionFactory)
        .cacheDefaults(defaultConfig().entryTtl(Duration.ofMinutes(10)))
        .withInitialCacheConfigurations(
            Map.of(
                CacheConstants.CACHE_EVENTOS,
                    defaultConfig().entryTtl(Duration.ofMinutes(5)),
                CacheConstants.CACHE_EVENTO,
                    defaultConfig().entryTtl(Duration.ofMinutes(10)),
                CacheConstants.CACHE_TIPOS_INGRESSO,
                    defaultConfig().entryTtl(Duration.ofMinutes(5))))
        .build();
  }
}
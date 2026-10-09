package com.bluesky.pos_system.configuration;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String CACHE_CATEGORIES = "categories";
    public static final String CACHE_CATEGORY_TREES = "categoryTrees";
    public static final String CACHE_PRODUCTS = "products";
    public static final String CACHE_STORES = "stores";
    public static final String CACHE_PROMOTIONS = "promotions";

    @Bean
    @Primary
    @Profile("!test")
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        objectMapper.activateDefaultTyping(
                com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator.instance,
                com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping.NON_FINAL,
                com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY
        );
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer));

        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put(CACHE_CATEGORIES, defaultConfig.entryTtl(Duration.ofHours(2)));
        cacheConfigs.put(CACHE_CATEGORY_TREES, defaultConfig.entryTtl(Duration.ofHours(2)));
        cacheConfigs.put(CACHE_PRODUCTS, defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigs.put(CACHE_STORES, defaultConfig.entryTtl(Duration.ofHours(4)));
        cacheConfigs.put(CACHE_PROMOTIONS, defaultConfig.entryTtl(Duration.ofMinutes(30)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }

    @Bean
    @Profile("test")
    public CacheManager testCacheManager() {
        return new ConcurrentMapCacheManager(
                CACHE_CATEGORIES, CACHE_CATEGORY_TREES, CACHE_PRODUCTS, CACHE_STORES, CACHE_PROMOTIONS
        );
    }
}

package com.example.library.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;


@Configuration
@EnableCaching
public class RedisConfig {


    @Bean
    public RedisCacheManager cacheManager(
            RedisConnectionFactory connectionFactory) {

        RedisCacheConfiguration defaultConfig =
                baseConfig(Duration.ofMinutes(10));


        Map<String, RedisCacheConfiguration> cacheConfigurations =
                new HashMap<>();


        cacheConfigurations.put(
                "books",
                baseConfig(Duration.ofMinutes(10))
        );


        cacheConfigurations.put(
                "members",
                baseConfig(Duration.ofMinutes(5))
        );


        cacheConfigurations.put(
                "loans",
                baseConfig(Duration.ofMinutes(60))
        );


        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }



    private RedisCacheConfiguration baseConfig(Duration ttl) {


        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.registerModule(new JavaTimeModule());

        objectMapper.disable(
                SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
        );


        GenericJacksonJsonRedisSerializer serializer =
                GenericJacksonJsonRedisSerializer.builder()
                        .build();



        return RedisCacheConfiguration
                .defaultCacheConfig()

                .entryTtl(ttl)

                .disableCachingNullValues()


                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(
                                        new StringRedisSerializer()
                                )
                )


                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(serializer)
                );
    }
}
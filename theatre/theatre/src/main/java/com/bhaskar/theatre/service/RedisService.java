package com.bhaskar.theatre.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    private final RedisTemplate<String ,Object> redisTemplate;


    public RedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void set(String key, Object value, Long timeoutInMinutes) {
            redisTemplate.opsForValue().set(key, value, timeoutInMinutes, TimeUnit.MINUTES);
    }

    // generic type for all type of class objects
    public <T> T get(String key, Class<T> entityClass) {
        try{
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) return null;
            return entityClass.cast(value);
        } catch (Exception e) {
            return null;
        }
    }
    public void deleteByPattern(String pattern) {
        // This finds all keys starting with "movies:all:"
        Set<String> keys = redisTemplate.keys(pattern + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    //for update and delete
    public void delete(String key) {
        redisTemplate.delete(key);
    }
}

package org.reddot15.be_stockmanager.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Transactional
@Service
public class RedisAuthService {
    RedisTemplate<String, Long> redisLongTemplate;

    // Save
    public void saveInvalidatedTokenExpirationKey(String invalidatedToken, Long timeExpiration) {
        redisLongTemplate.opsForValue().set(invalidatedToken, timeExpiration, timeExpiration, TimeUnit.MILLISECONDS);
    }

    // Get
    public Long getInvalidatedTokenExpirationKey(String invalidatedToken) {
        return redisLongTemplate.opsForValue().get(invalidatedToken);
    }
}

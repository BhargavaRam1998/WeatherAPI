package com.weatherAPI.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@Service
public class WeatherService {

    @Value("${weather.api.url}")
    private String apiUrl;

    @Value("${weather.api.key}")
    private String apiKey;

    private final StringRedisTemplate redisTemplate;

    private final RestTemplate restTemplate;

    public WeatherService(StringRedisTemplate redisTemplate, RestTemplate restTemplate) {
        this.redisTemplate = redisTemplate;
        this.restTemplate = restTemplate;
    }


    public String getWeather(String city) {

        if (city != null) {
            String cacheKey = "weather:" + city;

            String cachedWeather = redisTemplate.opsForValue().get(cacheKey);

            if (cachedWeather != null) {
                return cachedWeather;
            }

            String url = apiUrl + city + "?key=" + apiKey;

            try {
                String response = restTemplate.getForObject(url, String.class);
                redisTemplate.opsForValue().set(cacheKey, response, 12, TimeUnit.HOURS);
                return response;
            } catch (Exception e) {
                throw new RuntimeException("Failed to fetch weather data: " + e.getMessage());
            }
        } else {
            return "Enter proper city name";
        }
    }
}

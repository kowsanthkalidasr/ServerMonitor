package com.sermon.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        String idForEncode = "argon2";

        Map<String, PasswordEncoder> encoderMap = new HashMap<>();

        encoderMap.put("argon2", Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8());
        encoderMap.put("bcrypt", new BCryptPasswordEncoder());

        return new DelegatingPasswordEncoder(idForEncode,encoderMap);
    }
}

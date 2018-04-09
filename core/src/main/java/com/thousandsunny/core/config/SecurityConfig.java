package com.thousandsunny.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.dao.ReflectionSaltSource;
import org.springframework.security.authentication.encoding.BasePasswordEncoder;
import org.springframework.security.authentication.encoding.PlaintextPasswordEncoder;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * Created by guitarist on 5/5/16.
 * 青春气贯长虹
 * 勇锐盖过怯弱
 * 进取压倒苟安
 * 年岁有加,并非垂老,理想丢弃,方堕暮年
 */
@Configuration
public class SecurityConfig {

    public static ShaPasswordEncoder shaPasswordEncoder() {
        ShaPasswordEncoder shaPasswordEncoder = new ShaPasswordEncoder(512);
        shaPasswordEncoder.setEncodeHashAsBase64(true);
        shaPasswordEncoder.setIterations(10);
        return shaPasswordEncoder;
    }

    public static String encodePassword(String rawPassword, Object salt) {
        return shaPasswordEncoder().encodePassword(rawPassword, salt);
    }

    public static PlaintextPasswordEncoder plaintextPasswordEncoder() {
        return new PlaintextPasswordEncoder();
    }

    public static DaoAuthenticationProvider daoAuthenticationProviderInstance(UserDetailsService userDetailsService, BasePasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        daoAuthenticationProvider.setSaltSource(reflectionSaltSource());
        daoAuthenticationProvider.setHideUserNotFoundExceptions(false);
        return daoAuthenticationProvider;
    }

    private static ReflectionSaltSource reflectionSaltSource() {
        ReflectionSaltSource reflectionSaltSource = new ReflectionSaltSource();
        reflectionSaltSource.setUserPropertyToUse("getSalt");
        return reflectionSaltSource;
    }

    /**
     * 打开所有的跨域
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedMethods(GET.name(), POST.name(), PUT.name(), DELETE.name(), PATCH.name(), OPTIONS.name());
            }
        };
    }
}

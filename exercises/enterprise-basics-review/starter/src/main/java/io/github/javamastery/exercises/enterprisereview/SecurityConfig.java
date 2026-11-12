package io.github.javamastery.exercises.enterprisereview;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        throw new UnsupportedOperationException("练习：配置公开、学生、管理员边界和默认拒绝，保留登录与 CSRF");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public UserDetailsService users(PasswordEncoder encoder,
                                   @Value("${learning.security.student-password}") String studentPassword,
                                   @Value("${learning.security.admin-password}") String adminPassword) {
        if (studentPassword.isBlank() || adminPassword.isBlank()) {
            throw new IllegalArgumentException("演示账户密码不能为空");
        }
        return new InMemoryUserDetailsManager(
                User.withUsername("student").password(encoder.encode(studentPassword)).roles("STUDENT").build(),
                User.withUsername("admin").password(encoder.encode(adminPassword)).roles("ADMIN", "STUDENT").build());
    }
}

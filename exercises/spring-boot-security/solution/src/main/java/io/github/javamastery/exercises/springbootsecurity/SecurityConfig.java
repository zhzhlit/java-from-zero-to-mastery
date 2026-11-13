package io.github.javamastery.exercises.springbootsecurity;

import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
        return http.authorizeHttpRequests(authorize -> authorize
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(HttpMethod.GET, "/courses", "/csrf").permitAll()
                        .requestMatchers(HttpMethod.GET, "/profile").authenticated()
                        .requestMatchers(HttpMethod.GET, "/admin/summary").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/progress").hasRole("STUDENT")
                        .requestMatchers(HttpMethod.POST, "/progress").hasRole("STUDENT")
                        .anyRequest().denyAll())
                .formLogin(form -> form.defaultSuccessUrl("/profile", true).permitAll())
                .logout(logout -> logout.logoutSuccessUrl("/courses"))
                .build();
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

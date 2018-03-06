package com.example.issuesdashboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter{

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public InMemoryUserDetailsManager inMemoryUserDetailsManager() {
        return new InMemoryUserDetailsManager(
            User.withUsername("user").password(passwordEncoder.encode("password")).authorities("ROLE_USER").build(),
            User.withUsername("admin").password(passwordEncoder.encode("password")).authorities("ROLE_ADMIN").build());

    /*
      User.withDefaultPasswordEncoder is deprecated, list here as a reference.
     */
//                User.withDefaultPasswordEncoder().username("user").password("password")
//                        .authorities("ROLE_USER").build(),
//                User.withDefaultPasswordEncoder().username("admin").password("admin")
//                        .authorities("ROLE_ACTUATOR", "ROLE_ADMIN", "ROLE_USER").build());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/admin").hasRole("ADMIN")
                .requestMatchers(EndpointRequest.to("info", "health")).permitAll()
                .requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole("ACTUATOR")
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                .antMatchers("/events/**").hasRole("USER")
                .antMatchers("/**").permitAll()
                .and().httpBasic();
    }
}

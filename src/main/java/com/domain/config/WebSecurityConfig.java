package com.domain.config;

import java.security.SecureRandom;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder.BCryptVersion;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.domain.security.JWTAuthorizationFilter;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.authentication.configuration.EnableGlobalAuthentication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableGlobalAuthentication
public class WebSecurityConfig{
	
//	@Bean
//	public BCryptPasswordEncoder passwordEncoder() {
//		return new BCryptPasswordEncoder();
//	};

/*
    @Configuration
    @Order( 2 )
    public static class RestApiSecurityConfig {
        private Logger LOG = LoggerFactory.getLogger( RestApiSecurityConfig.class );

        @Bean
        public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
    		LOG.info("configure: " + http.toString() );		
    		http
    			.csrf().disable()
    			.addFilterAfter(new JWTAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class)
    			.authorizeHttpRequests( auth -> auth
    				.requestMatchers(HttpMethod.POST, "/api/authorize/**").permitAll()
    				.requestMatchers( "/api/**" ).hasAuthority( "API" )
    			)
    			.formLogin()
    				.loginPage("/api/notauthenticated")
    				.permitAll();
    		http.cors();
    		return http.build();
    	}
    }
*/
    @Configuration
    @Order( 1 )
    public static class UiSecurityConfig {
        private Logger LOG = LoggerFactory.getLogger( UiSecurityConfig.class );

    	@Bean
    	public BCryptPasswordEncoder passwordEncoder() {
    		return new BCryptPasswordEncoder();
    	};
        
    	@Bean
    	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
    	    return authenticationConfiguration.getAuthenticationManager();
    	}
    	
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    		LOG.info("configure: " + http.toString() );		
    		http
    			.authorizeHttpRequests( auth -> auth 
    				.requestMatchers( "/css/**", "/js/**", "/webjars/**", "/images/**", "/validate/**", "/password/**", "/passwordReset/**" ).permitAll()	
    				.requestMatchers( "/category/**", "/process/**", "/measureType/**", "/batch/**", "/measurement/**", "/sensor/**", "/domain/**", "/user/**"  ).hasRole( "ADMIN" )
    				.requestMatchers( "/**", "/profile/**" ).hasAnyRole( "ADMIN", "USER" )
    			)
    			.formLogin()
    				.loginPage("/login")
    				.permitAll()
    				.and()
    			.logout()
    				.permitAll();
    		http.cors();
    		return http.build();
    	}        
        
        @Bean
        public WebSecurityCustomizer webSecurityCustomizer() {
            return (web) -> web.ignoring().requestMatchers( "/h2/**", "/swagger-ui**" );
        }    	
    }
    
}
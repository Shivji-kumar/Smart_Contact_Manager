package com.smart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class MyConfiguration{
	
	@Bean
	public UserDetailsService getUserDetailService() {
		
		return new UserDetailsServiceImpl();
	}
	
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		
		return new BCryptPasswordEncoder();
	}
		
	//configure method..
	@Bean
	 public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
		http.authorizeRequests()
		.requestMatchers("/admin/**")
		.hasRole("ADMIN")
		.requestMatchers("/user/**")
		.hasRole("User")
		.requestMatchers("/**")
		.permitAll()
		.and()
		.formLogin().
		loginPage("/signin")
		.loginProcessingUrl("/dologin")
		.defaultSuccessUrl("/dashboard")
		.and()
		.csrf()
		.disable();
		http.authenticationProvider(daoAuthenticationProvider());
		DefaultSecurityFilterChain defaultSecurityFilterChain= http.build();
		return defaultSecurityFilterChain;
	 }
	
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)throws Exception {
		return configuration.getAuthenticationManager();
	}
	
	@Bean
	public DaoAuthenticationProvider daoAuthenticationProvider() {
		DaoAuthenticationProvider provider=new DaoAuthenticationProvider();
		provider.setUserDetailsService(this.getUserDetailService());
		provider.setPasswordEncoder(passwordEncoder());
		return provider;
	}

}

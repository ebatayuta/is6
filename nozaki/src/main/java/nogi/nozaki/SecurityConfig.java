package nogi.nozaki;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.ignoringRequestMatchers("/matches/**"))
			.authorizeHttpRequests(authorize -> authorize
				.requestMatchers("/matches", "/matches/**").permitAll()
				.anyRequest().authenticated())
			.formLogin(form -> form.permitAll());
		return http.build();
	}
}

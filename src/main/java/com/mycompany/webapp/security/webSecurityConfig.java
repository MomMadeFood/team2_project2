package com.mycompany.webapp.security;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;

import lombok.extern.slf4j.Slf4j;

@EnableWebSecurity
@Slf4j
public class webSecurityConfig extends WebSecurityConfigurerAdapter {

	@Resource
	private DataSource dataSource;

	@Resource
	private PasswordEncoder passwordEncoder;
	
	@Resource
	private MemberLoginFailureHandler memberLoginFailureHandler;

	// 회원가입을 할 때등 여러 곳에서 패스워드 인코더를 사용하기 때문에 관리 빈으로 반드시 등록해야 한다.
	@Bean // @Bean : 메소드가 return 하는 객체를 관리 객체로 만들어준다.
	public PasswordEncoder passwordEncoder() {
		PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();  	// password column 저장 형식 : {암호화알고리즘}암호화된패스워드
		return passwordEncoder;
	}
	
	@Resource
	private RoleHierarchyImpl roleHierarchyImpl;
	
	// HttpSecurity에서 관리 계층을 참조하기 위해 관리 빈으로 반드시 등록해야 한다.
	@Bean
	public RoleHierarchyImpl roleHierarchyImpl() {
		RoleHierarchyImpl roleHierarchyImpl = new RoleHierarchyImpl();
		roleHierarchyImpl.setHierarchy("ROLE_ADMIN > ROLE_MANAGER > ROLE_USER");
		return roleHierarchyImpl;
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		log.info("configure(HttpSecurity http) 실행");
		// 로그인 설정
		http.formLogin()
			.loginPage("/member/loginForm") 
			.usernameParameter("id") 
			.passwordParameter("password")
			.loginProcessingUrl("/login")
			.defaultSuccessUrl("/", false)
			.failureHandler(memberLoginFailureHandler);

		// 로그아웃 설정
		http.logout().logoutUrl("/logout") // default: /logout
				.logoutSuccessUrl("/member/loginForm"); // default: /

		// URL 권한 설정
		http.authorizeRequests()
			.antMatchers("/member/loginForm").permitAll()
			.antMatchers("/cart/**").authenticated()
			.antMatchers("/order/**").authenticated()
			.antMatchers("/member/**").authenticated()
			.antMatchers("/event/**").authenticated()
			.antMatchers("/**").permitAll();

		// 403(권한x) 에러가 발생할 경우 이동할 경로 설정
		http.exceptionHandling().accessDeniedPage("/error/403");

		// CSRF 설정
		http.csrf().disable();
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		log.info("configure(AuthenticationManagerBuilder auth) 실행");
		// 데이터베이스에서 가져올 사용자 정보 설정
		auth.jdbcAuthentication()
			.dataSource(dataSource)
			.usersByUsernameQuery("SELECT id, password, enabled FROM member WHERE id=?")
			.authoritiesByUsernameQuery("SELECT id, role FROM member WHERE id=?")
			.passwordEncoder(passwordEncoder); // default : DelegatingPasswordEncoder // 패스워드 인코딩 방법 설정
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		log.info("configure(WebSecurity web) 실행");
		// 권한 계층 관계 설정
		DefaultWebSecurityExpressionHandler handler = new DefaultWebSecurityExpressionHandler();
		handler.setRoleHierarchy(roleHierarchyImpl);
		web.expressionHandler(handler);
		
		// 인증이 필요 없는 경로 설정
		web.ignoring()
			.antMatchers("/bootstrap-4.6.0-dist/**")
			.antMatchers("/css/**")
			.antMatchers("/favicon.ico")
			.antMatchers("/images/**")
			.antMatchers("/jquery/**")
			.antMatchers("/js/**");
	}
}

package jp.onehr.securitystarter.uaa.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.logout.HeaderWriterLogoutHandler;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.header.writers.ClearSiteDataHeaderWriter;
import org.springframework.security.web.savedrequest.NullRequestCache;
import org.springframework.security.web.session.DisableEncodeUrlFilter;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

//    @Bean
//    SessionAuthenticationStrategy sessionAuthenticationStrategy(SessionRegistry sessionRegistry) {
//        ConcurrentSessionControlAuthenticationStrategy concurrentSessionControlStrategy =
//                new ConcurrentSessionControlAuthenticationStrategy(sessionRegistry);
//        concurrentSessionControlStrategy.setMaximumSessions(1);
//        concurrentSessionControlStrategy.setExceptionIfMaximumExceeded(true);
//        var sessionFixationProtectionStrategy = new SessionFixationProtectionStrategy();
//        sessionFixationProtectionStrategy.setMigrateSessionAttributes(true);
//        sessionFixationProtectionStrategy.setAlwaysCreateSession(true);
//
//        // 这里可以添加其他的策略，例如SessionFixationProtectionStrategy, RegisterSessionAuthenticationStrategy
//        //要注意这3个实例在集合中的顺序
//        return new CompositeSessionAuthenticationStrategy(
//                Arrays.asList(
//                        concurrentSessionControlStrategy,
//                        new ChangeSessionIdAuthenticationStrategy(),
//                        new RegisterSessionAuthenticationStrategy(sessionRegistry)
//                ));
//    }

    @Bean
    DemoFilter demoFilter() {
        return new DemoFilter();
    }

    @Bean
    SecurityContextRepository securityContextRepository() {
        return new DelegatingSecurityContextRepository(
                new RequestAttributeSecurityContextRepository(),
                new HttpSessionSecurityContextRepository()
        );
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http,
                                    SecurityContextRepository securityContextRepository,
                                    DemoFilter demoFilter,
                                    RememberMeServices rememberMeServices
//                                    BasicAuthenticationFilter basicAuthenticationFilter
    ) throws Exception {


        http
//                .addFilterAt(basicAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)  // 添加自定义过滤器
                .anonymous(AbstractHttpConfigurer::disable)
                .addFilterBefore(demoFilter, DisableEncodeUrlFilter.class)
                .requestCache(cache-> cache.requestCache(new NullRequestCache()))
            .securityContext((securityContext) -> securityContext
                    .securityContextRepository(securityContextRepository)
                    .requireExplicitSave(true)
            )
            .formLogin(form->form.loginPage("/login")
                    .successHandler((req,resp,authentication) -> {
                        SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();
                        SecurityContext context = securityContextHolderStrategy.createEmptyContext();
                        context.setAuthentication(authentication);
                        securityContextHolderStrategy.setContext(context);
                        securityContextRepository.saveContext(context,req,resp);
                        resp.sendRedirect("/");
                    }).failureHandler((req,resp,ex)->{
                        ex.printStackTrace();
                        resp.sendRedirect("/login");
                    })
            )
                // 只要是rest api方式的地址都不需要csrf拦截
            .csrf(csrf->csrf.ignoringRequestMatchers("/webjars/**","/error"))
            .exceptionHandling(exceptions ->
                    exceptions
                            .authenticationEntryPoint((request, response, authException) -> {
                                // 重定向到自定义登录页面
                                response.sendRedirect("/login");
                            }))
            .logout(logout->
                    logout
                            .addLogoutHandler(new HeaderWriterLogoutHandler(new ClearSiteDataHeaderWriter(ClearSiteDataHeaderWriter.Directive.COOKIES)))
// 移除自定义的其他cookie
//                            .addLogoutHandler(new CookieClearingLogoutHandler("our-custom-cookie"))
//                            .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler())
                            .logoutSuccessHandler((req,res,authentication) -> {
                                res.sendRedirect("/login");
                            })
            )
            .sessionManagement(session->
                    session
                            .sessionAuthenticationStrategy(
                                    new ChangeSessionIdAuthenticationStrategy()
                            )
                            .maximumSessions(1)
                            .maxSessionsPreventsLogin(false)
//                                .expiredSessionStrategy()
            )
            // 记住我 和maximumSessions，maxSessionsPreventsLogin一起使用时会有问题
            // 例如: 当使用记住我功能完成登录后关闭浏览器再打开，使用其他浏览器使用相同账号登录，此时maximumSessions，maxSessionsPreventsLogin就会失效
            .rememberMe((remember) -> remember
                    .rememberMeServices(rememberMeServices)
                     .key("remember-me")
            )
            .authorizeHttpRequests((authorize) -> authorize
                .requestMatchers("/login","/webjars/**","/error").permitAll()
//                    .requestMatchers("/admin/**").hasRole("ADMIN")
//                    .requestMatchers("/api/**").hasRole("USER")
                    .anyRequest().access(new CustomAuthorizationManager())
            );
//                .apply(new PartSecurityConfig());
        return http.build();
    }

//    @Bean
//    BasicAuthenticationFilter getBasicAuthenticationFilter(SessionAuthenticationStrategy sessionAuthenticationStrategy,
//                                                                          AuthenticationManager authenticationManager,
//                                                                          SecurityContextRepository securityContextRepository,
//                                                           SessionRegistry sessionRegistry) {
//        BasicAuthenticationFilter basicAuthenticationFilter = new  BasicAuthenticationFilter(sessionAuthenticationStrategy);
//        basicAuthenticationFilter.setAuthenticationSuccessHandler((req,resp,authentication)->{
//            SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();
//            SecurityContext context = securityContextHolderStrategy.createEmptyContext();
//            context.setAuthentication(authentication);
//            securityContextHolderStrategy.setContext(context);
//            securityContextRepository.saveContext(context,req,resp);
//            var sessions = sessionRegistry.getAllSessions(authentication.getPrincipal(),true);
//            for (SessionInformation session : sessions) {
//                System.out.println(session.getSessionId());
//                System.out.println(session.getPrincipal());
//                System.out.println(session.getLastRequest());
//                System.out.println("---");
//            }
//            resp.sendRedirect("/");
//        });
//        basicAuthenticationFilter.setAuthenticationFailureHandler((req,resp,exception)->{
//            resp.sendRedirect("/login");
//        });
//        basicAuthenticationFilter.setAuthenticationManager(authenticationManager);
//        return basicAuthenticationFilter;
//    }

    @Bean
    RememberMeServices rememberMeServices(UserDetailsService userDetailsService, PersistentTokenRepository tokenRepository) {
        PersistentTokenBasedRememberMeServices rememberMe = new PersistentTokenBasedRememberMeServices("remember-me", userDetailsService, tokenRepository);
        rememberMe.setTokenValiditySeconds(30*24*3600);
        rememberMe.setCookieName("remember-me");
        return rememberMe;
    }

    @Bean
    SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    FilterRegistrationBean<DemoFilter> demoFilterRegistration(DemoFilter filter) {
        FilterRegistrationBean<DemoFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }


//    @Bean
//    AuthenticationManager authenticationManager(
//            PasswordEncoder passwordEncoder,
//            AuthenticationEventPublisher authenticationEventPublisher,
//            JdbcUserDetailsManager userDetailsService
//    ) {
//        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
//        authenticationProvider.setPasswordEncoder(passwordEncoder);
//        authenticationProvider.setUserDetailsService(userDetailsService);
//        authenticationProvider.setUserDetailsPasswordService();
//
//        ProviderManager providerManager = new ProviderManager(authenticationProvider);
//        providerManager.setEraseCredentialsAfterAuthentication(true);
//        providerManager.setAuthenticationEventPublisher(authenticationEventPublisher);
//        return providerManager;
//    }

    @Bean
    JdbcUserDetailsManager userDetailsService(DataSource dataSource) {
        UserDetails user = User.builder()
                .username("user")
                .password("{bcrypt}$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW")
                .roles("USER")
                .build();
        UserDetails admin = User.builder()
                .username("admin")
                .password("{bcrypt}$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW")
                .roles("USER", "ADMIN")
                .build();
        JdbcUserDetailsManager users = new JdbcUserDetailsManager(dataSource);
//        users.createUser(user);
//        users.createUser(admin);
        return users;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    AuthenticationEventPublisher authenticationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        return new DefaultAuthenticationEventPublisher(applicationEventPublisher);
    }

    @Bean
    HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

//    @Bean
//    DataSource dataSource() {
//        return new EmbeddedDatabaseBuilder()
//                .setType(EmbeddedDatabaseType.H2)
//                .addScript(JdbcDaoImpl.DEFAULT_USER_SCHEMA_DDL_LOCATION)
//                .build();
//    }


    @Bean
    PersistentTokenRepository persistentTokenRepository(DataSource dataSource) {
        JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
        tokenRepository.setDataSource(dataSource);
        // tokenRepository.setCreateTableOnStartup(true);  // Uncomment this line if you want to create the table on startup
        return tokenRepository;
    }

}

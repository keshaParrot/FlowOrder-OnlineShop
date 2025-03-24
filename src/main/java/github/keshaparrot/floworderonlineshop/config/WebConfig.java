package github.keshaparrot.floworderonlineshop.config;

import github.keshaparrot.floworderonlineshop.security.FirebaseAuthFilter;
import github.keshaparrot.floworderonlineshop.security.RoleInterceptor;
import github.keshaparrot.floworderonlineshop.security.SecurityHeadersFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final RoleInterceptor roleInterceptor;
    private final FirebaseAuthFilter firebaseAuthFilter;
    private final SecurityHeadersFilter securityHeadersFilter;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(roleInterceptor);
    }

    @Bean
    public FilterRegistrationBean<FirebaseAuthFilter> firebaseAuthFilterRegistration() {
        FilterRegistrationBean<FirebaseAuthFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(firebaseAuthFilter);
        registrationBean.addUrlPatterns("/api/*");
        return registrationBean;
    }
    @Bean
    public FilterRegistrationBean<SecurityHeadersFilter> securityHeadersFilterRegistration() {
        FilterRegistrationBean<SecurityHeadersFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(securityHeadersFilter);
        registrationBean.addUrlPatterns("/*");
       registrationBean.setOrder(0);
        return registrationBean;
    }
}


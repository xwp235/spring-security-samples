package jp.onehr.securitystarter.uaa.config;

import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.FilterInvocation;

import java.util.Collection;

public class CustomVoter implements AccessDecisionVoter<FilterInvocation> {

    @Override
    public int vote(Authentication authentication, FilterInvocation filterInvocation, Collection<ConfigAttribute> attributes) {
        if (authentication == null) {
            return ACCESS_DENIED; // 如果没有认证信息，拒绝访问
        }

        // 获取请求的URL
        String requestUrl = filterInvocation.getRequestUrl();

        // 自定义逻辑：判断是否某些角色被拒绝访问这个URL
        if (isRoleDeniedForUrl(authentication, requestUrl)) {
            return ACCESS_DENIED;  // 如果有角色被拒绝访问，拒绝请求
        }

        return ACCESS_GRANTED; // 如果所有角色允许访问，允许请求
    }

    // 自定义逻辑：根据角色和URL判断是否拒绝访问
    private boolean isRoleDeniedForUrl(Authentication authentication, String url) {
        // 获取当前用户的角色
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        // 模拟逻辑：假设某个特定的URL不允许特定角色访问
        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority();
            if ("ROLE_DENIED".equals(role) && url.startsWith("/admin")) {
                return true; // 如果是ROLE_DENIED角色访问/admin路径，拒绝访问
            }
        }
        return false; // 否则允许访问
    }

    @Override
    public boolean supports(ConfigAttribute attribute) {
        return true; // 支持所有的ConfigAttribute
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return FilterInvocation.class.isAssignableFrom(clazz); // 支持FilterInvocation类型
    }
}

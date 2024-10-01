package jp.onehr.securitystarter.uaa.config;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.SecurityMetadataSource;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Supplier;

@RequiredArgsConstructor
@Component
public class CustomAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authenticationSupplier, RequestAuthorizationContext context) {
//        Authentication authentication = authenticationSupplier.get();
//        String requestUrl = context.getRequest().getRequestURI();
//
//        // 从数据库获取与请求URL相关联的角色
//        List<String> requiredRoles = databaseService.getRolesByUrl(requestUrl);
//
//        // 检查用户是否具备所需的角色
//        for (String requiredRole : requiredRoles) {
//            if (authentication.getAuthorities().stream()
//                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(requiredRole))) {
//                return new AuthorizationDecision(true);
//            }
//        }
        return new AuthorizationDecision(false);
//        authentication.getAuthorities()
//        return new AuthorizationDecision(true);
    }

}

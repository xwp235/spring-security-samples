package jp.onehr.securitystarter.uaa.config;

import lombok.RequiredArgsConstructor;
import org.apache.catalina.session.StandardSessionFacade;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.session.SessionCreationEvent;
import org.springframework.security.core.session.SessionDestroyedEvent;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticationEvents {

    private final SessionRegistry sessionRegistry;

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent success) {
        System.out.println("111");
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent failures) {
        System.out.println("***");
    }

    @EventListener
    public void onSessionCreated(SessionCreationEvent event) {
        System.out.println("Session created: " + event.getSource());
//        var sessionFacade = (StandardSessionFacade)event.getSource();
//        // 注册会话
//        sessionRegistry.registerNewSession(sessionFacade.getId(), "user");
    }

    @EventListener
    public void onSessionDestroyed(SessionDestroyedEvent event) {
        var sessionFacade = (StandardSessionFacade)event.getSource();
        // 注销会话
//        sessionRegistry.removeSessionInformation(sessionFacade.getId());
    }

}

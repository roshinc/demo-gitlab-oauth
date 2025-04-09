package dev.roshin.login.demogitlaboauth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Configuration
public class OAuthClientConfig1 {

    @Value("${gitlab.api.base-url}")
    private String gitlabBaseUrl;

    @Value("${gitlab.project-id}")
    private String projectId;

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        String clientId;
        String clientSecret;

        if (isLocalEnv()) {
            // Local fallback: read from properties
            clientId = System.getProperty("GITLAB_CLIENT_ID", "default-local-client-id");
            clientSecret = System.getProperty("GITLAB_CLIENT_SECRET", "default-local-secret");
        } else {
            // Call secret service
            Map<String, String> secrets = fetchSecretsFromRemote();
            clientId = secrets.get("clientId");
            clientSecret = secrets.get("clientSecret");
        }

        ClientRegistration gitlab = ClientRegistration.withRegistrationId("gitlab")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("api")
                .authorizationUri(gitlabBaseUrl + "/oauth/authorize")
                .tokenUri(gitlabBaseUrl + "/oauth/token")
                .userInfoUri(gitlabBaseUrl + "/api/v4/user")
                .userNameAttributeName("username")
                .clientName("GitLab")
                .build();

        return new InMemoryClientRegistrationRepository(gitlab);
    }

    private boolean isLocalEnv() {
        return System.getenv("ENV") == null || "local".equals(System.getenv("ENV"));
    }

    private Map<String, String> fetchSecretsFromRemote() {
        // Use WebClient or RestTemplate to call your Secret REST service
        WebClient client = WebClient.create("https://your-secrets-service/api/gitlab");
        return client.get()
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {})
                .block();
    }
}


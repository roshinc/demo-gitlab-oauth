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
public class OAuthClientConfig {

    private final GitLabAppProperties gitlabProps;
    private final WebClient.Builder webClientBuilder;

    @Value("${ENV:local}")
    private String environment;

    public OAuthClientConfig(GitLabAppProperties gitlabProps, WebClient.Builder webClientBuilder) {
        this.gitlabProps = gitlabProps;
        this.webClientBuilder = webClientBuilder;
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        String clientId;
        String clientSecret;

        if ("local".equalsIgnoreCase(environment)) {
            // Fallback to application.properties
            clientId = System.getProperty("GITLAB_CLIENT_ID", "your-local-client-id");
            clientSecret = System.getProperty("GITLAB_CLIENT_SECRET", "your-local-secret");
        } else {
            // Fetch from secret service
            Map<String, String> secrets = fetchSecretsFromService();
            clientId = secrets.get("clientId");
            clientSecret = secrets.get("clientSecret");
        }

        ClientRegistration registration = ClientRegistration.withRegistrationId("gitlab")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("api")
                .authorizationUri(gitlabProps.getApiBaseUrl() + "/oauth/authorize")
                .tokenUri(gitlabProps.getApiBaseUrl() + "/oauth/token")
                .userInfoUri(gitlabProps.getApiBaseUrl() + "/api/v4/user")
                .userNameAttributeName("username")
                .clientName("GitLab")
                .build();

        return new InMemoryClientRegistrationRepository(registration);
    }

    private Map<String, String> fetchSecretsFromService() {
        return webClientBuilder.build()
                .get()
                .uri("https://your-secrets-service/api/gitlab")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {})
                .block();
    }
}

package dev.roshin.login.demogitlaboauth.controller;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    private final WebClient.Builder webClientBuilder;

    @Value("${gitlab.api.base-url}")
    private String gitlabBaseUrl;

    @Value("${gitlab.project-id}")
    private String projectId;

    public HomeController(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @GetMapping("/")
    public String home(Model model,
                       @AuthenticationPrincipal OAuth2User principal,
                       @RegisteredOAuth2AuthorizedClient("gitlab") OAuth2AuthorizedClient authorizedClient) {

        model.addAttribute("name", principal.getAttribute("name"));
        model.addAttribute("username", principal.getAttribute("username"));
        model.addAttribute("avatar", principal.getAttribute("avatar_url"));

        String accessToken = authorizedClient.getAccessToken().getTokenValue();
        String issuesUrl = gitlabBaseUrl + "/api/v4/projects/" + projectId + "/issues";

        try {
            WebClient client = webClientBuilder.build();
            List<Map<String, Object>> allIssues = client.get()
                    .uri(issuesUrl)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToFlux(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .collectList()
                    .block();

            // Filter to just issues created by the current user
            String currentUsername = principal.getAttribute("username");

            List<Map<String, Object>> userIssues = allIssues.stream()
                    .filter(issue -> {
                        Map<String, Object> author = (Map<String, Object>) issue.get("author");
                        return author != null && currentUsername.equals(author.get("username"));
                    })
                    .toList();

            model.addAttribute("issues", userIssues);

        } catch (Exception e) {
            model.addAttribute("issuesError", "Failed to fetch issues: " + e.getMessage());
        }

        return "home";
    }
}

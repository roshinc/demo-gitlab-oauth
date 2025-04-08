package dev.roshin.login.demogitlaboauth.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Map;

@Controller
public class IssueController {

    private final WebClient.Builder webClientBuilder;

    @Value("${gitlab.api.base-url}")
    private String gitlabBaseUrl;

    @Value("${gitlab.project-id}")
    private String projectId;

    public IssueController(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @GetMapping("/new-issue")
    public String showIssueForm() {
        System.out.println("Showing issue form");
        return "issue";
    }

    @PostMapping("/create-issue")
    public String createIssue(@RequestParam String title,
                              @RequestParam String description,
                              @RegisteredOAuth2AuthorizedClient("gitlab") OAuth2AuthorizedClient authorizedClient,
                              Model model) {
        System.out.println("Creating issue");
        String accessToken = authorizedClient.getAccessToken().getTokenValue();

        String apiUrl = gitlabBaseUrl + "/api/v4/projects/" + projectId + "/issues";

        System.out.println("Access Token: " + accessToken);
        System.out.println("Posting to: " + apiUrl);

        try {
            WebClient client = webClientBuilder.build();

            String response = client.post()
                    .uri(apiUrl)
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("title", title, "description", description))
                    .retrieve()
                    .onStatus(status -> status.isError(), eResponse -> {
                        System.out.println("GitLab API returned status: " + eResponse.statusCode());
                        return eResponse.bodyToMono(String.class).flatMap(body -> {
                            System.out.println("Error body: " + body);
                            return Mono.error(new RuntimeException("Failed to create issue: " + body));
                        });
                    })
                    .bodyToMono(String.class)
                    .block();

            model.addAttribute("message", "Issue created successfully!");
            model.addAttribute("response", response);

        } catch (WebClientResponseException e) {
            model.addAttribute("message", "GitLab API error: " + e.getStatusCode());
            model.addAttribute("error", e.getResponseBodyAsString());
            return "result";

        } catch (Exception e) {
            model.addAttribute("message", "Failed to create issue.");
            model.addAttribute("error", e.getMessage());
        }

        return "result";
    }
}

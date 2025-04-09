package dev.roshin.login.demogitlaboauth;

import dev.roshin.login.demogitlaboauth.config.GitLabAppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication

@EnableConfigurationProperties(GitLabAppProperties.class)
public class DemoGitlabOauthApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoGitlabOauthApplication.class, args);
    }

}

package com.example.issuesdashboard.github;

import com.example.issuesdashboard.GithubProperties;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class GithubClient {

    private static final String EVENT_ISSUES_URL = "https://api.github.com/repos/{owner}/{repo}/issues/events";
    private final RestTemplate restTemplate;

    public GithubClient(RestTemplateBuilder builder,
                        GithubProperties properties,
                        MeterRegistry registry) {
        this.restTemplate = builder
                .additionalInterceptors(new GithubAppTokenInterceptor(properties.getToken()))
                .additionalInterceptors(new MetricsInterceptor(registry))
                .build();
    }

    public ResponseEntity<RepositoryEvent[]> fetchEvents(String orgName, String repoName){
        return this.restTemplate.getForEntity(EVENT_ISSUES_URL, RepositoryEvent[].class,
                orgName,repoName);
    }

    public List<RepositoryEvent> fetchEventList(String orgName, String repoName) {
        RepositoryEvent[] responseBody = fetchEvents(orgName, repoName).getBody();
        return Arrays.asList(responseBody);
    }

    private static class GithubAppTokenInterceptor implements ClientHttpRequestInterceptor {

        private final String token;

        public GithubAppTokenInterceptor(String token) {
            this.token = token;
        }

        @Override
        public ClientHttpResponse intercept(
                HttpRequest httpRequest,
                byte[] bytes,
                ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {
            if(StringUtils.hasText(this.token)){
                byte[] basicAuthValue = this.token.getBytes(StandardCharsets.UTF_8);
                httpRequest.getHeaders().set(HttpHeaders.AUTHORIZATION,
                        "Basic " + Base64Utils.encodeToString(basicAuthValue));
            }
            return clientHttpRequestExecution.execute(httpRequest, bytes);
        }
    }

    private static class MetricsInterceptor implements ClientHttpRequestInterceptor {

        private final AtomicInteger gauge;

        public MetricsInterceptor(MeterRegistry meterRegistry) {
            this.gauge = meterRegistry.gauge("github.ratelimit.remaining", new AtomicInteger(0));

        }

        @Override
        public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] bytes, ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {
            ClientHttpResponse response = clientHttpRequestExecution.execute(httpRequest, bytes);
            this.gauge.set(Integer.parseInt(response.getHeaders().getFirst("X-RateLimit-Remaining")));
            return response;
        }
    }
}

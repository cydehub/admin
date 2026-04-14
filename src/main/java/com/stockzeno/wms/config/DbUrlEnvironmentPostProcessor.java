package com.stockzeno.wms.config;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

public class DbUrlEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String existing = environment.getProperty("spring.datasource.url");
        if (existing != null && existing.startsWith("jdbc:")) {
            return;
        }
        String dbUrl = environment.getProperty("DB_URL");
        if (!StringUtils.hasText(dbUrl)) {
            return;
        }
        String trimmed = dbUrl == null ? "" : dbUrl.trim();
        String normalized = normalizeDbUrl(trimmed);
        if (normalized == null) {
            return;
        }
        Map<String, Object> props = new HashMap<>();
        props.put("spring.datasource.url", normalized);
        environment.getPropertySources().addFirst(new MapPropertySource("dbUrlOverride", props));
    }

    private String normalizeDbUrl(String dbUrl) {
        if (dbUrl.startsWith("jdbc:")) {
            return dbUrl;
        }
        if (!(dbUrl.startsWith("postgres://") || dbUrl.startsWith("postgresql://"))) {
            return null;
        }
        URI uri = URI.create(dbUrl);
        String host = uri.getHost();
        int port = uri.getPort();
        String path = uri.getPath() == null ? "" : uri.getPath();
        StringBuilder jdbc = new StringBuilder("jdbc:postgresql://").append(host);
        if (port > 0) {
            jdbc.append(":").append(port);
        }
        jdbc.append(path);

        List<String> params = new ArrayList<>();
        if (uri.getUserInfo() != null) {
            String[] parts = uri.getUserInfo().split(":", 2);
            params.add("user=" + encode(parts[0]));
            if (parts.length > 1) {
                params.add("password=" + encode(parts[1]));
            }
        }
        if (StringUtils.hasText(uri.getQuery())) {
            params.add(uri.getQuery());
        }
        if (!params.isEmpty()) {
            jdbc.append("?").append(String.join("&", params));
        }
        return jdbc.toString();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}

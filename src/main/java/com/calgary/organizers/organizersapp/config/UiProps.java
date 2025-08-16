package com.calgary.organizers.organizersapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.ui")
public record UiProps(String defaultTitle, String navbarTitle) {}

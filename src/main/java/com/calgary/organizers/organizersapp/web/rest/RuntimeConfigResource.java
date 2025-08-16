package com.calgary.organizers.organizersapp.web.rest;

import com.calgary.organizers.organizersapp.config.UiProps;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RuntimeConfigResource {

    private final UiProps ui;
    private final ObjectMapper om;

    public RuntimeConfigResource(UiProps ui, ObjectMapper om) {
        this.ui = ui;
        this.om = om;
    }

    @GetMapping(value = "/runtime-config.js", produces = "application/javascript")
    public ResponseEntity<String> runtimeConfig() throws JsonProcessingException {
        String json = om.writeValueAsString(Map.of("defaultTitle", ui.defaultTitle(), "navbarTitle", ui.navbarTitle()));

        String js = "window.__APP_CONFIG__ = " + json + ";";

        return ResponseEntity.ok().header(HttpHeaders.CACHE_CONTROL, CacheControl.noStore().getHeaderValue()).body(js);
    }
}

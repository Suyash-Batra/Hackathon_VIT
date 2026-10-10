package com.wildguard.conservation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

// Writes one line to the service log for every important action
// (who allocated which vehicle, who approved a release ...).
// The platform app has no audit API yet. When it gets one, only this class has to change.
@Component
public class ActivityLog {

    private static final Logger log = LoggerFactory.getLogger("conservation.activity");

    public void publish(String action, String type, String code, String message) {
        log.info("[{}] {} {} - {}", action, type, code, message);
    }
}

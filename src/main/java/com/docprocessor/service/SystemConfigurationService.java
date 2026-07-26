package com.docprocessor.service;

import com.docprocessor.model.SystemConfiguration;
import com.docprocessor.repository.SystemConfigurationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SystemConfigurationService {

    private final SystemConfigurationRepository repository;

    @Value("${app.defaults.title}")
    private String defaultTitle;

    @Value("${app.defaults.description}")
    private String defaultDescription;

    @Value("${app.defaults.version}")
    private String defaultVersion;

    @Value("${app.defaults.document-code}")
    private String defaultDocumentCode;

    @Value("${ldap.enabled}")
    private boolean defaultLdapEnabled;

    public SystemConfigurationService(SystemConfigurationRepository repository) {
        this.repository = repository;
    }

    private String getOrInit(String key, String defaultValue) {
        return repository.findByConfigKey(key)
                .map(SystemConfiguration::getConfigValue)
                .orElseGet(() -> {
                    repository.save(new SystemConfiguration(key, defaultValue));
                    return defaultValue;
                });
    }

    @Transactional(readOnly = true)
    public String getAppTitle() {
        return getOrInit("app.title", defaultTitle);
    }

    @Transactional(readOnly = true)
    public String getAppDescription() {
        return getOrInit("app.description", defaultDescription);
    }

    @Transactional(readOnly = true)
    public String getAppVersion() {
        return getOrInit("app.version", defaultVersion);
    }

    @Transactional(readOnly = true)
    public String getAppDocumentCode() {
        return getOrInit("app.document-code", defaultDocumentCode);
    }

    @Transactional(readOnly = true)
    public boolean isLdapEnabled() {
        String val = getOrInit("ldap.enabled", String.valueOf(defaultLdapEnabled));
        return Boolean.parseBoolean(val);
    }

    @Transactional
    public void setAppTitle(String title) {
        updateConfig("app.title", title);
    }

    @Transactional
    public void setAppDescription(String desc) {
        updateConfig("app.description", desc);
    }

    @Transactional
    public void setAppVersion(String version) {
        updateConfig("app.version", version);
    }

    @Transactional
    public void setAppDocumentCode(String code) {
        updateConfig("app.document-code", code);
    }

    @Transactional
    public void setLdapEnabled(boolean enabled) {
        updateConfig("ldap.enabled", String.valueOf(enabled));
    }

    private void updateConfig(String key, String value) {
        SystemConfiguration config = repository.findByConfigKey(key)
                .orElse(new SystemConfiguration(key, value));
        config.setConfigValue(value);
        repository.save(config);
    }
}

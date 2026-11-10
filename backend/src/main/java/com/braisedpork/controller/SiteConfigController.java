package com.braisedpork.controller;

import com.braisedpork.dto.Result;
import com.braisedpork.entity.SiteConfig;
import com.braisedpork.repository.SiteConfigRepository;
import com.braisedpork.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class SiteConfigController {

    private static final Set<String> EDITABLE_AD_KEYS = Set.of("ad_title", "ad_content", "ad_phone");

    private final SiteConfigRepository siteConfigRepository;

    @GetMapping("/ad")
    public ResponseEntity<Result<Map<String, String>>> getAdConfig() {
        Map<String, String> config = new HashMap<>();
        siteConfigRepository.findAll().forEach(sc -> config.put(sc.getConfigKey(), sc.getConfigValue()));
        return ResponseEntity.ok(Result.success(config));
    }

    @PutMapping("/ad")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<Void>> updateAdConfig(@RequestBody Map<String, String> updates) {
        if (updates == null || updates.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "广告配置不能为空");
        }
        if (!EDITABLE_AD_KEYS.containsAll(updates.keySet())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "包含不允许修改的配置项");
        }
        updates.forEach((key, value) -> {
            SiteConfig sc = siteConfigRepository.findByConfigKey(key).orElse(new SiteConfig());
            sc.setConfigKey(key);
            sc.setConfigValue(value);
            sc.setUpdatedAt(LocalDateTime.now());
            siteConfigRepository.save(sc);
        });
        return ResponseEntity.ok(Result.success(null));
    }
}

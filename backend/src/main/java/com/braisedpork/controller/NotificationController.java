package com.braisedpork.controller;

import com.braisedpork.dto.Result;
import com.braisedpork.entity.Notification;
import com.braisedpork.repository.NotificationRepository;
import com.braisedpork.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;

    @GetMapping
    public ResponseEntity<Result<Page<Notification>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Notification> notifications = notificationRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
        return ResponseEntity.ok(Result.success(notifications));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<Void>> send(@RequestBody Notification notification) {
        notification.setCreatedAt(java.time.LocalDateTime.now());
        notificationRepository.save(notification);
        return ResponseEntity.ok(Result.success(null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<Void>> delete(@PathVariable Long id) {
        if (!notificationRepository.existsById(id)) {
            throw new ResourceNotFoundException("通知不存在");
        }
        notificationRepository.deleteById(id);
        return ResponseEntity.ok(Result.success(null));
    }

    @PostMapping("/ad")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<Void>> sendAdNotification(@RequestBody Notification notification) {
        notification.setSenderId(null);
        notification.setCreatedAt(java.time.LocalDateTime.now());
        notificationRepository.save(notification);
        return ResponseEntity.ok(Result.success(null));
    }
}

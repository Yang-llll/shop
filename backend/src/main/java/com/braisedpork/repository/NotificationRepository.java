package com.braisedpork.repository;

import com.braisedpork.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findAllByOrderByCreatedAtDesc(Pageable pageable);
}

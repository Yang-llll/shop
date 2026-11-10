package com.braisedpork.controller;

import com.braisedpork.dto.Result;
import com.braisedpork.entity.ChatMessage;
import com.braisedpork.entity.User;
import com.braisedpork.exception.BusinessException;
import com.braisedpork.exception.ResourceNotFoundException;
import com.braisedpork.exception.UnauthorizedException;
import com.braisedpork.repository.ChatMessageRepository;
import com.braisedpork.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    @GetMapping("/messages")
    public ResponseEntity<Result<List<ChatMessage>>> getMessages(
            @RequestParam Long partnerId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        if (!userRepository.existsById(partnerId)) {
            throw new ResourceNotFoundException("会话用户不存在");
        }
        List<ChatMessage> messages = chatMessageRepository.findConversation(user.getId(), partnerId);
        return ResponseEntity.ok(Result.success(messages));
    }

    @PostMapping("/send")
    public ResponseEntity<Result<ChatMessage>> sendMessage(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        User sender = getCurrentUser(userDetails);

        Object receiverIdObj = body.get("receiverId");
        Object contentObj = body.get("content");

        if (receiverIdObj == null || contentObj == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "接收人和消息内容不能为空");
        }

        long receiverId;
        try {
            receiverId = Long.parseLong(receiverIdObj.toString());
        } catch (NumberFormatException e) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "接收人编号格式错误");
        }

        if (!userRepository.existsById(receiverId)) {
            throw new ResourceNotFoundException("接收人不存在");
        }

        String content = contentObj.toString().trim();
        if (content.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "消息内容不能为空");
        }
        if (content.length() > 1000) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "消息内容不能超过1000个字符");
        }

        ChatMessage msg = new ChatMessage();
        msg.setSenderId(sender.getId());
        msg.setReceiverId(receiverId);
        msg.setContent(content);
        msg.setIsRead(false);
        msg.setCreatedAt(LocalDateTime.now());
        chatMessageRepository.save(msg);
        return ResponseEntity.ok(Result.success(msg));
    }

    @PostMapping("/read/{partnerId}")
    public ResponseEntity<Result<Void>> markAsRead(
            @PathVariable Long partnerId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        chatMessageRepository.markAsRead(partnerId, user.getId());
        return ResponseEntity.ok(Result.success(null));
    }

    @GetMapping("/partners")
    public ResponseEntity<Result<List<Map<String, Object>>>> getChatPartners(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        List<Object[]> rows = chatMessageRepository.findChatPartners(user.getId());
        List<Map<String, Object>> partners = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        for (Object[] row : rows) {
            Long partnerId = ((Number) row[0]).longValue();
            if (seen.contains(partnerId)) continue;
            seen.add(partnerId);
            Optional<User> u = userRepository.findById(partnerId);
            if (u.isPresent()) {
                Map<String, Object> info = new HashMap<>();
                info.put("id", partnerId);
                info.put("username", u.get().getUsername());
                info.put("role", u.get().getRole());
                partners.add(info);
            }
        }
        return ResponseEntity.ok(Result.success(partners));
    }

    @GetMapping("/unread")
    public ResponseEntity<Result<Integer>> getUnreadCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        int count = chatMessageRepository.findUnreadByReceiverId(user.getId()).size();
        return ResponseEntity.ok(Result.success(count));
    }

    private User getCurrentUser(UserDetails userDetails) {
        if (userDetails == null) {
            throw new UnauthorizedException("登录状态无效或已过期");
        }
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("当前用户不存在"));
    }
}

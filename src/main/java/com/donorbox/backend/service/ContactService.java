package com.donorbox.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.donorbox.backend.repository.*;
import com.donorbox.backend.entity.*;
import com.donorbox.backend.dto.*;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContactService {
    private final MessageRepository messageRepository;

    @Transactional
    public Message sendMessage(ContactRequest request) {
        Message message = Message.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .subject(request.getSubject())
                .content(request.getContent())
                .build();

        return messageRepository.save(message);
    }
}

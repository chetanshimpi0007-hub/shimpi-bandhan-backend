package com.shimpimilan.controller;

import com.shimpimilan.model.FamilyDiscussionRoom;
import com.shimpimilan.model.FamilyMessage;
import com.shimpimilan.model.User;
import com.shimpimilan.repository.FamilyDiscussionRoomRepository;
import com.shimpimilan.repository.FamilyMessageRepository;
import com.shimpimilan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class FamilyChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final FamilyMessageRepository messageRepository;
    private final FamilyDiscussionRoomRepository roomRepository;
    private final UserRepository userRepository;

    @MessageMapping("/family.chat.{roomId}")
    public void sendMessage(@DestinationVariable Long roomId, @Payload Map<String, Object> payload) {
        Long senderId = Long.valueOf(payload.get("senderId").toString());
        String content = (String) payload.get("content");
        String messageType = (String) payload.getOrDefault("messageType", "TEXT");
        String fileUrl = (String) payload.get("fileUrl");

        FamilyDiscussionRoom room = roomRepository.findById(roomId).orElseThrow();
        User sender = userRepository.findById(senderId).orElseThrow();

        FamilyMessage message = FamilyMessage.builder()
                .room(room)
                .sender(sender)
                .content(content)
                .messageType(messageType)
                .fileUrl(fileUrl)
                .build();

        FamilyMessage saved = messageRepository.save(message);

        // Broadcast to the room topic
        messagingTemplate.convertAndSend("/topic/family/" + roomId, saved);
    }

    @MessageMapping("/family.typing.{roomId}")
    public void typingIndicator(@DestinationVariable Long roomId, @Payload Map<String, Object> payload) {
        messagingTemplate.convertAndSend("/topic/family/" + roomId + "/typing", payload);
    }
}

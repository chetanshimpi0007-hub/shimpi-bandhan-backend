package com.shimpimilan.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class WebRTCSignalingController {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/webrtc.signal.{roomId}")
    public void handleSignaling(@DestinationVariable String roomId, @Payload Map<String, Object> payload) {
        // Forward SDP offers, answers, and ICE candidates to everyone else in the room
        messagingTemplate.convertAndSend("/topic/webrtc/" + roomId, payload);
    }
}

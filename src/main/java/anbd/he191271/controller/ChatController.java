package anbd.he191271.controller;

import anbd.he191271.service.AIChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@Controller
@RequestMapping("/chat")
public class ChatController {

    private final AIChatService chatService;

    public ChatController(AIChatService chatService) {
        this.chatService = chatService;
    }

    // ⚙️ 1️⃣ Trả về giao diện trang chat (GET)
    @GetMapping
    public String chatPage() {
        return "aichat"; // Tên file templates/chat.html
    }

    // 🤖 2️⃣ API để gửi tin nhắn và nhận phản hồi (POST)
    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> body) {
        String msg = body.get("message");
        String reply = chatService.chat(msg);
        return ResponseEntity.ok(Map.of("reply", reply));
    }
}

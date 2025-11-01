// controller/ChatController.java
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

    // Sử dụng constructor injection thay vì @Autowired
    public ChatController(AIChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping
    public String chatPage() {
        return "aichat";
    }

    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> body) {
        String message = body.get("message");
        String reply = chatService.chat(message);
        return ResponseEntity.ok(Map.of("message", reply));
    }
}
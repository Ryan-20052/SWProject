// dto/ChatRequest.java
package anbd.he191271.dto;

import lombok.Data;

@Data
public class ChatRequest {
    private String message;
    private String sessionId;
    private Long userId; // optional
}
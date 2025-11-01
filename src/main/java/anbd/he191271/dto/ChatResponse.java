// dto/ChatResponse.java
package anbd.he191271.dto;

import anbd.he191271.entity.Product;
import lombok.Data;
import java.util.List;

@Data
public class ChatResponse {
    private String message;
    private List<Product> products;
    private String responseType; // "PRODUCT", "GENERAL", "ERROR"
    private String sessionId;

    public ChatResponse(String message, List<Product> products, String sessionId) {
        this.message = message;
        this.products = products;
        this.sessionId = sessionId;
        this.responseType = (products != null && !products.isEmpty()) ? "PRODUCT" : "GENERAL";
    }
}
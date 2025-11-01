// dto/IntentAnalysis.java  
package anbd.he191271.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class IntentAnalysis {
    private boolean productRelated = false;
    private List<String> searchQueries;
    private BigDecimal maxPrice;
    private String category;
    private List<Long> productIds;

    public boolean hasPriceFilter() {
        return maxPrice != null;
    }
}
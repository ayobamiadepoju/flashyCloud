package hng.backend.fashion_agent.tools;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class OutfitService {

    private static final Gson gson = new Gson();

    @Tool(description = "Suggest an outfit based on current weather data.")
    public static Map<String, Object> getSuggestedOutfit(
            @ToolParam(description = "Current weather data as JSON string")
            String weatherJson) {

        Map<String, Object> response = new HashMap<>();

        try {
            Map<?, ?> weatherData = tryParseJson(weatherJson);

            if (weatherData == null || weatherData.isEmpty()) {
                response.put("error", "No weather data");
                response.put("outfit_suggestion", "Unable to suggest outfit without weather info.");
                return response;
            }

            double temperature = ((Number) weatherData.get("temperature")).doubleValue();
            String condition = (String) weatherData.get("condition");
            String summary = (String) weatherData.get("summary");

            String suggestion;

            // 🌤️ Outfit logic
            if (temperature > 30) {
                suggestion = "Wear light, airy outfits such as cotton shirts, sleeveless tops, and shorts. Sunglasses recommended! 😎";
            } else if (temperature > 25) {
                suggestion = "A casual shirt or t-shirt with chinos or jeans fits well. Consider sandals or light sneakers.";
            } else if (temperature > 18) {
                suggestion = "Go for a checkered shirt with denim or chinos, maybe add a light jacket.";
            } else if (temperature > 10) {
                suggestion = "Wear a sweater or hoodie over your shirt, with trousers or jeans.";
            } else {
                suggestion = "Bundle up with a coat, thick trousers, and warm accessories like gloves and a scarf.";
            }

            if (condition != null && condition.toLowerCase().contains("rain")) {
                suggestion += " Don’t forget a waterproof jacket or umbrella!";
            } else if (condition != null && condition.toLowerCase().contains("cloud")) {
                suggestion += " Layering works best for cloudy days — add a light jacket.";
            }

            response.put("weather_summary", summary);
            response.put("outfit_suggestion", suggestion);
            return response;

        } catch (Exception e) {
            response.put("error", "Error parsing weather data: " + e.getMessage());
            response.put("outfit_suggestion", "Unable to provide a recommendation.");
            return response;
        }
    }

    // 🔧 Helper to handle both plain and stringified JSON
    private static Map<?, ?> tryParseJson(String json) {
        try {
            return gson.fromJson(json, Map.class);
        } catch (JsonSyntaxException e) {
            try {
                String inner = gson.fromJson(json, String.class);
                return gson.fromJson(inner, Map.class);
            } catch (Exception ignored) {
                return null;
            }
        }
    }

    public String extractLocation(String text) {
        // Simple heuristic: look for "in <city>"
        String lower = text.toLowerCase();
        if (lower.contains(" in ")) {
            return text.substring(lower.indexOf(" in ") + 4).split(" ")[0]; // crude but works for now
        }
        return null;
    }
}

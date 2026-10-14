package com.project.RoutineBuilder.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.project.RoutineBuilder.dto.SkinAnalysisResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

/**
 * Detects a skin concern from an uploaded photo.
 * <ul>
 *   <li>If HUGGINGFACE_API_TOKEN is set, it calls a Hugging Face zero-shot image
 *   classification model (CLIP) with our concern labels (real AI).</li>
 *   <li>Otherwise, or if the AI call fails, it falls back to a simple colour-based
 *   heuristic so the demo always works offline.</li>
 * </ul>
 */
@Slf4j
@Service
public class SkinAnalysisService {

    public static final String BODY_ACNE = "Body Acne";
    public static final String HYPERPIGMENTATION = "Hyperpigmentation";
    public static final String STRAWBERRY_SKIN = "Strawberry Skin";

    private static final Map<String, String> AI_LABELS = new LinkedHashMap<>();

    static {
        AI_LABELS.put("a close-up photo of skin with red acne pimples and breakouts", BODY_ACNE);
        AI_LABELS.put("a close-up photo of skin with dark spots and uneven pigmentation", HYPERPIGMENTATION);
        AI_LABELS.put("a close-up photo of skin with tiny dark dotted pores like strawberry legs", STRAWBERRY_SKIN);
    }

    private final RoutineBuilderService routineBuilderService;
    private final RestClient restClient;
    private final String apiToken;
    private final String modelUrl;

    public SkinAnalysisService(
            RoutineBuilderService routineBuilderService,
            @Value("${app.ai.huggingface.token:}") String apiToken,
            @Value("${app.ai.huggingface.model-url}") String modelUrl) {
        this.routineBuilderService = routineBuilderService;
        this.apiToken = apiToken;
        this.modelUrl = modelUrl;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(30));
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    public SkinAnalysisResponse analyze(MultipartFile file) throws IOException {
        byte[] bytes = file.getBytes();
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
        if (image == null) {
            throw new IllegalArgumentException("Unsupported image format. Please upload a JPG or PNG.");
        }

        Detection detection = null;
        if (StringUtils.hasText(apiToken)) {
            try {
                detection = classifyWithHuggingFace(bytes);
            } catch (Exception ex) {
                log.warn("Hugging Face analysis failed, falling back to heuristic: {}", ex.getMessage());
            }
        }
        if (detection == null) {
            detection = classifyWithHeuristic(image);
        }

        return new SkinAnalysisResponse(
                detection.concern(),
                detection.confidence(),
                detection.source(),
                routineBuilderService.buildRoutine(detection.concern()));
    }

    private Detection classifyWithHuggingFace(byte[] bytes) {
        Map<String, Object> body = Map.of(
                "inputs", Base64.getEncoder().encodeToString(bytes),
                "parameters", Map.of("candidate_labels", List.copyOf(AI_LABELS.keySet())));

        JsonNode result = restClient.post()
                .uri(modelUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(JsonNode.class);

        if (result == null || !result.isArray() || result.isEmpty()) {
            throw new IllegalStateException("Empty response from Hugging Face");
        }

        JsonNode best = result.get(0);
        for (JsonNode node : result) {
            if (node.path("score").asDouble() > best.path("score").asDouble()) {
                best = node;
            }
        }
        String concern = AI_LABELS.get(best.path("label").asText());
        if (concern == null) {
            throw new IllegalStateException("Unexpected label: " + best.path("label").asText());
        }
        return new Detection(concern, round(best.path("score").asDouble()), "huggingface");
    }

    /**
     * Demo heuristic: strong redness suggests acne, many darker-than-average patches
     * suggest hyperpigmentation, otherwise assume strawberry skin.
     */
    private Detection classifyWithHeuristic(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int step = Math.max(1, Math.max(width, height) / 200);

        double rednessSum = 0;
        double luminanceSum = 0;
        int samples = 0;
        int sampleCount = ((width + step - 1) / step) * ((height + step - 1) / step);
        double[] luminances = new double[sampleCount];

        for (int y = 0; y < height; y += step) {
            for (int x = 0; x < width; x += step) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                rednessSum += Math.max(0, r - (g + b) / 2.0) / 255.0;
                double luminance = 0.299 * r + 0.587 * g + 0.114 * b;
                luminances[samples++] = luminance;
                luminanceSum += luminance;
            }
        }

        double redness = rednessSum / samples;
        double meanLuminance = luminanceSum / samples;
        int darkPixels = 0;
        for (int i = 0; i < samples; i++) {
            if (luminances[i] < meanLuminance * 0.7) {
                darkPixels++;
            }
        }
        double darkFraction = (double) darkPixels / samples;

        if (redness > 0.18) {
            return new Detection(BODY_ACNE, round(Math.min(0.95, 0.5 + redness)), "heuristic");
        }
        if (darkFraction > 0.08) {
            return new Detection(HYPERPIGMENTATION, round(Math.min(0.95, 0.5 + darkFraction)), "heuristic");
        }
        return new Detection(STRAWBERRY_SKIN, 0.55, "heuristic");
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private record Detection(String concern, double confidence, String source) {
    }
}

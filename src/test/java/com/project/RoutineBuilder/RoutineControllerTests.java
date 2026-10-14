package com.project.RoutineBuilder;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class RoutineControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void bodyAcneClashIsResolvedBySplittingAmAndPm() throws Exception {
        mockMvc.perform(post("/api/v1/routine/build")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"primaryConcern\":\"Body Acne\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderedRoutine[0].category").value("Cleanser"))
                .andExpect(jsonPath("$.orderedRoutine[3].category").value("Moisturizer"))
                .andExpect(jsonPath("$.amRoutine[*].activeIngredient", hasItem("Salicylic Acid")))
                .andExpect(jsonPath("$.amRoutine[*].activeIngredient", not(hasItem("Physical Scrub"))))
                .andExpect(jsonPath("$.pmRoutine[*].activeIngredient", hasItem("Physical Scrub")))
                .andExpect(jsonPath("$.pmRoutine[*].activeIngredient", not(hasItem("Salicylic Acid"))))
                .andExpect(jsonPath("$.adjustments", hasSize(1)))
                .andExpect(jsonPath("$.safetyCheck.isSafe").value(true));
    }

    @Test
    void strawberrySkinMovesRetinolToPm() throws Exception {
        mockMvc.perform(post("/api/v1/routine/build")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"primaryConcern\":\"Strawberry Skin\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pmRoutine[*].activeIngredient", hasItem("Retinol")))
                .andExpect(jsonPath("$.amRoutine[*].activeIngredient", not(hasItem("Retinol"))))
                .andExpect(jsonPath("$.safetyCheck.isSafe").value(true));
    }

    @Test
    void hyperpigmentationRoutineIsSafe() throws Exception {
        mockMvc.perform(post("/api/v1/routine/build")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"primaryConcern\":\"Hyperpigmentation\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.safetyCheck.isSafe").value(true))
                .andExpect(jsonPath("$.pmRoutine", hasSize(0)))
                .andExpect(jsonPath("$.adjustments", hasSize(0)));
    }

    @Test
    void analyzeSkinDetectsAcneFromRedImage() throws Exception {
        MockMultipartFile file = new MockMultipartFile("image", "red.png", "image/png", png(new Color(220, 60, 60), null));

        mockMvc.perform(multipart("/api/v1/routine/analyze-skin").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.detectedConcern").value("Body Acne"))
                .andExpect(jsonPath("$.source").value("heuristic"))
                .andExpect(jsonPath("$.routine.pmRoutine", hasSize(3)));
    }

    @Test
    void analyzeSkinDetectsHyperpigmentationFromDarkPatches() throws Exception {
        MockMultipartFile file = new MockMultipartFile("image", "spots.png", "image/png",
                png(new Color(225, 200, 180), new Color(110, 80, 60)));

        mockMvc.perform(multipart("/api/v1/routine/analyze-skin").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.detectedConcern").value("Hyperpigmentation"));
    }

    @Test
    void analyzeSkinRejectsNonImage() throws Exception {
        MockMultipartFile file = new MockMultipartFile("image", "notes.txt", "text/plain", "hello".getBytes());

        mockMvc.perform(multipart("/api/v1/routine/analyze-skin").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void corsAllowsViteDevServer() throws Exception {
        mockMvc.perform(options("/api/v1/routine/build")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    private byte[] png(Color base, Color spots) throws Exception {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(base);
        g.fillRect(0, 0, 100, 100);
        if (spots != null) {
            g.setColor(spots);
            g.fillRect(10, 10, 30, 30);
            g.fillRect(60, 55, 25, 25);
        }
        g.dispose();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return out.toByteArray();
    }
}

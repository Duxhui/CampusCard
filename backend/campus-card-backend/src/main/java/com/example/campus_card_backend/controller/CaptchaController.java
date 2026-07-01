package com.example.campus_card_backend.controller;

import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 验证码控制器（基于 token，不依赖 session/cookie）
 */
@RestController
@RequestMapping("/api/captcha")
public class CaptchaController {

    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final Random RAND = new Random();
    // token → code 内存映射
    private static final ConcurrentHashMap<String, String> CODE_MAP = new ConcurrentHashMap<>();

    /**
     * 生成验证码，返回 { token, imageBase64 }
     */
    @GetMapping("/image")
    public Map<String, String> image() throws IOException {
        int width = 120, height = 40;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();

        g.setColor(new Color(240, 240, 240));
        g.fillRect(0, 0, width, height);

        g.setColor(new Color(200, 200, 200));
        for (int i = 0; i < 5; i++) {
            g.drawLine(RAND.nextInt(width), RAND.nextInt(height),
                       RAND.nextInt(width), RAND.nextInt(height));
        }
        for (int i = 0; i < 30; i++) {
            img.setRGB(RAND.nextInt(width), RAND.nextInt(height), 0xCCCCCC);
        }

        StringBuilder code = new StringBuilder();
        g.setFont(new Font("Arial", Font.BOLD, 24));
        for (int i = 0; i < 4; i++) {
            String ch = String.valueOf(CHARS.charAt(RAND.nextInt(CHARS.length())));
            code.append(ch);
            g.setColor(new Color(RAND.nextInt(100), RAND.nextInt(100), RAND.nextInt(150)));
            g.drawString(ch, 15 + i * 25, 20 + RAND.nextInt(12));
        }
        g.dispose();

        // 生成 token
        String token = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        CODE_MAP.put(token, code.toString());

        // 图片转 Base64
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "PNG", baos);
        String base64 = "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());

        return Map.of("token", token, "imageBase64", base64);
    }

    /**
     * 校验验证码
     */
    public static boolean verify(String token, String input) {
        if (token == null || input == null) return false;
        String code = CODE_MAP.remove(token);  // 用完即删
        return code != null && code.equalsIgnoreCase(input);
    }
}

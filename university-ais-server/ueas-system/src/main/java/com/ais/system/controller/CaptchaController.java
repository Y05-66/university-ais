package com.ais.system.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.ais.common.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 验证码控制器。
 *
 * <p>提供图形验证码的生成接口，验证码存储于 Redis，有效期 5 分钟。
 */
@Tag(name = "验证码接口")
@RestController
@RequestMapping("/captcha")
public class CaptchaController {

    private static final String CAPTCHA_KEY_PREFIX = "ueas:captcha:";
    private static final long CAPTCHA_TTL_MINUTES = 5;
    private static final String CAPTCHA_CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final int CAPTCHA_LENGTH = 4;
    private static final int WIDTH = 220;
    private static final int HEIGHT = 60;

    private final StringRedisTemplate redisTemplate;
    private final Random random = new Random();

    public CaptchaController(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Operation(summary = "获取验证码")
    @SaIgnore
    @GetMapping
    public R<Map<String, String>> getCaptcha() {
        String key = UUID.randomUUID().toString().replace("-", "");

        StringBuilder captcha = new StringBuilder();
        for (int i = 0; i < CAPTCHA_LENGTH; i++) {
            captcha.append(CAPTCHA_CHARS.charAt(random.nextInt(CAPTCHA_CHARS.length())));
        }
        String code = captcha.toString();

        redisTemplate.opsForValue().set(CAPTCHA_KEY_PREFIX + key, code, CAPTCHA_TTL_MINUTES, TimeUnit.MINUTES);

        String image = generateCaptchaImage(code);

        Map<String, String> result = new HashMap<>();
        result.put("key", key);
        result.put("image", image);
        return R.ok(result);
    }

    private String generateCaptchaImage(String code) {
        try {
            // 确保 AWT 在 headless 模式下运行
            System.setProperty("java.awt.headless", "true");

            BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();

            // 启用抗锯齿
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // 白色背景
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, WIDTH, HEIGHT);

            // 随机干扰线（3条，浅灰色）
            for (int i = 0; i < 3; i++) {
                g2d.setColor(new Color(random.nextInt(150) + 80, random.nextInt(150) + 80, random.nextInt(150) + 80));
                g2d.setStroke(new BasicStroke(1.0f));
                g2d.drawLine(
                    random.nextInt(WIDTH), random.nextInt(HEIGHT),
                    random.nextInt(WIDTH), random.nextInt(HEIGHT)
                );
            }

            // 随机干扰点（10个）
            for (int i = 0; i < 10; i++) {
                g2d.setColor(new Color(random.nextInt(200) + 30, random.nextInt(200) + 30, random.nextInt(200) + 30));
                g2d.fillOval(random.nextInt(WIDTH), random.nextInt(HEIGHT), 2, 2);
            }

            // 大号字体（36号），字符几乎填满高度
            Font font = new Font("Arial", Font.BOLD, 36);
            g2d.setFont(font);
            FontMetrics fm = g2d.getFontMetrics();

            // 4个字符等分填满整个宽度，左右各留 6px 边距
            int margin = 6;
            int cellWidth = (WIDTH - margin * 2) / CAPTCHA_LENGTH;
            // 垂直居中
            int baseY = HEIGHT / 2 + fm.getAscent() / 2;

            for (int i = 0; i < code.length(); i++) {
                char c = code.charAt(i);
                int charW = fm.charWidth(c);

                // 随机鲜艳颜色
                int r = random.nextInt(100);
                int g = random.nextInt(100);
                int b = random.nextInt(100);
                g2d.setColor(new Color(r, g, b));

                // 字符在其单元格内居中，以单元格中心为旋转轴心
                int cellCenterX = margin + i * cellWidth + cellWidth / 2;
                int drawX = cellCenterX - charW / 2;

                // 不旋转，保持字符水平清晰可见
                AffineTransform transform = g2d.getTransform();
                g2d.drawString(String.valueOf(c), drawX, baseY);
                g2d.setTransform(transform);
            }
            g2d.dispose();

            // 转换为Base64
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);
            return java.util.Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            // 如果图形生成失败，返回简单的文本验证码
            return code;
        }
    }

    @Operation(summary = "校验验证码")
    @SaIgnore
    @PostMapping("/verify")
    public R<Boolean> verifyCaptcha(@RequestParam String key, @RequestParam String input) {
        if (key == null || key.isBlank() || input == null || input.isBlank()) {
            return R.fail("验证码参数不能为空");
        }

        String expected = redisTemplate.opsForValue().get(CAPTCHA_KEY_PREFIX + key);
        if (expected == null) {
            return R.fail("验证码已过期，请重新获取");
        }

        redisTemplate.delete(CAPTCHA_KEY_PREFIX + key);

        if (expected.equals(input)) {
            return R.ok(true);
        } else {
            return R.fail("验证码错误");
        }
    }
}

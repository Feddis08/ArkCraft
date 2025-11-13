package at.riemer.client.mixin;

import at.riemer.core.Main;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ResourceLoadProgressGui;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.resources.data.TextureMetadataSection;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.io.InputStream;

/* ================================
 * 1) Logo texture override
 * ================================ */
@OnlyIn(Dist.CLIENT)
@Mixin(targets = "net.minecraft.client.gui.ResourceLoadProgressGui$MojangLogoTexture")
public abstract class MojangLogoTextureMixin {
    private static final Logger LOG = LogManager.getLogger("ArkCraft");
    private static final ResourceLocation CUSTOM_RL =
            new ResourceLocation(Main.MOD_ID, "textures/gui/splash.png");
    private static final String CLASSPATH_PATH =
            "assets/" + Main.MOD_ID + "/textures/gui/splash.png";

    @Unique private static volatile int SPLASH_TEX_W = 120;
    @Unique private static volatile int SPLASH_TEX_H = 60;

    @Inject(
            method = "getTextureData(Lnet/minecraft/resources/IResourceManager;)Lnet/minecraft/client/renderer/texture/SimpleTexture$TextureData;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void arkcraft$overrideLogo(IResourceManager rm,
                                       CallbackInfoReturnable<SimpleTexture.TextureData> cir) {
        // Try via resource manager
        try {
            IResource res = rm.getResource(CUSTOM_RL);
            try (InputStream in = res.getInputStream()) {
                SimpleTexture.TextureData data = arkcraft$makeData(in);
                LOG.info("[ArkCraft] Splash from resource manager: {}", CUSTOM_RL);
                cir.setReturnValue(data);
                return;
            }
        } catch (Throwable ignored) {
            // fall through to classpath
        }

        // Fallback to classpath (earliest frames)
        try (InputStream in = Thread.currentThread()
                .getContextClassLoader().getResourceAsStream(CLASSPATH_PATH)) {
            if (in != null) {
                SimpleTexture.TextureData data = arkcraft$makeData(in);
                LOG.info("[ArkCraft] Splash from classpath: {}", CLASSPATH_PATH);
                cir.setReturnValue(data);
                return;
            } else {
                LOG.warn("[ArkCraft] Classpath splash not found at {}", CLASSPATH_PATH);
            }
        } catch (Throwable t) {
            LOG.error("[ArkCraft] Classpath splash failed: {}", t.toString());
        }
        // Let vanilla proceed if both fail.
    }

    @Unique
    private SimpleTexture.TextureData arkcraft$makeData(InputStream in) throws IOException {
        NativeImage img = NativeImage.read(in);
        SPLASH_TEX_W = img.getWidth();
        SPLASH_TEX_H = img.getHeight();
        // No blur (removes "filter"), clamp edges to avoid seams
        return new SimpleTexture.TextureData(new TextureMetadataSection(false, true), img);
    }
}

/* ==========================================
 * 2) Draw the image fullscreen (no "filter")
 * ========================================== */
@OnlyIn(Dist.CLIENT)
@Mixin(targets = "net.minecraft.client.gui.ResourceLoadProgressGui")
class ResourceLoadProgressGuiMixin {
    @Shadow @Final private static ResourceLocation MOJANG_LOGO_TEXTURE;
    @Shadow @Final private Minecraft mc;

    @Shadow private boolean reloading;
    @Shadow private long fadeOutStart;
    @Shadow private long fadeInStart;

    // Run after vanilla finishes its render()
    @Inject(method = "render(Lcom/mojang/blaze3d/matrix/MatrixStack;IIF)V", at = @At("TAIL"))
    private void arkcraft$drawFullscreenSplash(MatrixStack ms, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        // Recompute vanilla fade alpha (f2)
        long now = Util.milliTime();
        float f  = (this.fadeOutStart > -1L) ? (float)(now - this.fadeOutStart) / 1000.0F : -1.0F;
        float f1 = (this.fadeInStart  > -1L) ? (float)(now - this.fadeInStart)  /  500.0F : -1.0F;
        float f2;
        if (f >= 1.0F) {
            f2 = 1.0F - MathHelper.clamp(f - 1.0F, 0.0F, 1.0F);
        } else if (this.reloading) {
            f2 = MathHelper.clamp(f1, 0.0F, 1.0F);
        } else {
            f2 = 1.0F;
        }

        // Bind our (already replaced) texture
        this.mc.getTextureManager().bindTexture(MOJANG_LOGO_TEXTURE);

        // Query actual GL texture size to preserve aspect
        int texW = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
        int texH = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
        if (texW <= 0 || texH <= 0) {
            texW = 512;
            texH = 512;
        }

        // Normal blending (no additive wash)
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, f2);

        int winW = this.mc.getMainWindow().getScaledWidth();
        int winH = this.mc.getMainWindow().getScaledHeight();

        // Cover the whole window while preserving aspect ratio (centered)
        double scale = Math.max((double) winW / texW, (double) winH / texH);
        int drawW = (int)Math.ceil(texW * scale);
        int drawH = (int)Math.ceil(texH * scale);
        int x = (winW - drawW) / 2;
        int y = (winH - drawH) / 2;

        // Draw full UVs
        ((ResourceLoadProgressGui)(Object)this).blit(
                ms,
                x, y,
                drawW, drawH,
                0.0F, 0.0F,
                texW, texH,
                texW, texH
        );

        RenderSystem.disableBlend();
    }
}
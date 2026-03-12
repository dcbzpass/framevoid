package com.framevoid.mixin;

import com.framevoid.window.WindowManager;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Window.class)
public class WindowMixin {

    @Inject(method = "setMode", at = @At("TAIL"))
    private void onSetMode(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.gui == null) return;

        boolean nowFullscreen = mc.getWindow().isFullscreen();
        long handle = GLFW.glfwGetCurrentContext();

        if (nowFullscreen && !WindowManager.isBorderless()) {
            WindowManager.applyBorderless(handle);
            mc.gui.setOverlayMessage(Component.literal("Borderless Fullscreen: ON"), false);
        } else if (!nowFullscreen && WindowManager.isBorderless()) {
            WindowManager.restoreWindow(handle);
            mc.gui.setOverlayMessage(Component.literal("Borderless Fullscreen: OFF"), false);
        }
    }
}
package com.framevoid.mixin;

import com.framevoid.window.WindowManager;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Window.class)
public class WindowMixin {

    @Inject(method = "setMode", at = @At("HEAD"))
    private void onSetModeHead(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getWindow() == null) return;

        boolean currentlyFullscreen = mc.getWindow().isFullscreen();
        if (!currentlyFullscreen && !WindowManager.isBorderless()) {
            WindowManager.saveState();
        }
    }

    @Inject(method = "setMode", at = @At("TAIL"))
    private void onSetModeTail(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.gui == null) return;

        boolean nowFullscreen = mc.getWindow().isFullscreen();

        if (nowFullscreen && !WindowManager.isBorderless()) {
            WindowManager.applyBorderless();
            mc.gui.setOverlayMessage(Component.translatable("framevoid.status.on"), false);
        } else if (!nowFullscreen && WindowManager.isBorderless()) {
            WindowManager.restoreWindow();
            mc.gui.setOverlayMessage(Component.translatable("framevoid.status.off"), false);
        }
    }

    @Inject(method = "onResize", at = @At("TAIL"))
    private void onResizeTail(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getWindow() == null) return;

        int w = mc.getWindow().getWidth();
        int h = mc.getWindow().getHeight();
        WindowManager.updateSavedSize(w, h);
    }
}
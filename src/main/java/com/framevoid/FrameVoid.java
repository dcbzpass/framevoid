package com.framevoid;

import com.framevoid.config.FrameVoidConfig;
import com.framevoid.window.WindowManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.lwjgl.glfw.GLFW;

public class FrameVoid implements ClientModInitializer {

    public static final String MOD_ID = "framevoid";

    @Override
    public void onInitializeClient() {
        FrameVoidConfig.getInstance();

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            long handle = GLFW.glfwGetCurrentContext();
            GLFW.glfwSetWindowPosCallback(handle, (win, x, y) -> WindowManager.updateSavedPos(x, y));
        });

        ClientTickEvents.END_CLIENT_TICK.register(new ClientTickEvents.EndTick() {
            private boolean applied = false;

            @Override
            public void onEndTick(net.minecraft.client.Minecraft client) {
                if (!applied) {
                    applied = true;
                    WindowManager.saveState();
                    WindowManager.applyBorderless();
                }
            }
        });
    }
}
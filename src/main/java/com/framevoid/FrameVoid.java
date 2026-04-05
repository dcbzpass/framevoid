package com.framevoid;

import com.framevoid.window.WindowManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import org.lwjgl.glfw.GLFW;

public class FrameVoid implements ClientModInitializer {

    public static final String MOD_ID = "framevoid";

    @Override
    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            long handle = GLFW.glfwGetCurrentContext();
            GLFW.glfwSetWindowPosCallback(handle, (win, x, y) -> WindowManager.updateSavedPos(x, y));

            WindowManager.saveState();
            WindowManager.applyBorderless();
        });
    }
}
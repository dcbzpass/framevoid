package com.framevoid;

import com.framevoid.config.FrameVoidConfig;
import net.fabricmc.api.ClientModInitializer;

public class FrameVoid implements ClientModInitializer {

    public static final String MOD_ID = "framevoid";

    @Override
    public void onInitializeClient() {
        FrameVoidConfig.getInstance();
    }
}
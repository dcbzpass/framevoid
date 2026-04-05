package com.framevoid.config;

import com.framevoid.window.MonitorDetector;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class ConfigScreen extends Screen {

    private final Screen parent;
    private final FrameVoidConfig config;

    public ConfigScreen(Screen parent) {
        super(Component.translatable("framevoid.config.title"));
        this.parent = parent;
        this.config = FrameVoidConfig.getInstance();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = this.height / 4;

        this.addRenderableWidget(
                Button.builder(
                                buildMonitorLabel(),
                                button -> {
                                    int next = (config.getMonitorIndex() + 1) % Math.max(1, MonitorDetector.getMonitorCount());
                                    config.setMonitorIndex(next);
                                    config.save();
                                    button.setMessage(buildMonitorLabel());
                                })
                        .bounds(centerX - 100, startY, 200, 20)
                        .tooltip(Tooltip.create(
                                Component.translatable("framevoid.config.monitorIndex.tooltip")))
                        .build()
        );

        this.addRenderableWidget(
                Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
                        .bounds(centerX - 100, startY + 120, 200, 20)
                        .build()
        );
    }

    private Component buildMonitorLabel() {
        String name = MonitorDetector.getMonitorName(config.getMonitorIndex());
        return Component.translatable("framevoid.config.monitorIndex").append(": " + name);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }
}
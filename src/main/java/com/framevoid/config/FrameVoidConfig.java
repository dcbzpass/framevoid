package com.framevoid.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class FrameVoidConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("framevoid.json");
    private static FrameVoidConfig instance;

    private boolean autoApply = false;
    private int monitorIndex = 0;

    public static FrameVoidConfig getInstance() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    public static FrameVoidConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                FrameVoidConfig loaded = GSON.fromJson(reader, FrameVoidConfig.class);
                if (loaded != null) {
                    instance = loaded;
                    return instance;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        instance = new FrameVoidConfig();
        instance.save();
        return instance;
    }

    public void save() {
        try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isAutoApply() {
        return autoApply;
    }

    public void setAutoApply(boolean autoApply) {
        this.autoApply = autoApply;
    }

    public int getMonitorIndex() {
        return monitorIndex;
    }

    public void setMonitorIndex(int monitorIndex) {
        this.monitorIndex = monitorIndex;
    }
}

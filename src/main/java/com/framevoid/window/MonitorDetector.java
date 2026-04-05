package com.framevoid.window;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MonitorDetector {

    private static List<String> cachedMonitorNames = null;

    public static int[] getMonitorResolution(int monitorIndex) {
        PointerBuffer monitors = GLFW.glfwGetMonitors();
        if (monitors == null || monitors.limit() == 0) {
            return getPrimaryMonitorResolution();
        }
        int clampedIndex = Math.clamp(monitorIndex, 0, monitors.limit() - 1);
        long monitor = monitors.get(clampedIndex);
        GLFWVidMode vidMode = GLFW.glfwGetVideoMode(monitor);
        if (vidMode == null) {
            return getPrimaryMonitorResolution();
        }
        return new int[]{vidMode.width(), vidMode.height()};
    }

    public static int[] getMonitorPosition(int monitorIndex) {
        PointerBuffer monitors = GLFW.glfwGetMonitors();
        if (monitors == null || monitors.limit() == 0) {
            return new int[]{0, 0};
        }
        int clampedIndex = Math.clamp(monitorIndex, 0, monitors.limit() - 1);
        long monitor = monitors.get(clampedIndex);
        int[] posX = new int[1];
        int[] posY = new int[1];
        GLFW.glfwGetMonitorPos(monitor, posX, posY);
        return new int[]{posX[0], posY[0]};
    }

    public static int getMonitorCount() {
        PointerBuffer monitors = GLFW.glfwGetMonitors();
        if (monitors == null) {
            return 1;
        }
        return monitors.limit();
    }

    public static String getMonitorName(int monitorIndex) {
        List<String> names = resolveMonitorNames();
        if (monitorIndex >= 0 && monitorIndex < names.size()) {
            return names.get(monitorIndex);
        }
        return glfwFallbackName(monitorIndex);
    }

    private static List<String> resolveMonitorNames() {
        if (cachedMonitorNames != null) {
            return cachedMonitorNames;
        }
        cachedMonitorNames = new ArrayList<>();

        if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
            cachedMonitorNames = readWindowsMonitorNames();
        }

        if (cachedMonitorNames.isEmpty()) {
            int count = getMonitorCount();
            for (int i = 0; i < count; i++) {
                cachedMonitorNames.add(glfwFallbackName(i));
            }
        }

        return cachedMonitorNames;
    }

    private static List<String> readWindowsMonitorNames() {
        List<String> names = new ArrayList<>();
        try {
            // List immediate subkeys of DISPLAY - each is one monitor device
            Process listProc = new ProcessBuilder(
                    "reg", "query",
                    "HKLM\\SYSTEM\\CurrentControlSet\\Enum\\DISPLAY"
            ).redirectErrorStream(true).start();

            List<String> monitorSubkeys = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(listProc.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Enum\\DISPLAY\\")) {
                        monitorSubkeys.add(line.replace("HKEY_LOCAL_MACHINE\\", "HKLM\\"));
                    }
                }
            }
            listProc.waitFor();

            for (String subkey : monitorSubkeys) {
                // Query FriendlyName one level deep under this monitor subkey
                Process queryProc = new ProcessBuilder(
                        "reg", "query", subkey, "/s", "/v", "FriendlyName"
                ).redirectErrorStream(true).start();

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(queryProc.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.startsWith("FriendlyName") && line.contains("REG_SZ")) {
                            String raw = line.substring(line.lastIndexOf("REG_SZ") + 6).trim();
                            String extracted = extractModelName(raw);
                            if (extracted != null) {
                                names.add(extracted);
                                break;
                            }
                        }
                    }
                }
                queryProc.waitFor();
            }
        } catch (Exception e) {
            // fall through to GLFW fallback
        }
        return names;
    }

    /**
     * The FriendlyName registry value looks like:
     *   @System32\drivers\dxgkrnl.sys,#303;Generic Monitor (XF240Y X1)
     * We want only the part inside the last parentheses: "XF240Y X1"
     * If there are no parentheses, return the raw value as-is.
     */
    private static String extractModelName(String raw) {
        if (raw == null || raw.isBlank()) return null;
        int open = raw.lastIndexOf('(');
        int close = raw.lastIndexOf(')');
        if (open >= 0 && close > open) {
            String model = raw.substring(open + 1, close).trim();
            if (!model.isBlank()) {
                return model;
            }
        }
        // No parentheses — take everything after the last semicolon if present
        int semi = raw.lastIndexOf(';');
        if (semi >= 0) {
            String after = raw.substring(semi + 1).trim();
            if (!after.isBlank()) return after;
        }
        return raw.trim();
    }

    private static String glfwFallbackName(int monitorIndex) {
        PointerBuffer monitors = GLFW.glfwGetMonitors();
        if (monitors == null || monitors.limit() == 0) {
            return "Display " + monitorIndex;
        }
        int clampedIndex = Math.clamp(monitorIndex, 0, monitors.limit() - 1);
        long monitor = monitors.get(clampedIndex);
        String name = GLFW.glfwGetMonitorName(monitor);
        return (name != null && !name.isBlank()) ? name : "Display " + monitorIndex;
    }

    private static int[] getPrimaryMonitorResolution() {
        long primary = GLFW.glfwGetPrimaryMonitor();
        GLFWVidMode vidMode = GLFW.glfwGetVideoMode(primary);
        if (vidMode == null) {
            return new int[]{1920, 1080};
        }
        return new int[]{vidMode.width(), vidMode.height()};
    }
}
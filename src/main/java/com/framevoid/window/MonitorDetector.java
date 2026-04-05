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
        List<String> names = getEdidMonitorNames();
        if (monitorIndex >= 0 && monitorIndex < names.size()) {
            return names.get(monitorIndex);
        }
        return glfwMonitorName(monitorIndex);
    }

    private static List<String> getEdidMonitorNames() {
        if (cachedMonitorNames != null) {
            return cachedMonitorNames;
        }

        cachedMonitorNames = new ArrayList<>();

        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) {
            cachedMonitorNames = queryWindowsMonitorNames();
        }

        if (cachedMonitorNames.isEmpty()) {
            int count = getMonitorCount();
            for (int i = 0; i < count; i++) {
                cachedMonitorNames.add(glfwMonitorName(i));
            }
        }

        return cachedMonitorNames;
    }

    private static List<String> queryWindowsMonitorNames() {
        List<String> names = new ArrayList<>();
        try {
            Process process = new ProcessBuilder(
                    "reg", "query",
                    "HKLM\\SYSTEM\\CurrentControlSet\\Enum\\DISPLAY",
                    "/s", "/v", "DeviceDesc"
            ).redirectErrorStream(true).start();

            List<String> descs = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.contains("DeviceDesc") && line.contains("REG_SZ")) {
                        String value = line.substring(line.lastIndexOf("REG_SZ") + 6).trim();
                        int parenOpen = value.lastIndexOf('(');
                        int parenClose = value.lastIndexOf(')');
                        if (parenOpen >= 0 && parenClose > parenOpen) {
                            String model = value.substring(parenOpen + 1, parenClose).trim();
                            if (!model.isBlank() && !descs.contains(model)) {
                                descs.add(model);
                            }
                        }
                    }
                }
            }
            process.waitFor();

            int glfwCount = getMonitorCount();
            for (int i = 0; i < glfwCount; i++) {
                if (i < descs.size()) {
                    names.add(descs.get(i));
                } else {
                    names.add(glfwMonitorName(i));
                }
            }
        } catch (Exception e) {
            // fall through to GLFW names
        }
        return names;
    }

    private static String glfwMonitorName(int monitorIndex) {
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
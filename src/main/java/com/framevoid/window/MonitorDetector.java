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
            cachedMonitorNames = readWindowsDisplayNames();
        }

        if (cachedMonitorNames.isEmpty()) {
            int count = getMonitorCount();
            for (int i = 0; i < count; i++) {
                cachedMonitorNames.add(glfwFallbackName(i));
            }
        }

        return cachedMonitorNames;
    }

    /**
     * Reads monitor friendly names from the Windows registry.
     * HKLM\SYSTEM\CurrentControlSet\Enum\DISPLAY has one subkey per connected monitor.
     * Each subkey's FriendlyName value is the human-readable name shown in Device Manager.
     */
    private static List<String> readWindowsDisplayNames() {
        List<String> names = new ArrayList<>();
        try {
            // First, list immediate subkeys of DISPLAY — one per monitor model
            Process listProc = new ProcessBuilder(
                    "reg", "query",
                    "HKLM\\SYSTEM\\CurrentControlSet\\Enum\\DISPLAY"
            ).redirectErrorStream(true).start();

            List<String> subkeys = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(listProc.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Enum\\DISPLAY\\")) {
                        subkeys.add(line);
                    }
                }
            }
            listProc.waitFor();

            // For each monitor subkey, query FriendlyName recursively
            for (String subkey : subkeys) {
                String regPath = subkey.replace("HKEY_LOCAL_MACHINE\\", "HKLM\\");
                Process queryProc = new ProcessBuilder(
                        "reg", "query", regPath, "/s", "/v", "FriendlyName"
                ).redirectErrorStream(true).start();

                String found = null;
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(queryProc.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.startsWith("FriendlyName") && line.contains("REG_SZ")) {
                            String value = line.substring(line.lastIndexOf("REG_SZ") + 6).trim();
                            if (!value.isBlank()) {
                                found = value;
                                break;
                            }
                        }
                    }
                }
                queryProc.waitFor();

                if (found != null) {
                    names.add(found);
                }
            }
        } catch (Exception e) {
            // fall through to GLFW fallback
        }
        return names;
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
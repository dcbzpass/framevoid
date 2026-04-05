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
        return fallbackName(monitorIndex);
    }

    private static List<String> resolveMonitorNames() {
        if (cachedMonitorNames != null) {
            return cachedMonitorNames;
        }
        cachedMonitorNames = new ArrayList<>();

        if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
            cachedMonitorNames = queryWmiMonitorNames();
        }

        if (cachedMonitorNames.isEmpty()) {
            int count = getMonitorCount();
            for (int i = 0; i < count; i++) {
                cachedMonitorNames.add(fallbackName(i));
            }
        }

        return cachedMonitorNames;
    }

    private static List<String> queryWmiMonitorNames() {
        List<String> names = new ArrayList<>();
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "powershell", "-NoProfile", "-NonInteractive", "-Command",
                    "Get-WmiObject -Namespace root\\wmi -Class WmiMonitorID | ForEach-Object { " +
                            "  $n = ($_.UserFriendlyName | Where-Object {$_ -ne 0} | ForEach-Object {[char]$_}) -join ''; " +
                            "  if ($n) { $n } else { 'Display' }" +
                            "}"
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isBlank()) {
                        names.add(line);
                    }
                }
            }
            process.waitFor();
        } catch (Exception e) {
            // fall through, caller will use GLFW fallback
        }
        return names;
    }

    private static String fallbackName(int monitorIndex) {
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
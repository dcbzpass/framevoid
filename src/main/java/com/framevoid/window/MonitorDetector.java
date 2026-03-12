package com.framevoid.window;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;

public class MonitorDetector {

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

    private static int[] getPrimaryMonitorResolution() {
        long primary = GLFW.glfwGetPrimaryMonitor();
        GLFWVidMode vidMode = GLFW.glfwGetVideoMode(primary);
        if (vidMode == null) {
            return new int[]{1920, 1080};
        }
        return new int[]{vidMode.width(), vidMode.height()};
    }
}

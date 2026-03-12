package com.framevoid.window;

import org.lwjgl.glfw.GLFW;

public class WindowManager {

    private static boolean borderless = false;

    public static boolean isBorderless() {
        return borderless;
    }

    public static void applyBorderless(long handle) {
        int[] monitorPos = MonitorDetector.getMonitorPosition(0);
        int[] resolution = MonitorDetector.getMonitorResolution(0);

        GLFW.glfwSetWindowMonitor(handle, 0L,
                monitorPos[0], monitorPos[1],
                resolution[0], resolution[1],
                GLFW.GLFW_DONT_CARE);

        GLFW.glfwSetWindowAttrib(handle, GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE);
        GLFW.glfwSetWindowAttrib(handle, GLFW.GLFW_AUTO_ICONIFY, GLFW.GLFW_FALSE);

        borderless = true;
    }

    public static void restoreWindow(long handle) {
        int[] monitorPos = MonitorDetector.getMonitorPosition(0);

        GLFW.glfwSetWindowMonitor(handle, 0L,
                monitorPos[0] + 80, monitorPos[1] + 80,
                1280, 720,
                GLFW.GLFW_DONT_CARE);

        GLFW.glfwSetWindowAttrib(handle, GLFW.GLFW_DECORATED, GLFW.GLFW_TRUE);
        GLFW.glfwSetWindowAttrib(handle, GLFW.GLFW_AUTO_ICONIFY, GLFW.GLFW_TRUE);

        borderless = false;
    }
}
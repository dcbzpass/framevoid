package com.framevoid.window;

import com.framevoid.config.FrameVoidConfig;
import org.lwjgl.glfw.GLFW;

public class WindowManager {

    private static boolean borderless = false;
    private static int savedX, savedY, savedWidth, savedHeight;
    private static boolean hasSavedState = false;

    public static boolean isBorderless() {
        return borderless;
    }

    public static void saveState(long handle) {
        int[] x = new int[1], y = new int[1], w = new int[1], h = new int[1];
        GLFW.glfwGetWindowPos(handle, x, y);
        GLFW.glfwGetWindowSize(handle, w, h);
        savedX = x[0]; savedY = y[0]; savedWidth = w[0]; savedHeight = h[0];
        hasSavedState = true;
    }

    public static void applyBorderless(long handle) {
        int monitorIndex = FrameVoidConfig.getInstance().getMonitorIndex();
        int[] monitorPos = MonitorDetector.getMonitorPosition(monitorIndex);
        int[] resolution = MonitorDetector.getMonitorResolution(monitorIndex);

        GLFW.glfwSetWindowMonitor(handle, 0L,
                monitorPos[0], monitorPos[1],
                resolution[0], resolution[1],
                GLFW.GLFW_DONT_CARE);
        GLFW.glfwSetWindowAttrib(handle, GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE);
        GLFW.glfwSetWindowAttrib(handle, GLFW.GLFW_AUTO_ICONIFY, GLFW.GLFW_FALSE);

        borderless = true;
    }

    public static void restoreWindow(long handle) {
        int x = hasSavedState ? savedX : 80;
        int y = hasSavedState ? savedY : 80;
        int w = hasSavedState ? savedWidth : 1280;
        int h = hasSavedState ? savedHeight : 720;

        GLFW.glfwSetWindowMonitor(handle, 0L, x, y, w, h, GLFW.GLFW_DONT_CARE);
        GLFW.glfwSetWindowAttrib(handle, GLFW.GLFW_DECORATED, GLFW.GLFW_TRUE);
        GLFW.glfwSetWindowAttrib(handle, GLFW.GLFW_AUTO_ICONIFY, GLFW.GLFW_TRUE);

        borderless = false;
        hasSavedState = false;
    }
}
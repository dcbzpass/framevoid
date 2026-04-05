package com.framevoid.window;

import com.framevoid.config.FrameVoidConfig;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public class WindowManager {

    private static boolean borderless = false;
    private static final Map<Integer, int[]> savedStatePerMonitor = new HashMap<>();
    private static int lastMonitorIndex = -1;

    public static boolean isBorderless() {
        return borderless;
    }

    private static long getHandle() {
        return GLFW.glfwGetCurrentContext();
    }

    public static void saveState() {
        long handle = getHandle();
        if (handle == 0L) return;

        int monitorIndex = FrameVoidConfig.getInstance().getMonitorIndex();
        int[] x = new int[1], y = new int[1], w = new int[1], h = new int[1];
        GLFW.glfwGetWindowPos(handle, x, y);
        GLFW.glfwGetWindowSize(handle, w, h);
        savedStatePerMonitor.put(monitorIndex, new int[]{x[0], y[0], w[0], h[0]});
        lastMonitorIndex = monitorIndex;
    }

    public static void applyBorderless() {
        long handle = getHandle();
        if (handle == 0L) return;

        int monitorIndex = FrameVoidConfig.getInstance().getMonitorIndex();
        int[] monitorPos = MonitorDetector.getMonitorPosition(monitorIndex);
        int[] resolution = MonitorDetector.getMonitorResolution(monitorIndex);

        GLFW.glfwSetWindowMonitor(handle, 0L,
                monitorPos[0], monitorPos[1],
                resolution[0], resolution[1],
                GLFW.GLFW_DONT_CARE);
        GLFW.glfwSetWindowAttrib(handle, GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE);
        GLFW.glfwSetWindowAttrib(handle, GLFW.GLFW_AUTO_ICONIFY, GLFW.GLFW_FALSE);
        GLFW.glfwSetWindowAttrib(handle, GLFW.GLFW_FLOATING, GLFW.GLFW_FALSE);

        borderless = true;
    }

    public static void restoreWindow() {
        long handle = getHandle();
        if (handle == 0L) return;

        int monitorIndex = lastMonitorIndex >= 0 ? lastMonitorIndex : FrameVoidConfig.getInstance().getMonitorIndex();
        int[] saved = savedStatePerMonitor.get(monitorIndex);

        int x = saved != null ? saved[0] : 80;
        int y = saved != null ? saved[1] : 80;
        int w = saved != null ? saved[2] : 1280;
        int h = saved != null ? saved[3] : 720;

        GLFW.glfwSetWindowMonitor(handle, 0L, x, y, w, h, GLFW.GLFW_DONT_CARE);
        GLFW.glfwSetWindowAttrib(handle, GLFW.GLFW_DECORATED, GLFW.GLFW_TRUE);
        GLFW.glfwSetWindowAttrib(handle, GLFW.GLFW_AUTO_ICONIFY, GLFW.GLFW_TRUE);
        GLFW.glfwSetWindowAttrib(handle, GLFW.GLFW_FLOATING, GLFW.GLFW_FALSE);
        GLFW.glfwFocusWindow(handle);

        borderless = false;
        lastMonitorIndex = -1;
    }

    public static void updateSavedSize(int width, int height) {
        if (borderless) return;
        int monitorIndex = FrameVoidConfig.getInstance().getMonitorIndex();
        int[] existing = savedStatePerMonitor.get(monitorIndex);
        if (existing != null) {
            existing[2] = width;
            existing[3] = height;
        }
    }

    public static void updateSavedPos(int x, int y) {
        if (borderless) return;
        int monitorIndex = FrameVoidConfig.getInstance().getMonitorIndex();
        int[] existing = savedStatePerMonitor.get(monitorIndex);
        if (existing != null) {
            existing[0] = x;
            existing[1] = y;
        }
    }
}
package me.retrotv.bookmanagement.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SystemUtil {
    private static String osName = System.getProperty("os.name").toLowerCase();

    private SystemUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static Object getOperationSystemType() {
        String message = "현재 운영체제: {}";

        if(isWindows()) {
            log.debug(message, OperationSystem.WINDOWS);
            return OperationSystem.WINDOWS;
        } else if(isMac()) {
            log.debug(message, OperationSystem.MACOS);
            return OperationSystem.MACOS;
        } else if(isLinux()) {
            log.debug(message, OperationSystem.LINUX);
            return OperationSystem.LINUX;
        } else if(isUnix()) {
            log.debug(message, OperationSystem.UNIX);
            return OperationSystem.UNIX;
        } else if(isSolaris()) {
            log.debug(message, OperationSystem.SOLARIS);
            return OperationSystem.SOLARIS;
        }

        log.debug(message, OperationSystem.UNKNOWN);
        return OperationSystem.UNKNOWN;
    }

    public static boolean isWindows() {
        return (osName.contains("win"));
    }

    public static boolean isMac() {
        return (osName.contains("mac"));
    }

    public static boolean isLinux() {
        return (osName.contains("linux"));
    }

    public static boolean isUnix() {
        return (osName.contains("nix") || osName.contains("nux") || osName.contains("aix"));
    }

    public static boolean isSolaris() {
        return (osName.contains("sunos"));
    }

    public enum OperationSystem {
        WINDOWS,
        MACOS,
        LINUX,
        UNIX,
        SOLARIS,
        UNKNOWN
    }
}

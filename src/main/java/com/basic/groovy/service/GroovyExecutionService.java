package com.basic.groovy.service;

import com.basic.groovy.model.ScriptResult;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.runtime.StackTraceUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Compiles and evaluates Groovy scripts, capturing every output channel so the
 * caller gets a structured {@link ScriptResult} instead of a raw text dump.
 *
 * <p>A fresh {@link GroovyShell} and {@link Binding} are created per request so
 * concurrent executions never share state. Script output is captured by binding
 * {@code out} and {@code err} writers, which Groovy's {@code println}/{@code print}
 * helpers write to.
 */
@Service
public class GroovyExecutionService {

    public ScriptResult execute(String script) {
        if (!StringUtils.hasText(script)) {
            return ScriptResult.failure("", "", "Nothing to run - the script is empty.", 0L);
        }

        StringWriter stdout = new StringWriter();
        StringWriter stderr = new StringWriter();
        PrintWriter outWriter = new PrintWriter(stdout);
        PrintWriter errWriter = new PrintWriter(stderr);

        Binding binding = new Binding();
        binding.setProperty("out", outWriter);
        binding.setProperty("err", errWriter);

        CompilerConfiguration config = new CompilerConfiguration();
        config.setScriptBaseClass(null);

        GroovyShell shell = new GroovyShell(getClass().getClassLoader(), binding, config);

        long start = System.nanoTime();
        try {
            Object value = shell.evaluate(script, "ConsoleScript");
            long elapsed = millisSince(start);
            outWriter.flush();
            errWriter.flush();

            String returnValue = value == null ? null : String.valueOf(value);
            String returnType = value == null ? null : value.getClass().getName();
            return ScriptResult.ok(stdout.toString(), stderr.toString(), returnValue, returnType, elapsed);
        } catch (Throwable throwable) {
            long elapsed = millisSince(start);
            outWriter.flush();
            errWriter.flush();
            return ScriptResult.failure(stdout.toString(), stderr.toString(), formatError(throwable), elapsed);
        }
    }

    private static long millisSince(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000L;
    }

    /** Frames at/after any of these belong to the host app, not the user's script. */
    private static final String[] HOST_FRAME_PREFIXES = {
            "com.basic.groovy.",
            "jdk.internal.reflect.",
            "java.lang.reflect.",
            "org.springframework.",
            "org.apache.catalina.",
            "org.apache.coyote.",
            "org.apache.tomcat.",
            "jakarta.servlet."
    };

    /**
     * Produces a clean stack trace: Groovy's internal frames are sanitized away
     * and the host application's frames (Spring, Tomcat, this service) are
     * trimmed off, so the user sees only what relates to their own script.
     */
    private static String formatError(Throwable throwable) {
        Throwable sanitized = StackTraceUtils.deepSanitize(throwable);
        trimHostFrames(sanitized);
        StringWriter sw = new StringWriter();
        sanitized.printStackTrace(new PrintWriter(sw));
        return sw.toString().trim();
    }

    private static void trimHostFrames(Throwable throwable) {
        Set<Throwable> seen = new HashSet<>();
        for (Throwable t = throwable; t != null && seen.add(t); t = t.getCause()) {
            StackTraceElement[] frames = t.getStackTrace();
            int cut = frames.length;
            for (int i = 0; i < frames.length; i++) {
                if (isHostFrame(frames[i].getClassName())) {
                    cut = i;
                    break;
                }
            }
            t.setStackTrace(Arrays.copyOf(frames, cut));
        }
    }

    private static boolean isHostFrame(String className) {
        for (String prefix : HOST_FRAME_PREFIXES) {
            if (className.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}

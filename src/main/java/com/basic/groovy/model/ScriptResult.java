package com.basic.groovy.model;

/**
 * Outcome of running a single Groovy script.
 *
 * <p>The fields deliberately separate the different "channels" a script can
 * produce so the UI can render each one in its own place instead of mashing
 * everything into one text blob:
 * <ul>
 *   <li>{@code stdout}      - anything written via {@code println}/{@code print}</li>
 *   <li>{@code stderr}      - anything written via {@code err.println}</li>
 *   <li>{@code returnValue} - the value of the last evaluated expression</li>
 *   <li>{@code returnType}  - the runtime class of that value</li>
 *   <li>{@code error}       - a sanitized stack trace when the script throws</li>
 * </ul>
 *
 * @param success         whether the script ran without throwing
 * @param stdout          captured standard output
 * @param stderr          captured standard error
 * @param returnValue     string form of the script's return value, or {@code null}
 * @param returnType      class name of the return value, or {@code null}
 * @param error           sanitized stack trace when {@code success} is false
 * @param executionTimeMs wall-clock execution time in milliseconds
 */
public record ScriptResult(
        boolean success,
        String stdout,
        String stderr,
        String returnValue,
        String returnType,
        String error,
        long executionTimeMs
) {

    public static ScriptResult ok(String stdout, String stderr, String returnValue,
                                  String returnType, long executionTimeMs) {
        return new ScriptResult(true, stdout, stderr, returnValue, returnType, null, executionTimeMs);
    }

    public static ScriptResult failure(String stdout, String stderr, String error,
                                       long executionTimeMs) {
        return new ScriptResult(false, stdout, stderr, null, null, error, executionTimeMs);
    }
}

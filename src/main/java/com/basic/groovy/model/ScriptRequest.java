package com.basic.groovy.model;

/**
 * Payload posted by the console UI when the user runs a script.
 *
 * @param script the raw Groovy source to evaluate
 */
public record ScriptRequest(String script) {
}

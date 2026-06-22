package com.basic.groovy.service;

import com.basic.groovy.model.ScriptResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GroovyExecutionServiceTests {

    private final GroovyExecutionService service = new GroovyExecutionService();

    @Test
    void capturesStandardOutput() {
        ScriptResult result = service.execute("println 'hi'");

        assertThat(result.success()).isTrue();
        assertThat(result.stdout()).contains("hi");
        assertThat(result.error()).isNull();
    }

    @Test
    void capturesReturnValueAndType() {
        ScriptResult result = service.execute("1 + 2");

        assertThat(result.success()).isTrue();
        assertThat(result.returnValue()).isEqualTo("3");
        assertThat(result.returnType()).isEqualTo("java.lang.Integer");
    }

    @Test
    void capturesStandardError() {
        ScriptResult result = service.execute("err.println 'boom'");

        assertThat(result.success()).isTrue();
        assertThat(result.stderr()).contains("boom");
    }

    @Test
    void reportsExceptionsWithoutCrashing() {
        ScriptResult result = service.execute("throw new IllegalStateException('nope')");

        assertThat(result.success()).isFalse();
        assertThat(result.error()).contains("IllegalStateException").contains("nope");
        // host/framework frames should be trimmed away
        assertThat(result.error())
                .doesNotContain("org.springframework")
                .doesNotContain("com.basic.groovy.service");
    }

    @Test
    void rejectsEmptyScript() {
        ScriptResult result = service.execute("   ");

        assertThat(result.success()).isFalse();
        assertThat(result.error()).contains("empty");
    }
}

package com.basic.groovy.groovy.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.util.StringUtils;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScriptResult {

    public String[] output;
    public Object result;

    public ScriptResult() {
    }

    public String[] getOutput() {
        return output;
    }

    public Object getResult() {
        return result;
    }

    public static ScriptResult create(Throwable throwable) {
        String message = throwable.getMessage() == null ? throwable.getClass().getName() : throwable.getMessage();
        return create(null, message);
    }

    public static ScriptResult create(Object result, String output) {
        ScriptResult scriptletResult = new ScriptResult();
        scriptletResult.result = result;
        if (StringUtils.hasLength(output)) {
            scriptletResult.output = output.split("\\R");
        }
        return scriptletResult;
    }
}
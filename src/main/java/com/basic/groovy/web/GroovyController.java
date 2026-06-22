package com.basic.groovy.web;

import com.basic.groovy.model.ScriptRequest;
import com.basic.groovy.model.ScriptResult;
import com.basic.groovy.service.GroovyExecutionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/groovy")
public class GroovyController {

    private final GroovyExecutionService executionService;

    public GroovyController(GroovyExecutionService executionService) {
        this.executionService = executionService;
    }

    /** Serves the single-page console UI. */
    @GetMapping
    public String console() {
        return "groovy-console";
    }

    /** Executes a script submitted by the UI and returns a structured result as JSON. */
    @PostMapping("/api/execute")
    @ResponseBody
    public ScriptResult execute(@RequestBody ScriptRequest request) {
        return executionService.execute(request.script());
    }
}

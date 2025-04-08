package com.basic.groovy.groovy.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller; // Change back to Controller
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam; // Use RequestParam for form data

import javax.script.*;
import java.io.PrintWriter;
import java.io.StringWriter;

@Controller
@RequestMapping("/groovy")
public class GroovyController {

    @Autowired
    private ApplicationContext context;

    private final ScriptEngineManager manager = new ScriptEngineManager();
    private final ScriptEngine engine = manager.getEngineByName("groovy");

    // GET request handler to show the initial console page
    @GetMapping()
    public String showConsole(Model model) {
        if (engine == null) {
            model.addAttribute("error", "Groovy ScriptEngine not found. Is Groovy dependency added?");
            model.addAttribute("script", ""); // Provide empty script for textarea
        } else {
            // Provide a default sample script for the initial view
            model.addAttribute("script", "println 'Hello from Groovy via Thymeleaf!'\nreturn 'Script executed successfully.'");
        }
        // Provide initial placeholder for result
        model.addAttribute("result", "// Output will appear here");
        // Return the logical view name (Thymeleaf template name without .html)
        return "groovy-console";
    }

    // POST request handler to execute the script from the form
    @PostMapping("/execute")
    public String executeScript(@RequestParam(defaultValue = "") String script, Model model) { // Get script from form parameter

        if (engine == null) {
            model.addAttribute("error", "Groovy ScriptEngine not found.");
            model.addAttribute("script", script); // Keep submitted script in textarea
            model.addAttribute("result", "// Execution failed");
            return "groovy-console"; // Re-render the view with error
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        StringWriter errorSw = new StringWriter();
        PrintWriter errorPw = new PrintWriter(errorSw);
        StringBuilder resultOutput = new StringBuilder();
        Object scriptResult = null;
        Exception scriptException = null;

        try {
            Bindings bindings = engine.createBindings();
            bindings.put("out", pw);
            bindings.put("err", errorPw);
            bindings.put("spring", context); // Use with caution!

            // Optional: Expose specific beans
            // try {
            //     SomeService myService = context.getBean(SomeService.class);
            //     bindings.put("myService", myService);
            // } catch (Exception e) {
            //     errorPw.println("Could not get bean 'myService': " + e.getMessage());
            // }

            scriptResult = engine.eval(script, bindings);

        } catch (Exception e) {
            scriptException = e;
        } finally {
            pw.flush();
            errorPw.flush();
        }

        // Build the result string (same logic as before)

        if ( StringUtils.hasText(sw.toString())) {
            resultOutput.append("--- Standard Output ---\n");
            resultOutput.append(sw);
        }

        if (scriptResult != null && StringUtils.hasText(scriptResult.toString())) {
            resultOutput.append("\n--- Return Value ---\n");
            resultOutput.append( scriptResult);
        }
        if (StringUtils.hasText(errorSw.toString())) {
            resultOutput.append("\n--- Standard Error ---\n");
            resultOutput.append(errorSw);
        }

        if (scriptException != null) {
            resultOutput.append("\n--- Exception ---\n");
            StringWriter exceptionSw = new StringWriter();
            scriptException.printStackTrace(new PrintWriter(exceptionSw));
            resultOutput.append(exceptionSw.toString());
        }

        // Pass data back to the view via the Model
        model.addAttribute("script", script); // Keep the executed script in the textarea
        model.addAttribute("result", resultOutput.toString()); // Add the execution result

        // Return the logical view name to re-render the page
        return "groovy-console";
    }
}
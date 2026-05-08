package edu.curtin.saed.assignment2;

import org.python.util.PythonInterpreter;

public class ScriptHandler {
    public void runScript(ApiInterface api, String pythonScript) {
        try (PythonInterpreter interpreter = new PythonInterpreter()) {
            interpreter.set("api", api);
            interpreter.exec(pythonScript);
        }
    }
} 

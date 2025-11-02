package com.mcp.tools.mdd_tools.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class MathematicalTools {
    @Tool(name = "add", description = "Adds two numbers and returns the sum")
    public double add(double a, double b) {
        return a + b;
    }

    @Tool(name = "subtract", description = "Subtracts the second number from the first and returns the result")
    public double subtract(double a, double b) {
        return a - b;
    }

}

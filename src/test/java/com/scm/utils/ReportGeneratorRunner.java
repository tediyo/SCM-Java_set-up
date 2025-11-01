package com.scm.utils;

/**
 * Utility class to run the custom report generator.
 * This can be executed after tests complete.
 */
public class ReportGeneratorRunner {
    
    public static void main(String[] args) {
        String jsonReportPath = "target/cucumber-reports/cucumber.json";
        String outputPath = "target/cucumber-reports/test-report-two.html";
        
        // Allow custom paths via command line arguments
        if (args.length >= 1) {
            jsonReportPath = args[0];
        }
        if (args.length >= 2) {
            outputPath = args[1];
        }
        
        System.out.println("🚀 Generating fancy HTML report...");
        System.out.println("   Input JSON: " + jsonReportPath);
        System.out.println("   Output HTML: " + outputPath);
        
        ReportGenerator.generateFancyHTMLReport(jsonReportPath, outputPath);
        
        System.out.println("✅ Report generation complete!");
    }
    
    /**
     * Generate report using default paths.
     */
    public static void generateReport() {
        ReportGenerator.generateFancyHTMLReport(
            "target/cucumber-reports/cucumber.json",
            "target/cucumber-reports/test-report-two.html"
        );
    }
}


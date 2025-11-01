package com.scm.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ReportGenerator {
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public static void generateFancyHTMLReport(String jsonReportPath, String outputPath) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            File jsonFile = new File(jsonReportPath);
            
            if (!jsonFile.exists()) {
                System.err.println("JSON report not found: " + jsonReportPath);
                return;
            }
            
            JsonNode rootNode = mapper.readTree(jsonFile);
            String html = generateHTML(rootNode);
            
            // Ensure directory exists
            File outputFile = new File(outputPath);
            outputFile.getParentFile().mkdirs();
            
            try (FileWriter writer = new FileWriter(outputFile)) {
                writer.write(html);
            }
            
            System.out.println("✨ Fancy HTML report generated: " + outputPath);
        } catch (IOException e) {
            System.err.println("Error generating report: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static String generateHTML(JsonNode rootNode) {
        StringBuilder html = new StringBuilder();
        
        // Calculate statistics
        ReportStats stats = calculateStats(rootNode);
        List<FeatureData> features = parseFeatures(rootNode);
        
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"en\">\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <title>Test Report - Interactive Dashboard</title>\n");
        html.append(generateStyles());
        html.append("</head>\n");
        html.append("<body>\n");
        
        // Header
        html.append(generateHeader(stats));
        
        // Statistics Dashboard
        html.append(generateStatsDashboard(stats));
        
        // Filters
        html.append(generateFilters());
        
        // Features Section
        html.append(generateFeaturesSection(features));
        
        // Footer
        html.append(generateFooter());
        
        html.append(generateScripts());
        html.append("</body>\n");
        html.append("</html>");
        
        return html.toString();
    }
    
    private static String generateStyles() {
        StringBuilder sb = new StringBuilder();
        sb.append("            <style>\n");
        sb.append("                * { margin: 0; padding: 0; box-sizing: border-box; }\n");
        sb.append("                body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; ");
        sb.append("background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); min-height: 100vh; ");
        sb.append("padding: 20px; color: #333; }\n");
        sb.append("                .container { max-width: 1400px; margin: 0 auto; }\n");
        sb.append("                .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); ");
        sb.append("color: white; padding: 40px; border-radius: 15px; margin-bottom: 30px; ");
        sb.append("box-shadow: 0 10px 40px rgba(0,0,0,0.3); text-align: center; animation: slideDown 0.6s ease-out; }\n");
        sb.append("                @keyframes slideDown { from { transform: translateY(-50px); opacity: 0; } ");
        sb.append("to { transform: translateY(0); opacity: 1; } }\n");
        sb.append("                .header h1 { font-size: 3em; margin-bottom: 10px; text-shadow: 2px 2px 4px rgba(0,0,0,0.2); }\n");
        sb.append("                .header p { font-size: 1.2em; opacity: 0.9; }\n");
        sb.append("                .stats-dashboard { display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); ");
        sb.append("gap: 20px; margin-bottom: 30px; }\n");
        sb.append("                .stat-card { background: white; padding: 30px; border-radius: 15px; ");
        sb.append("box-shadow: 0 5px 20px rgba(0,0,0,0.1); text-align: center; ");
        sb.append("transition: transform 0.3s ease, box-shadow 0.3s ease; animation: fadeInUp 0.6s ease-out; ");
        sb.append("animation-fill-mode: backwards; }\n");
        sb.append("                .stat-card:nth-child(1) { animation-delay: 0.1s; }\n");
        sb.append("                .stat-card:nth-child(2) { animation-delay: 0.2s; }\n");
        sb.append("                .stat-card:nth-child(3) { animation-delay: 0.3s; }\n");
        sb.append("                .stat-card:nth-child(4) { animation-delay: 0.4s; }\n");
        sb.append("                .stat-card:nth-child(5) { animation-delay: 0.5s; }\n");
        sb.append("                @keyframes fadeInUp { from { transform: translateY(30px); opacity: 0; } ");
        sb.append("to { transform: translateY(0); opacity: 1; } }\n");
        sb.append("                .stat-card:hover { transform: translateY(-10px); box-shadow: 0 10px 30px rgba(0,0,0,0.2); }\n");
        sb.append("                .stat-card .icon { font-size: 3em; margin-bottom: 15px; }\n");
        sb.append("                .stat-card .value { font-size: 2.5em; font-weight: bold; margin-bottom: 10px; }\n");
        sb.append("                .stat-card .label { font-size: 1.1em; color: #666; text-transform: uppercase; letter-spacing: 1px; }\n");
        sb.append("                .stat-card.passed { border-top: 5px solid #4caf50; }\n");
        sb.append("                .stat-card.failed { border-top: 5px solid #f44336; }\n");
        sb.append("                .stat-card.skipped { border-top: 5px solid #ff9800; }\n");
        sb.append("                .stat-card.total { border-top: 5px solid #2196f3; }\n");
        sb.append("                .stat-card.percentage { border-top: 5px solid #9c27b0; }\n");
        sb.append("                .filters { background: white; padding: 20px; border-radius: 15px; margin-bottom: 30px; ");
        sb.append("box-shadow: 0 5px 20px rgba(0,0,0,0.1); display: flex; gap: 15px; flex-wrap: wrap; align-items: center; }\n");
        sb.append("                .filter-btn { padding: 12px 24px; border: 2px solid #667eea; background: white; ");
        sb.append("color: #667eea; border-radius: 25px; cursor: pointer; font-size: 1em; font-weight: 600; ");
        sb.append("transition: all 0.3s ease; }\n");
        sb.append("                .filter-btn:hover { background: #667eea; color: white; transform: scale(1.05); }\n");
        sb.append("                .filter-btn.active { background: #667eea; color: white; }\n");
        sb.append("                .search-box { flex: 1; min-width: 200px; padding: 12px 20px; border: 2px solid #e0e0e0; ");
        sb.append("border-radius: 25px; font-size: 1em; transition: border-color 0.3s ease; }\n");
        sb.append("                .search-box:focus { outline: none; border-color: #667eea; }\n");
        sb.append("                .features-section { display: flex; flex-direction: column; gap: 20px; }\n");
        sb.append("                .feature-card { background: white; border-radius: 15px; box-shadow: 0 5px 20px rgba(0,0,0,0.1); ");
        sb.append("overflow: hidden; transition: all 0.3s ease; }\n");
        sb.append("                .feature-card:hover { box-shadow: 0 10px 30px rgba(0,0,0,0.15); }\n");
        sb.append("                .feature-header { padding: 25px; cursor: pointer; display: flex; ");
        sb.append("justify-content: space-between; align-items: center; ");
        sb.append("background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%); transition: background 0.3s ease; }\n");
        sb.append("                .feature-header:hover { background: linear-gradient(135deg, #e8ecf1 0%, #b5c4d8 100%); }\n");
        sb.append("                .feature-title { font-size: 1.5em; font-weight: bold; color: #333; }\n");
        sb.append("                .feature-stats { display: flex; gap: 20px; align-items: center; }\n");
        sb.append("                .feature-stat { display: flex; align-items: center; gap: 5px; padding: 5px 15px; ");
        sb.append("border-radius: 20px; font-weight: 600; }\n");
        sb.append("                .feature-stat.passed { background: #e8f5e9; color: #2e7d32; }\n");
        sb.append("                .feature-stat.failed { background: #ffebee; color: #c62828; }\n");
        sb.append("                .feature-stat.skipped { background: #fff3e0; color: #e65100; }\n");
        sb.append("                .expand-icon { font-size: 1.5em; transition: transform 0.3s ease; }\n");
        sb.append("                .feature-card.expanded .expand-icon { transform: rotate(180deg); }\n");
        sb.append("                .feature-content { max-height: 0; overflow: hidden; transition: max-height 0.5s ease; }\n");
        sb.append("                .feature-card.expanded .feature-content { max-height: 5000px; }\n");
        sb.append("                .scenarios-list { padding: 20px; }\n");
        sb.append("                .scenario-item { background: #f9f9f9; border-left: 4px solid #ddd; padding: 20px; ");
        sb.append("margin-bottom: 15px; border-radius: 8px; transition: all 0.3s ease; }\n");
        sb.append("                .scenario-item:hover { background: #f0f0f0; transform: translateX(5px); }\n");
        sb.append("                .scenario-item.passed { border-left-color: #4caf50; }\n");
        sb.append("                .scenario-item.failed { border-left-color: #f44336; }\n");
        sb.append("                .scenario-item.skipped { border-left-color: #ff9800; }\n");
        sb.append("                .scenario-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }\n");
        sb.append("                .scenario-name { font-size: 1.2em; font-weight: 600; color: #333; }\n");
        sb.append("                .scenario-duration { color: #666; font-size: 0.9em; }\n");
        sb.append("                .scenario-tags { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 15px; }\n");
        sb.append("                .tag { padding: 4px 12px; background: #667eea; color: white; border-radius: 12px; ");
        sb.append("font-size: 0.85em; font-weight: 500; }\n");
        sb.append("                .steps-list { margin-top: 15px; }\n");
        sb.append("                .step-item { padding: 10px 15px; margin-bottom: 8px; border-radius: 5px; ");
        sb.append("display: flex; align-items: center; gap: 10px; }\n");
        sb.append("                .step-item.passed { background: #e8f5e9; color: #2e7d32; }\n");
        sb.append("                .step-item.failed { background: #ffebee; color: #c62828; }\n");
        sb.append("                .step-item.skipped { background: #fff3e0; color: #e65100; }\n");
        sb.append("                .step-keyword { font-weight: 600; min-width: 80px; }\n");
        sb.append("                .step-text { flex: 1; }\n");
        sb.append("                .step-duration { color: #666; font-size: 0.85em; }\n");
        sb.append("                .error-message { background: #ffebee; border-left: 4px solid #f44336; padding: 15px; ");
        sb.append("margin-top: 10px; border-radius: 5px; font-family: 'Courier New', monospace; font-size: 0.9em; ");
        sb.append("color: #c62828; white-space: pre-wrap; }\n");
        sb.append("                .screenshots { margin-top: 15px; display: flex; gap: 10px; flex-wrap: wrap; }\n");
        sb.append("                .screenshot { max-width: 300px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); ");
        sb.append("cursor: pointer; transition: transform 0.3s ease; }\n");
        sb.append("                .screenshot:hover { transform: scale(1.05); }\n");
        sb.append("                .footer { background: white; padding: 30px; border-radius: 15px; margin-top: 30px; ");
        sb.append("text-align: center; color: #666; box-shadow: 0 5px 20px rgba(0,0,0,0.1); }\n");
        sb.append("                .no-results { background: white; padding: 40px; border-radius: 15px; text-align: center; ");
        sb.append("color: #666; font-size: 1.2em; }\n");
        sb.append("                @media (max-width: 768px) { .header h1 { font-size: 2em; } ");
        sb.append(".stats-dashboard { grid-template-columns: 1fr; } .filters { flex-direction: column; } ");
        sb.append(".search-box { width: 100%; } }\n");
        sb.append("            </style>\n");
        return sb.toString();
    }
    
    private static String generateHeader(ReportStats stats) {
        StringBuilder sb = new StringBuilder();
        sb.append("            <div class=\"header\">\n");
        sb.append("                <h1>🧪 Test Execution Report</h1>\n");
        sb.append("                <p>Generated on ").append(LocalDateTime.now().format(FORMATTER)).append(" | Interactive Dashboard</p>\n");
        sb.append("            </div>\n");
        return sb.toString();
    }
    
    private static String generateStatsDashboard(ReportStats stats) {
        double passRate = stats.total > 0 ? (stats.passed * 100.0 / stats.total) : 0;
        StringBuilder sb = new StringBuilder();
        sb.append("            <div class=\"stats-dashboard\">\n");
        sb.append("                <div class=\"stat-card total\">\n");
        sb.append("                    <div class=\"icon\">📊</div>\n");
        sb.append("                    <div class=\"value\">").append(stats.total).append("</div>\n");
        sb.append("                    <div class=\"label\">Total Scenarios</div>\n");
        sb.append("                </div>\n");
        sb.append("                <div class=\"stat-card passed\">\n");
        sb.append("                    <div class=\"icon\">✅</div>\n");
        sb.append("                    <div class=\"value\">").append(stats.passed).append("</div>\n");
        sb.append("                    <div class=\"label\">Passed</div>\n");
        sb.append("                </div>\n");
        sb.append("                <div class=\"stat-card failed\">\n");
        sb.append("                    <div class=\"icon\">❌</div>\n");
        sb.append("                    <div class=\"value\">").append(stats.failed).append("</div>\n");
        sb.append("                    <div class=\"label\">Failed</div>\n");
        sb.append("                </div>\n");
        sb.append("                <div class=\"stat-card skipped\">\n");
        sb.append("                    <div class=\"icon\">⏭️</div>\n");
        sb.append("                    <div class=\"value\">").append(stats.skipped).append("</div>\n");
        sb.append("                    <div class=\"label\">Skipped</div>\n");
        sb.append("                </div>\n");
        sb.append("                <div class=\"stat-card percentage\">\n");
        sb.append("                    <div class=\"icon\">📈</div>\n");
        sb.append("                    <div class=\"value\">").append(String.format("%.1f", passRate)).append("%</div>\n");
        sb.append("                    <div class=\"label\">Pass Rate</div>\n");
        sb.append("                </div>\n");
        sb.append("            </div>\n");
        return sb.toString();
    }
    
    private static String generateFilters() {
        StringBuilder sb = new StringBuilder();
        sb.append("            <div class=\"filters\">\n");
        sb.append("                <button class=\"filter-btn active\" data-filter=\"all\">All</button>\n");
        sb.append("                <button class=\"filter-btn\" data-filter=\"passed\">✅ Passed</button>\n");
        sb.append("                <button class=\"filter-btn\" data-filter=\"failed\">❌ Failed</button>\n");
        sb.append("                <button class=\"filter-btn\" data-filter=\"skipped\">⏭️ Skipped</button>\n");
        sb.append("                <input type=\"text\" class=\"search-box\" id=\"searchBox\" placeholder=\"🔍 Search scenarios...\">\n");
        sb.append("            </div>\n");
        return sb.toString();
    }
    
    private static String generateFeaturesSection(List<FeatureData> features) {
        StringBuilder html = new StringBuilder("<div class=\"features-section\" id=\"featuresSection\">\n");
        
        for (FeatureData feature : features) {
            html.append(generateFeatureCard(feature));
        }
        
        html.append("</div>\n");
        return html.toString();
    }
    
    private static String generateFeatureCard(FeatureData feature) {
        StringBuilder html = new StringBuilder();
        html.append("<div class=\"feature-card\" data-status=\"").append(feature.status).append("\">\n");
        html.append("    <div class=\"feature-header\" onclick=\"toggleFeature(this)\">\n");
        html.append("        <div class=\"feature-title\">").append(escapeHtml(feature.name)).append("</div>\n");
        html.append("        <div class=\"feature-stats\">\n");
        
        if (feature.passed > 0) {
            html.append("            <span class=\"feature-stat passed\">✅ ").append(feature.passed).append("</span>\n");
        }
        if (feature.failed > 0) {
            html.append("            <span class=\"feature-stat failed\">❌ ").append(feature.failed).append("</span>\n");
        }
        if (feature.skipped > 0) {
            html.append("            <span class=\"feature-stat skipped\">⏭️ ").append(feature.skipped).append("</span>\n");
        }
        
        html.append("            <span class=\"expand-icon\">▼</span>\n");
        html.append("        </div>\n");
        html.append("    </div>\n");
        html.append("    <div class=\"feature-content\">\n");
        html.append("        <div class=\"scenarios-list\">\n");
        
        for (ScenarioData scenario : feature.scenarios) {
            html.append(generateScenarioItem(scenario));
        }
        
        html.append("        </div>\n");
        html.append("    </div>\n");
        html.append("</div>\n");
        
        return html.toString();
    }
    
    private static String generateScenarioItem(ScenarioData scenario) {
        StringBuilder html = new StringBuilder();
        html.append("<div class=\"scenario-item ").append(scenario.status).append("\">\n");
        html.append("    <div class=\"scenario-header\">\n");
        html.append("        <div class=\"scenario-name\">").append(escapeHtml(scenario.name)).append("</div>\n");
        html.append("        <div class=\"scenario-duration\">⏱️ ").append(formatDuration(scenario.duration)).append("</div>\n");
        html.append("    </div>\n");
        
        if (!scenario.tags.isEmpty()) {
            html.append("    <div class=\"scenario-tags\">\n");
            for (String tag : scenario.tags) {
                html.append("        <span class=\"tag\">").append(escapeHtml(tag)).append("</span>\n");
            }
            html.append("    </div>\n");
        }
        
        html.append("    <div class=\"steps-list\">\n");
        for (StepData step : scenario.steps) {
            html.append(generateStepItem(step));
        }
        html.append("    </div>\n");
        
        if (scenario.errorMessage != null && !scenario.errorMessage.isEmpty()) {
            html.append("    <div class=\"error-message\">").append(escapeHtml(scenario.errorMessage)).append("</div>\n");
        }
        
        if (!scenario.screenshots.isEmpty()) {
            html.append("    <div class=\"screenshots\">\n");
            for (String screenshot : scenario.screenshots) {
                html.append("        <img src=\"data:image/png;base64,").append(screenshot).append("\" class=\"screenshot\" onclick=\"expandImage(this)\">\n");
            }
            html.append("    </div>\n");
        }
        
        html.append("</div>\n");
        return html.toString();
    }
    
    private static String generateStepItem(StepData step) {
        StringBuilder sb = new StringBuilder();
        sb.append("            <div class=\"step-item ").append(step.status).append("\">\n");
        sb.append("                <span class=\"step-keyword\">").append(escapeHtml(step.keyword)).append("</span>\n");
        sb.append("                <span class=\"step-text\">").append(escapeHtml(step.text)).append("</span>\n");
        sb.append("                <span class=\"step-duration\">").append(formatDuration(step.duration)).append("</span>\n");
        sb.append("            </div>\n");
        return sb.toString();
    }
    
    private static String generateFooter() {
        StringBuilder sb = new StringBuilder();
        sb.append("            <div class=\"footer\">\n");
        sb.append("                <p>✨ Generated by Custom Cucumber Report Generator | Enhanced Interactive Dashboard</p>\n");
        sb.append("                <p>Report generated on ").append(LocalDateTime.now().format(FORMATTER)).append("</p>\n");
        sb.append("            </div>\n");
        return sb.toString();
    }
    
    private static String generateScripts() {
        StringBuilder sb = new StringBuilder();
        sb.append("            <script>\n");
        sb.append("                function toggleFeature(header) {\n");
        sb.append("                    const card = header.parentElement;\n");
        sb.append("                    card.classList.toggle('expanded');\n");
        sb.append("                }\n");
        sb.append("                function expandImage(img) {\n");
        sb.append("                    const modal = document.createElement('div');\n");
        sb.append("                    modal.style.cssText = 'position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,0.9);display:flex;align-items:center;justify-content:center;z-index:10000;cursor:pointer';\n");
        sb.append("                    const expandedImg = document.createElement('img');\n");
        sb.append("                    expandedImg.src = img.src;\n");
        sb.append("                    expandedImg.style.cssText = 'max-width:90%;max-height:90%;border-radius:8px';\n");
        sb.append("                    modal.appendChild(expandedImg);\n");
        sb.append("                    modal.onclick = function() { modal.remove(); };\n");
        sb.append("                    document.body.appendChild(modal);\n");
        sb.append("                }\n");
        sb.append("                const filterButtons = document.querySelectorAll('.filter-btn');\n");
        sb.append("                const searchBox = document.getElementById('searchBox');\n");
        sb.append("                const featuresSection = document.getElementById('featuresSection');\n");
        sb.append("                filterButtons.forEach(function(btn) {\n");
        sb.append("                    btn.addEventListener('click', function() {\n");
        sb.append("                        filterButtons.forEach(function(b) { b.classList.remove('active'); });\n");
        sb.append("                        this.classList.add('active');\n");
        sb.append("                        filterFeatures(this.dataset.filter, searchBox.value);\n");
        sb.append("                    });\n");
        sb.append("                });\n");
        sb.append("                searchBox.addEventListener('input', function() {\n");
        sb.append("                    const activeFilter = document.querySelector('.filter-btn.active').dataset.filter;\n");
        sb.append("                    filterFeatures(activeFilter, this.value);\n");
        sb.append("                });\n");
        sb.append("                function filterFeatures(statusFilter, searchText) {\n");
        sb.append("                    const features = document.querySelectorAll('.feature-card');\n");
        sb.append("                    let visibleCount = 0;\n");
        sb.append("                    features.forEach(function(feature) {\n");
        sb.append("                        const featureStatus = feature.dataset.status;\n");
        sb.append("                        const featureText = feature.textContent.toLowerCase();\n");
        sb.append("                        const matchesStatus = statusFilter === 'all' || featureStatus === statusFilter;\n");
        sb.append("                        const matchesSearch = searchText === '' || featureText.indexOf(searchText.toLowerCase()) !== -1;\n");
        sb.append("                        if (matchesStatus && matchesSearch) {\n");
        sb.append("                            feature.style.display = 'block';\n");
        sb.append("                            visibleCount++;\n");
        sb.append("                        } else {\n");
        sb.append("                            feature.style.display = 'none';\n");
        sb.append("                        }\n");
        sb.append("                    });\n");
        sb.append("                    if (visibleCount === 0) {\n");
        sb.append("                        if (!document.querySelector('.no-results')) {\n");
        sb.append("                            const noResults = document.createElement('div');\n");
        sb.append("                            noResults.className = 'no-results';\n");
        sb.append("                            noResults.textContent = 'No results found matching your criteria';\n");
        sb.append("                            featuresSection.appendChild(noResults);\n");
        sb.append("                        }\n");
        sb.append("                    } else {\n");
        sb.append("                        const noResults = document.querySelector('.no-results');\n");
        sb.append("                        if (noResults) { noResults.remove(); }\n");
        sb.append("                    }\n");
        sb.append("                }\n");
        sb.append("            </script>\n");
        return sb.toString();
    }
    
    private static ReportStats calculateStats(JsonNode rootNode) {
        ReportStats stats = new ReportStats();
        
        if (rootNode.isArray()) {
            for (JsonNode feature : rootNode) {
                JsonNode elements = feature.get("elements");
                if (elements != null && elements.isArray()) {
                    for (JsonNode element : elements) {
                        if ("scenario".equals(element.get("type").asText())) {
                            stats.total++;
                            String status = getScenarioStatus(element);
                            if ("passed".equals(status)) {
                                stats.passed++;
                            } else if ("failed".equals(status)) {
                                stats.failed++;
                            } else {
                                stats.skipped++;
                            }
                        }
                    }
                }
            }
        }
        
        return stats;
    }
    
    private static List<FeatureData> parseFeatures(JsonNode rootNode) {
        List<FeatureData> features = new ArrayList<>();
        
        if (rootNode.isArray()) {
            for (JsonNode featureNode : rootNode) {
                FeatureData feature = new FeatureData();
                feature.name = featureNode.has("name") ? featureNode.get("name").asText() : "Unnamed Feature";
                
                JsonNode elements = featureNode.get("elements");
                if (elements != null && elements.isArray()) {
                    for (JsonNode element : elements) {
                        if ("scenario".equals(element.get("type").asText())) {
                            ScenarioData scenario = parseScenario(element);
                            feature.scenarios.add(scenario);
                            
                            if ("passed".equals(scenario.status)) {
                                feature.passed++;
                            } else if ("failed".equals(scenario.status)) {
                                feature.failed++;
                            } else {
                                feature.skipped++;
                            }
                        }
                    }
                }
                
                // Determine feature status
                if (feature.failed > 0) {
                    feature.status = "failed";
                } else if (feature.passed > 0) {
                    feature.status = "passed";
                } else {
                    feature.status = "skipped";
                }
                
                features.add(feature);
            }
        }
        
        return features;
    }
    
    private static ScenarioData parseScenario(JsonNode element) {
        ScenarioData scenario = new ScenarioData();
        scenario.name = element.has("name") ? element.get("name").asText() : "Unnamed Scenario";
        scenario.status = getScenarioStatus(element);
        
        // Parse tags
        JsonNode tags = element.get("tags");
        if (tags != null && tags.isArray()) {
            for (JsonNode tag : tags) {
                String tagName = tag.has("name") ? tag.get("name").asText() : "";
                if (!tagName.isEmpty()) {
                    scenario.tags.add(tagName);
                }
            }
        }
        
        // Parse steps
        JsonNode steps = element.get("steps");
        if (steps != null && steps.isArray()) {
            for (JsonNode step : steps) {
                StepData stepData = parseStep(step);
                scenario.steps.add(stepData);
                
                if (stepData.duration > 0) {
                    scenario.duration += stepData.duration;
                }
            }
        }
        
        // Parse error message and screenshots from embeddings
        JsonNode embeddings = element.get("embeddings");
        if (embeddings != null && embeddings.isArray()) {
            for (JsonNode embedding : embeddings) {
                String mimeType = embedding.has("mime_type") ? embedding.get("mime_type").asText() : "";
                if ("image/png".equals(mimeType) && embedding.has("data")) {
                    scenario.screenshots.add(embedding.get("data").asText());
                }
            }
        }
        
        // Get error message from failed step
        if ("failed".equals(scenario.status)) {
            for (StepData step : scenario.steps) {
                if ("failed".equals(step.status) && step.errorMessage != null) {
                    scenario.errorMessage = step.errorMessage;
                    break;
                }
            }
        }
        
        return scenario;
    }
    
    private static StepData parseStep(JsonNode step) {
        StepData stepData = new StepData();
        stepData.keyword = step.has("keyword") ? step.get("keyword").asText() : "";
        stepData.text = step.has("name") ? step.get("name").asText() : "";
        
        JsonNode result = step.get("result");
        if (result != null) {
            stepData.status = result.has("status") ? result.get("status").asText() : "skipped";
            if (result.has("duration")) {
                stepData.duration = result.get("duration").asLong() / 1_000_000_000.0; // Convert nanoseconds to seconds
            }
            
            if (result.has("error_message")) {
                stepData.errorMessage = result.get("error_message").asText();
            }
        } else {
            stepData.status = "skipped";
        }
        
        return stepData;
    }
    
    private static String getScenarioStatus(JsonNode element) {
        JsonNode steps = element.get("steps");
        if (steps != null && steps.isArray()) {
            for (JsonNode step : steps) {
                JsonNode result = step.get("result");
                if (result != null && result.has("status")) {
                    String status = result.get("status").asText();
                    if ("failed".equals(status)) {
                        return "failed";
                    }
                }
            }
            // If no step failed, check if all steps passed
            boolean allPassed = true;
            for (JsonNode step : steps) {
                JsonNode result = step.get("result");
                if (result == null || !"passed".equals(result.get("status").asText())) {
                    allPassed = false;
                    break;
                }
            }
            return allPassed ? "passed" : "skipped";
        }
        return "skipped";
    }
    
    private static String formatDuration(double seconds) {
        if (seconds < 1) {
            return String.format("%.0f ms", seconds * 1000);
        } else {
            return String.format("%.2f s", seconds);
        }
    }
    
    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
    
    // Data classes
    private static class ReportStats {
        int total = 0;
        int passed = 0;
        int failed = 0;
        int skipped = 0;
    }
    
    private static class FeatureData {
        String name;
        String status;
        List<ScenarioData> scenarios = new ArrayList<>();
        int passed = 0;
        int failed = 0;
        int skipped = 0;
    }
    
    private static class ScenarioData {
        String name;
        String status;
        List<String> tags = new ArrayList<>();
        List<StepData> steps = new ArrayList<>();
        List<String> screenshots = new ArrayList<>();
        String errorMessage;
        double duration = 0;
    }
    
    private static class StepData {
        String keyword;
        String text;
        String status;
        double duration = 0;
        String errorMessage;
    }
}


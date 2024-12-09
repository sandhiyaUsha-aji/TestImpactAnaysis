import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

public class CodeToTestMapper {
    public static void main(String[] args) {
        try {
            // Paths
            String jacocoReportPath = "D:\\TestCoverageDemo\\target\\site\\jacoco\\jacoco.xml";
            String testScriptPath = "D:\\TestCoverageDemo\\src\\test\\java\\Bahwan\\TestCoverageDemo\\codeCoverageTest.java";
            String sourceCodeBasePath = "D:\\TestCoverageDemo\\src\\main\\java\\Bahwan\\TestCoverageDemo\\codeCoverage.java";
            String jsonOutputPath = "D:\\TestCoverageDemo\\src\\test\\java\\Data\\codeToTest.json";

            // Parse JaCoCo report and test script
            Map<String, String> codeToTestMap = parseJaCoCoReportAndTestScript(
                    jacocoReportPath, testScriptPath, sourceCodeBasePath);

            // Generate JSON file
            generateJsonFile(codeToTestMap, jsonOutputPath);

            System.out.println("codeToTest.json file generated successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Map<String, String> parseJaCoCoReportAndTestScript(
            String reportPath, String testScriptPath, String sourceCodeBasePath) {
        Map<String, String> codeToTestMap = new HashMap<>();

        try {
            // Extract covered methods from JaCoCo XML report
            Map<String, String> methodToSourceMap = new HashMap<>();
            File reportFile = new File(reportPath);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            // Disable DTD validation
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setFeature("http://xml.org/sax/features/validation", false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(reportFile);

            NodeList packageNodes = doc.getElementsByTagName("package");
            for (int i = 0; i < packageNodes.getLength(); i++) {
                NodeList classNodes = packageNodes.item(i).getChildNodes();
                for (int j = 0; j < classNodes.getLength(); j++) {
                    if (classNodes.item(j).getNodeName().equals("class")) {
                        String className = classNodes.item(j).getAttributes()
                                .getNamedItem("name").getNodeValue();

                        // Convert class name to source file path
                        String sourceFilePath = sourceCodeBasePath.replace("\\", "\\\\");

                        NodeList methodNodes = classNodes.item(j).getChildNodes();
                        for (int k = 0; k < methodNodes.getLength(); k++) {
                            if (methodNodes.item(k).getNodeName().equals("method")) {
                                String methodName = methodNodes.item(k).getAttributes()
                                        .getNamedItem("name").getNodeValue();

                                // Map method to its source file path
                                methodToSourceMap.put(methodName, sourceFilePath);
                            }
                        }
                    }
                }
            }

            // Parse test script to extract test method names
            File testScriptFile = new File(testScriptPath);
            if (testScriptFile.exists()) {
                try (java.util.Scanner scanner = new java.util.Scanner(testScriptFile)) {
                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine().trim();
                        if (line.startsWith("@Test")) {
                            // Read the next line for the method declaration
                            String methodLine = scanner.nextLine().trim();
                            if (methodLine.startsWith("public void")) {
                                String testMethodName = methodLine.split("\\s+")[2].split("\\(")[0];

                                // Map test method to source method using traditional switch
                                String coveredSource;
                                switch (testMethodName) {
                                    case "sample1":
                                    case "sample2":
                                        coveredSource = methodToSourceMap.getOrDefault("start", "No Coverage");
                                        break;
                                    case "sample3":
                                        coveredSource = "No Coverage";
                                        break;
                                    default:
                                        coveredSource = "Unknown Source";
                                }

                                // Map test method to the source file
                                codeToTestMap.put(testMethodName, coveredSource);
                            }
                        }
                    }
                }
            } else {
                System.err.println("Test script file not found: " + testScriptPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return codeToTestMap;
    }

    private static void generateJsonFile(Map<String, String> codeToTestMap, String outputPath) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode rootNode = mapper.createObjectNode();

            for (Map.Entry<String, String> entry : codeToTestMap.entrySet()) {
                // Replace double backslashes with single backslashes
                String normalizedPath = entry.getValue().replace("\\\\", "\\");
                rootNode.put(entry.getKey(), normalizedPath);
            }

            // Write JSON to file
            try (FileWriter file = new FileWriter(outputPath)) {
                mapper.writerWithDefaultPrettyPrinter().writeValue(file, rootNode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

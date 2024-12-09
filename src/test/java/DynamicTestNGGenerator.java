import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynamicTestNGGenerator {
    public static void main(String[] args) {
        try {
            // Paths to inputs and output
            String modifiedFilesPath = "D:\\TestCoverageDemo\\src\\test\\java\\Data\\ModifiedFiles.txt";
            String codeToTestJsonPath = "D:\\TestCoverageDemo\\src\\test\\java\\Data\\codeToTest.json";
            String testngXmlPath = "D:\\TestCoverageDemo\\testng.xml";

            // Step 1: Read and normalize modified files
            List<String> modifiedFiles = readModifiedFiles(modifiedFilesPath);
            List<String> normalizedModifiedFiles = normalizeFilePaths(modifiedFiles);

            // Step 2: Load and normalize codeToTest JSON
            Map<String, String> codeToTestMap = loadCodeToTestJson(codeToTestJsonPath);
            Map<String, String> normalizedCodeToTestMap = normalizeCodeToTestMap(codeToTestMap);

            // Step 3: Identify impacted tests
            List<String> impactedTests = findImpactedTests(normalizedModifiedFiles, normalizedCodeToTestMap);

            // Debugging: Print results
            System.out.println("Normalized Modified Files: " + normalizedModifiedFiles);
            System.out.println("Impacted Tests: " + impactedTests);

            // Step 4: Update testng.xml dynamically
            generateTestNGXml(impactedTests, testngXmlPath);

            System.out.println("testng.xml file updated successfully with impacted tests.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<String> readModifiedFiles(String filePath) throws Exception {
        return Files.readAllLines(Paths.get(filePath));
    }

    private static List<String> normalizeFilePaths(List<String> filePaths) {
        List<String> normalizedPaths = new ArrayList<>();
        for (String path : filePaths) {
            normalizedPaths.add(path.trim().replace("\\", "/").toLowerCase());
        }
        return normalizedPaths;
    }

    private static Map<String, String> loadCodeToTestJson(String filePath) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File(filePath), Map.class);
    }

    private static Map<String, String> normalizeCodeToTestMap(Map<String, String> codeToTestMap) {
        Map<String, String> normalizedMap = new HashMap<>();
        codeToTestMap.forEach((key, value) -> {
            normalizedMap.put(key, value.trim().replace("\\", "/").toLowerCase());
        });
        return normalizedMap;
    }

    private static List<String> findImpactedTests(List<String> modifiedFiles, Map<String, String> normalizedCodeToTestMap) {
        List<String> impactedTests = new ArrayList<>();
        for (Map.Entry<String, String> entry : normalizedCodeToTestMap.entrySet()) {
            String testMethod = entry.getKey();
            String sourceFilePath = entry.getValue();

            if (modifiedFiles.contains(sourceFilePath)) {
                impactedTests.add(testMethod);
            }
        }
        return impactedTests;
    }

    private static void generateTestNGXml(List<String> impactedTests, String outputPath) throws Exception {
        if (impactedTests.isEmpty()) {
            System.out.println("No impacted tests to execute.");
            return;
        }

        StringBuilder xmlBuilder = new StringBuilder();
        xmlBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xmlBuilder.append("<!DOCTYPE suite SYSTEM \"https://testng.org/testng-1.0.dtd\">\n");
        xmlBuilder.append("<suite name=\"ImpactedTests\">\n");
        xmlBuilder.append("  <test thread-count=\"5\" name=\"Test\">\n");
        xmlBuilder.append("    <classes>\n");
        xmlBuilder.append("      <class name=\"Bahwan.TestCoverageDemo.codeCoverageTest\">\n");
        xmlBuilder.append("        <methods>\n");

        for (String test : impactedTests) {
            xmlBuilder.append("          <include name=\"").append(test).append("\"/>\n");
        }

        xmlBuilder.append("        </methods>\n");
        xmlBuilder.append("      </class>\n");
        xmlBuilder.append("    </classes>\n");
        xmlBuilder.append("  </test>\n");
        xmlBuilder.append("</suite>");

        try (FileWriter writer = new FileWriter(outputPath)) {
            writer.write(xmlBuilder.toString());
        }
    }
}

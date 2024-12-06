import java.io.*;

import java.nio.file.*;
import java.util.*;
import org.json.*;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;


public class IdentifyImpactedTests {

    public static void main(String[] args) {
        try {
            // Paths to input files
            String testMappingPath = "D:\\TestCoverageDemo\\src\\test\\java\\Data\\CodeToTest.json"; // Test-to-code mapping file
            String modifiedFilesArtifactPath = "artifacts/modified_files.txt"; // GitHub Actions artifact

            // Output file for impacted tests
            String impactedTestsPath = "D:\\TestCoverageDemo\\src\\test\\java\\Data\\impacted_tests.txt";

            // Load test-to-code mapping
            Map<String, List<String>> testMapping = loadTestMapping(testMappingPath);

            // Read modified files from GitHub Actions artifact
            List<String> modifiedFiles = readModifiedFiles(modifiedFilesArtifactPath);

            // Identify impacted tests
            List<String> impactedTests = findImpactedTests(testMapping, modifiedFiles);

            // Write impacted tests to a file
            writeImpactedTests(impactedTestsPath, impactedTests);

            System.out.println("Impacted tests identified and saved to " + impactedTestsPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Load test-to-code mapping from JSON file
    private static Map<String, List<String>> loadTestMapping(String filePath) throws IOException, JSONException {
        Map<String, List<String>> testMapping = new HashMap<>();

        // Read the JSON file
        String jsonContent = Files.readString(Paths.get(filePath));

        // Parse JSON into a map
        JSONObject jsonObject = new JSONObject(jsonContent);
        for (String testName : jsonObject.keySet()) {
            JSONArray filesArray = jsonObject.getJSONArray(testName);
            List<String> filesList = new ArrayList<>();
            for (int i = 0; i < filesArray.length(); i++) {
                filesList.add(filesArray.getString(i));
            }
            testMapping.put(testName, filesList);
        }

        return testMapping;
    }

    // Read modified files from GitHub Actions artifact
    private static List<String> readModifiedFiles(String filePath) throws IOException {
        List<String> modifiedFiles = new ArrayList<>();

        // Read the file line by line
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        while ((line = reader.readLine()) != null) {
            modifiedFiles.add(line.trim());
        }
        reader.close();

        return modifiedFiles;
    }

    // Find impacted tests by matching modified files with the test mapping
    private static List<String> findImpactedTests(Map<String, List<String>> testMapping, List<String> modifiedFiles) {
        List<String> impactedTests = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : testMapping.entrySet()) {
            String testName = entry.getKey();
            List<String> files = entry.getValue();

            // Check if any modified file matches the mapped files for this test
            if (files.stream().anyMatch(modifiedFiles::contains)) {
                impactedTests.add(testName);
            }
        }

        return impactedTests;
    }

    // Write impacted tests to a text file
    private static void writeImpactedTests(String filePath, List<String> impactedTests) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));

        // Write each test on a new line
        for (String test : impactedTests) {
            writer.write(test);
            writer.newLine();
        }

        writer.close();
    }
}

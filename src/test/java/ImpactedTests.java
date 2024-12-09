import org.json.JSONObject;

import org.json.JSONArray;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImpactedTests {

    public static void main(String[] args) {
        // Path to the JSON file
    	 String jsonFilePath = "D:\\TestCoverageDemo\\src\\test\\java\\Data\\CodeToTestMap.json";
    	 String modifiedFilesPath = "D:\\Work/OneDrive - bahwancybertek.com\\modified-files\\modified_files.txt";

        // List of changed files (simulate git diff output)
    	 
//         List<String> changedMethods = List.of(
//                 "public void sample3()",
//                 "public static String start()",
//                 "public static String stop()",
//                 "public static void main(String[] args)",
//                 "public static String processing()",
//                 "public static String start()",
//                 "public static String stop()"
//             );

        // Identify impacted tests
        List<String> impactedTests = getImpactedTests(jsonFilePath, modifiedFilesPath);

        // Print impacted tests
        System.out.println("Impacted Tests: " + impactedTests);
    }

    public static List<String> getImpactedTests(String jsonFilePath, String modifiedFilesPath) {
        List<String> impactedTests = new ArrayList<>();

        try {
            // Read the JSON file
            FileReader reader = new FileReader(jsonFilePath);
            char[] buffer = new char[1024];
            int bytesRead = reader.read(buffer);
            String jsonData = new String(buffer, 0, bytesRead);
            reader.close();

            // Parse the JSON data
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONObject tests = jsonObject.getJSONObject("tests");

            // Loop through the test-to-code mapping
            for (String testName : tests.keySet()) {
                JSONArray files = tests.getJSONArray(testName);

                // Check if any of the changed files match the test-to-code mapping
                for (int i = 0; i < files.length(); i++) {
                    if (modifiedFilesPath.contains(files.getString(i))) {
                        impactedTests.add(testName);
                        break;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading the JSON file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error processing JSON data: " + e.getMessage());
        }

        return impactedTests;    
    }
}

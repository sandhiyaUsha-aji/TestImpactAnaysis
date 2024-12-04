
import java.io.*;

import java.util.*;
import java.util.stream.Collectors;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.json.JSONObject;

public class UpdateTestMapping {

    public static void main(String[] args) {
        try {
            // Paths
            String jacocoReportPath = "D:\\TestCoverageDemo\\target\\site\\jacoco\\jacoco.xml";
            String testMappingPath = "D:\\TestCoverageDemo\\src\\test\\java\\Data\\CodeToTest.json";

            // Parse JaCoCo XML
            Map<String, List<String>> newMapping = parseJaCoCoReport(jacocoReportPath);

            // Update test_mapping.json
            updateTestMapping(testMappingPath, newMapping);

            System.out.println("json updated successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Map<String, List<String>> parseJaCoCoReport(String reportPath) throws Exception {
        Map<String, List<String>> testMapping = new HashMap<>();

        // Load and parse the XML
        File xmlFile = new File(reportPath);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false); // Disable DTD loading
        factory.setFeature("http://xml.org/sax/features/validation", false); // Disable validation
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(xmlFile);
        document.getDocumentElement().normalize();

        // Parse <package> and <sourcefile> elements
        NodeList packageList = document.getElementsByTagName("package");

        for (int i = 0; i < packageList.getLength(); i++) {
            Element packageElement = (Element) packageList.item(i);
            String packageName = packageElement.getAttribute("name");

            NodeList sourceFiles = packageElement.getElementsByTagName("sourcefile");
            for (int j = 0; j < sourceFiles.getLength(); j++) {
                Element sourceFile = (Element) sourceFiles.item(j);
                String fileName = sourceFile.getAttribute("name");

                // Full file path
                String filePath = packageName.replace('/', '.') + "." + fileName;

                // Get covered test cases from <line> elements
                NodeList lineNodes = sourceFile.getElementsByTagName("line");
                for (int k = 0; k < lineNodes.getLength(); k++) {
                    Element lineNode = (Element) lineNodes.item(k);
                    String covered = lineNode.getAttribute("ci"); // CI = covered instruction count
                    if (!"0".equals(covered)) {
                        String testName = "test" + fileName; // Derive test name (mocked for demo)
                        testMapping.computeIfAbsent(testName, key -> new ArrayList<>()).add(filePath);
                    }
                }
            }
        }

        return testMapping;
    }


    private static void updateTestMapping(String mappingFilePath, Map<String, List<String>> newMapping) throws IOException {
        // Load existing test mapping
        File mappingFile = new File(mappingFilePath);
        JSONObject testMappingJson;

        if (mappingFile.exists()) {
            BufferedReader reader = new BufferedReader(new FileReader(mappingFile));
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line);
            }
            reader.close();
            testMappingJson = new JSONObject(jsonContent.toString());
        } else {
            testMappingJson = new JSONObject();
        }

        // Update mappings with new data
        for (Map.Entry<String, List<String>> entry : newMapping.entrySet()) {
            String testName = entry.getKey();
            List<String> files = entry.getValue();

            // Add or update test mapping
            if (!testMappingJson.has(testName)) {
                testMappingJson.put(testName, files);
            } else {
            	List<String> existingFiles = testMappingJson.getJSONArray(testName).toList().stream()
            		    .map(Object::toString)
            		    .collect(Collectors.toList());

                Set<String> updatedFiles = new HashSet<>(existingFiles);
                updatedFiles.addAll(files);
                testMappingJson.put(testName, new ArrayList<>(updatedFiles));
            }
        }

        // Write updated JSON back to file
        BufferedWriter writer = new BufferedWriter(new FileWriter(mappingFilePath));
        writer.write(testMappingJson.toString(4)); // Pretty print JSON
        writer.close();
    }
}

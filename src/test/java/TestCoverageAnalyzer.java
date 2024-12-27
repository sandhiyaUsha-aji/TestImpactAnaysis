import com.fasterxml.jackson.databind.ObjectMapper;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.lang.reflect.Method;
import java.util.*;
import org.junit.Test;

public class TestCoverageAnalyzer {

    public static void main(String[] args) {
        String jacocoXMLPath = "D:\\CustomTestImpactAnalysis\\target\\site\\jacoco\\jacoco.xml"; // Path to JaCoCo report
        String outputJSONPath = "D:\\CustomTestImpactAnalysis\\src\\test\\java\\Data\\CodeToTest.json"; // JSON file path

        // Test classes to analyze
        List<String> testClasses = Arrays.asList(
                "Bahwan.TestCoverageDemo.codeCoverageTest"
        );

        // Source code file mappings
        Map<String, String> sourceFilePaths = new HashMap<>();
        sourceFilePaths.put("codeCoverage1", "D:\\CustomTestImpactAnalysis\\src\\main\\java\\Bahwan\\TestCoverageDemo\\sourceCode1.java");
        sourceFilePaths.put("codeCoverage2", "D:\\CustomTestImpactAnalysis\\src\\main\\java\\Bahwan\\TestCoverageDemo\\sourceCode2.java");

        try {
            // Step 1: Parse JaCoCo XML for coverage data
            Map<String, Set<String>> coverageData = parseJaCoCoXML(jacocoXMLPath);
            System.out.println("Parsed Coverage Data: " + coverageData);

            // Step 2: Extract test methods from test classes
            List<String> testMethods = extractTestMethods(testClasses);
            System.out.println("Extracted Test Methods: " + testMethods);

            // Step 3: Combine data and build JSON
            Map<String, String> finalMapping = buildJSONData(testMethods, coverageData, sourceFilePaths);
            System.out.println("Final Mapping: " + finalMapping);

            // Step 4: Write to JSON file
            writeToJsonFile(outputJSONPath, finalMapping);

            System.out.println("JSON file generated successfully at: " + outputJSONPath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Parse JaCoCo XML to extract class and method names
    private static Map<String, Set<String>> parseJaCoCoXML(String filePath) {
        Map<String, Set<String>> coverageData = new HashMap<>();
        try {
            File file = new File(filePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false); // Disable external DTD loading
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);

            NodeList classes = doc.getElementsByTagName("class");

            for (int i = 0; i < classes.getLength(); i++) {
                Element classElement = (Element) classes.item(i);
                String className = classElement.getAttribute("name").replace("/", ".");

                NodeList methods = classElement.getElementsByTagName("method");
                Set<String> methodNames = new HashSet<>();

                for (int j = 0; j < methods.getLength(); j++) {
                    Element method = (Element) methods.item(j);
                    String methodName = method.getAttribute("name");

                    NodeList counters = method.getElementsByTagName("counter");
                    for (int k = 0; k < counters.getLength(); k++) {
                        Element counter = (Element) counters.item(k);
                        if ("METHOD".equals(counter.getAttribute("type")) && Integer.parseInt(counter.getAttribute("covered")) > 0) {
                            methodNames.add(methodName);
                        }
                    }
                }

                coverageData.put(className, methodNames);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return coverageData;
    }

    // Extract test method names using reflection
    private static List<String> extractTestMethods(List<String> testClasses) {
        List<String> testMethods = new ArrayList<>();
        try {
            for (String testClass : testClasses) {
                System.out.println("Analyzing Test Class: " + testClass);
                try {
                    Class<?> clazz = Class.forName(testClass);
                    System.out.println("Loaded Class: " + clazz.getName());

                    Method[] methods = clazz.getDeclaredMethods();
                    System.out.println("Methods in Class:");
                    for (Method method : methods) {
                        System.out.println("  - " + method.getName() + " with parameters: " + Arrays.toString(method.getParameterTypes()));
                        System.out.println("    Is Test Method: " + method.isAnnotationPresent(Test.class));
                        if (method.isAnnotationPresent(Test.class)) {
                            testMethods.add(method.getName());
                            System.out.println("    \u2713 Found Test Method: " + method.getName());
                        }
                    }
                } catch (ClassNotFoundException e) {
                    System.err.println("Class not found: " + testClass);
                    // Log specific information for debugging
                    System.err.println("Ensure that the class name is correct and the test classes are properly imported.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return testMethods;
    }


    // Build JSON data combining test methods, coverage, and source files
    private static Map<String, String> buildJSONData(
            List<String> testMethods,
            Map<String, Set<String>> coverageData,
            Map<String, String> sourceFilePaths) {

        Map<String, String> result = new LinkedHashMap<>();

        for (String testMethod : testMethods) {
            System.out.println("Processing Test Method: " + testMethod);
            boolean isCovered = false;

            for (Map.Entry<String, Set<String>> entry : coverageData.entrySet()) {
                System.out.println("Checking Coverage in Class: " + entry.getKey());
                if (entry.getValue().contains(testMethod)) {
                    String sourceFile = sourceFilePaths.getOrDefault(entry.getKey(), "No Coverage");
                    System.out.println("Method " + testMethod + " is covered in source file: " + sourceFile);
                    result.put(testMethod, sourceFile);
                    isCovered = true;
                    break;
                }
            }

            if (!isCovered) {
                System.out.println("Method " + testMethod + " has no coverage.");
                result.put(testMethod, "No Coverage");
            }
        }
        return result;
    }

    // Write data to a JSON file
    private static void writeToJsonFile(String filePath, Map<String, String> data) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), data);
            System.out.println("Written JSON to file: " + filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

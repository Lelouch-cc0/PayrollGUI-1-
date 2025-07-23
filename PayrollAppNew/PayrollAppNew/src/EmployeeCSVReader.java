// EmployeeCSVReader.java
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.*;
import java.util.*;

public class EmployeeCSVReader {
    private static final List<String> REQUIRED_COLUMNS = List.of(
        "Last Name", "First Name", "Position",
        "Basic Salary", "Rice Subsidy", "Phone Allowance", "Clothing Allowance"
    );

    public static List<Employee> readEmployees(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("CSV file not found at: " + file.getAbsolutePath());
        }

        List<Employee> employees = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(file))) {
            String[] headers = validateHeaders(reader.readNext());
            
            String[] row;
            while ((row = reader.readNext()) != null) {
                if (row.length != headers.length) continue;
                
                Map<String, String> details = new LinkedHashMap<>();
                for (int i = 0; i < headers.length; i++) {
                    String value = row[i].trim();
                    // Handle empty salary fields
                    if (REQUIRED_COLUMNS.subList(3, 7).contains(headers[i]) && value.isEmpty()) {
                        value = "0";
                    }
                    details.put(headers[i], value);
                }
                
                // Add default values for new credential fields if missing
                if (!details.containsKey("employee_id")) {
                    details.put("employee_id", ""); // Will be generated in Employee constructor
                }
                if (!details.containsKey("username")) {
                    details.put("username", ""); // Will be generated in Employee constructor
                }
                
                employees.add(new Employee(details));
            }
        } catch (CsvValidationException e) {
            throw new IOException("Invalid CSV format: " + e.getMessage());
        }
        return employees;
    }

    // New method to read employees with credentials
    public static List<Employee> readEmployeesWithCredentials(String employeesPath, String credentialsPath) 
            throws IOException {
        List<Employee> employees = readEmployees(employeesPath);
        Map<String, Map<String, String>> credentialsMap = loadCredentials(credentialsPath);
        
        // Merge credential data
        for (Employee emp : employees) {
            Map<String, String> creds = credentialsMap.get(emp.getEmployeeId());
            if (creds != null) {
                emp.getDetails().putAll(creds);
            }
        }
        return employees;
    }

    private static Map<String, Map<String, String>> loadCredentials(String filePath) throws IOException {
        Map<String, Map<String, String>> credentials = new HashMap<>();
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            reader.readNext(); // Skip header
            String[] row;
            while ((row = reader.readNext()) != null) {
                if (row.length >= 4) {
                    Map<String, String> cred = new HashMap<>();
                    cred.put("username", row[0]);
                    cred.put("password", row[1]);
                    cred.put("employee_id", row[2]);
                    cred.put("role", row[3]);
                    credentials.put(row[2], cred); // employee_id as key
                }
            }
        } catch (CsvValidationException e) {
            throw new IOException("Invalid credentials format: " + e.getMessage());
        }
        return credentials;
    }

    // Your existing validateHeaders method remains unchanged
    private static String[] validateHeaders(String[] headers) throws IOException {
        if (headers == null || headers.length == 0) {
            throw new IOException("CSV file is empty");
        }

        Set<String> headerSet = new HashSet<>(Arrays.asList(headers));
        List<String> missing = new ArrayList<>();

        for (String col : REQUIRED_COLUMNS) {
            if (!headerSet.contains(col)) {
                missing.add(col);
            }
        }

        if (!missing.isEmpty()) {
            throw new IOException("Missing required columns: " + String.join(", ", missing));
        }

        return headers;
    }
}
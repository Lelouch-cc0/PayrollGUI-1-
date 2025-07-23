// CredentialManager.java
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import java.io.*;
import java.util.*;

public class CredentialManager {
    private static final String EMPLOYEE_FILE = "data/employees.csv";
    private static final String CREDENTIALS_FILE = "data/login_credentials.csv";
    public static final String DEFAULT_PASSWORD = "password";
    public static void syncCredentials() throws IOException {
        // 1. Read existing employee data
        List<Employee> employees = EmployeeCSVReader.readEmployees(EMPLOYEE_FILE);
        
        // 2. Load existing credentials to preserve passwords
        Map<String, String[]> existingCredentials = loadExistingCredentials();
        
        // 3. Write updated credentials file
        try (CSVWriter writer = new CSVWriter(new FileWriter(CREDENTIALS_FILE))) {
            // Write header
            writer.writeNext(new String[]{"username", "password", "employee_id", "role"});
            
            // Write each employee's credentials
            for (Employee emp : employees) {
                String[] creds = existingCredentials.getOrDefault(
                    emp.getEmployeeId(),
                    new String[]{
                        emp.getUsername(),
                        emp.getDefaultPassword(), // Uses SHA-256 hashed "default123"
                        emp.getEmployeeId(),
                        emp.getEmployeeRole()
                    }
                );
                writer.writeNext(creds);
            }
        }
    }

    private static Map<String, String[]> loadExistingCredentials() throws IOException {
        Map<String, String[]> credentials = new HashMap<>();
        File credFile = new File(CREDENTIALS_FILE);
        
        if (!credFile.exists()) {
            return credentials; // Return empty map if file doesn't exist
        }
        
        try (CSVReader reader = new CSVReader(new FileReader(credFile))) {
            reader.readNext(); // Skip header
            String[] row;
            while ((row = reader.readNext()) != null) {
                if (row.length >= 4) { // Ensure valid credential line
                    credentials.put(row[2], row); // Key: employee_id
                }
            }
        } catch (CsvValidationException e) {
            throw new IOException("Invalid credentials file format", e);
        }
        return credentials;
    }

    public static void resetPassword(String employeeId, String newPassword) throws IOException {
        // 1. Load all credentials
        Map<String, String[]> credentials = loadExistingCredentials();
        
        // 2. Update specific password
        String[] cred = credentials.get(employeeId);
        if (cred != null) {
            cred[1] = Employee.hashPassword(newPassword); // Update password hash
        } else {
            throw new IOException("Employee ID not found: " + employeeId);
        }
        
        // 3. Rewrite entire file
        try (CSVWriter writer = new CSVWriter(new FileWriter(CREDENTIALS_FILE))) {
            writer.writeNext(new String[]{"username", "password", "employee_id", "role"});
            for (String[] creds : credentials.values()) {
                writer.writeNext(creds);
            }
        }
    }

    public static boolean validateCredentials(String username, String password) throws IOException {
        try (CSVReader reader = new CSVReader(new FileReader(CREDENTIALS_FILE))) {
            reader.readNext(); // Skip header
            String[] row;
            while ((row = reader.readNext()) != null) {
                if (row.length >= 4 && 
                    row[0].equals(username) && 
                    row[1].equals(Employee.hashPassword(password))) {
                    return true;
                }
            }
        } catch (CsvValidationException e) {
            throw new IOException("Invalid credentials file format", e);
        }
        return false;
    }

    public static void main(String[] args) {
        try {
            // Initial sync
            syncCredentials();
            System.out.println("Credentials synchronized successfully!");
            
            // Test password reset
            resetPassword("EMP001", "newSecurePassword123");
            System.out.println("Password reset successfully!");
            
            // Test validation
            System.out.println("Credentials valid? " + 
                validateCredentials("mgarcia", "newSecurePassword123"));
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
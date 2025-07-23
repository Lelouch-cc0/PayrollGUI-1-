import com.opencsv.CSVWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class EmployeeCSVWriter {
    private static final String[] CSV_HEADERS = {
        "Last Name", "First Name", "Position",
        "Basic Salary", "Rice Subsidy", "Phone Allowance", "Clothing Allowance"
    };

    public static void writeEmployees(String filePath, List<Employee> employees) throws IOException {
        if (employees == null) {
            throw new IllegalArgumentException("Employee list cannot be null");
        }

        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath, StandardCharsets.UTF_8))) {
            // Write headers (using pre-allocated array)
            writer.writeNext(CSV_HEADERS);
            
            // Write data rows
            for (Employee emp : employees) {
                Map<String, String> details = emp.getDetails();
                String[] row = new String[CSV_HEADERS.length];
                
                for (int i = 0; i < CSV_HEADERS.length; i++) {
                    row[i] = details.getOrDefault(CSV_HEADERS[i], "");
                }
                writer.writeNext(row);
            }
        }
    }
}
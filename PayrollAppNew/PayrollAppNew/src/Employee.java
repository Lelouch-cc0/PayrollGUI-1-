// Employee.java
import java.util.Map;
import java.util.List;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Employee {

    static String hashPassword(String newPassword) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    private final Map<String, String> details;
    private final String employeeId;
    private final String username;

    public Employee(Map<String, String> details) {
        this.details = details;
        this.employeeId = generateEmployeeId();
        this.username = generateUsername();
    }

    // Existing payroll methods (unchanged)
    public static List<String> getCSVHeaders() {
        return List.of(
            "Last Name", "First Name", "Position",
            "Basic Salary", "Rice Subsidy", "Phone Allowance", "Clothing Allowance"
        );
    }

    public double calculateGrossSalary() {
        return parseNumber("Basic Salary") 
             + parseNumber("Rice Subsidy")
             + parseNumber("Phone Allowance")
             + parseNumber("Clothing Allowance");
    }

    public double calculateNetSalary() {
        double gross = calculateGrossSalary();
        return gross - calculateSSS() - calculatePhilHealth() - calculatePagIBIG() - calculateTax();
    }

    private double calculateSSS() {
        double basic = parseNumber("Basic Salary");
        return Math.min(basic * 0.045, 1350);
    }

    private double calculatePhilHealth() {
        double basic = parseNumber("Basic Salary");
        return Math.max(450, Math.min(basic * 0.04, 4050));
    }

    private double calculatePagIBIG() {
        return 100;
    }

    private double calculateTax() {
        double taxableIncome = calculateGrossSalary() - calculateSSS() - calculatePhilHealth() - calculatePagIBIG();
        if (taxableIncome <= 20833) return 0;
        if (taxableIncome <= 33333) return (taxableIncome - 20833) * 0.20;
        if (taxableIncome <= 66667) return (taxableIncome - 33333) * 0.25 + 2500;
        if (taxableIncome <= 166667) return (taxableIncome - 66667) * 0.30 + 10833.33;
        return (taxableIncome - 166667) * 0.35 + 40833.33;
    }

    private double parseNumber(String key) {
        String value = details.getOrDefault(key, "0").replaceAll("[^\\d.]", "");
        return Double.parseDouble(value.isEmpty() ? "0" : value);
    }

    // New credential-related methods
    private String generateUsername() {
        String firstName = details.get("First Name");
        String lastName = details.get("Last Name");
        return (firstName.charAt(0) + lastName).toLowerCase()
                .replaceAll("[^a-z0-9]", ""); // Remove special chars
    }

    private String generateEmployeeId() {
    String lastName = details.getOrDefault("Last Name", "");
    String position = details.getOrDefault("Position", "");
    
    // Handle empty/null cases
    if (lastName.isEmpty() || position.isEmpty()) {
        throw new IllegalArgumentException("Missing required employee details");
    }

    // Get first 3 letters of last name (or full name if shorter)
    String lastNamePart = lastName.substring(0, Math.min(3, lastName.length()));
    
    // Clean position string and get first 3 non-space chars
    String cleanPosition = position.replaceAll("\\s+", "");
    String positionPart = cleanPosition.substring(0, Math.min(3, cleanPosition.length()));
    
    // Generate unique numeric suffix
    int uniqueNum = Math.abs((lastName + position).hashCode() % 1000);
    
    return (lastNamePart + positionPart).toUpperCase() 
           + String.format("%03d", uniqueNum);
}

    public String getDefaultPassword() {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest("default123".getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Password hashing failed", e);
        }
    }

    public String getEmployeeRole() {
        String position = details.get("Position");
        return position.matches("(?i).*(Chief|Manager|Head|Director).*") ? "admin" : "employee";
    }

    // Getters for credential fields
    public String getEmployeeId() { return employeeId; }
    public String getUsername() { return username; }

    // Existing display method (unchanged)
    public String getFormattedInfo() {
        return String.format("""
            === EMPLOYEE PAYSLIP ===
            Name: %s, %s
            Position: %s
            Employee ID: %s
            Username: %s

            EARNINGS:
            Basic Salary: ₱%,.2f
            Rice Subsidy: ₱%,.2f
            Phone Allowance: ₱%,.2f
            Clothing Allowance: ₱%,.2f
            Gross Salary: ₱%,.2f

            DEDUCTIONS:
            SSS: ₱%,.2f
            PhilHealth: ₱%,.2f
            Pag-IBIG: ₱%,.2f
            Income Tax: ₱%,.2f

            NET SALARY: ₱%,.2f
            """,
            details.get("Last Name"), details.get("First Name"),
            details.get("Position"),
            employeeId,
            username,
            parseNumber("Basic Salary"),
            parseNumber("Rice Subsidy"),
            parseNumber("Phone Allowance"),
            parseNumber("Clothing Allowance"),
            calculateGrossSalary(),
            calculateSSS(),
            calculatePhilHealth(),
            calculatePagIBIG(),
            calculateTax(),
            calculateNetSalary()
        );
    }

    public Map<String, String> getDetails() {
        return details;
    }
}
package com.hcl.hrgenius.config;

import com.hcl.hrgenius.entity.*;
import com.hcl.hrgenius.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Initializes baseline reference data only if the respective database tables are empty.
 * Never overwrites or deletes existing records.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final CandidateRepository candidateRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRepository;
    private final PayrollRepository payrollRepository;
    private final PerformanceRepository performanceRepository;
    private final InterviewRepository interviewRepository;
    private final TrainingRepository trainingRepository;
    private final DocumentRepository documentRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(DepartmentRepository departmentRepository,
                      EmployeeRepository employeeRepository,
                      UserRepository userRepository,
                      JobRepository jobRepository,
                      CandidateRepository candidateRepository,
                      AttendanceRepository attendanceRepository,
                      LeaveRequestRepository leaveRepository,
                      PayrollRepository payrollRepository,
                      PerformanceRepository performanceRepository,
                      InterviewRepository interviewRepository,
                      TrainingRepository trainingRepository,
                      DocumentRepository documentRepository,
                      PasswordEncoder passwordEncoder) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.candidateRepository = candidateRepository;
        this.attendanceRepository = attendanceRepository;
        this.leaveRepository = leaveRepository;
        this.payrollRepository = payrollRepository;
        this.performanceRepository = performanceRepository;
        this.interviewRepository = interviewRepository;
        this.trainingRepository = trainingRepository;
        this.documentRepository = documentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        Map<String, Department> deptMap = seedDepartments();
        seedEmployees(deptMap);
        seedUsers();
        seedJobs();
        seedCandidates();
        seedAttendance();
        seedLeaves();
        seedPayrolls();
        seedPerformance();
        seedInterviews();
        seedTrainings();
        seedDocuments();
    }

    private Map<String, Department> seedDepartments() {
        Map<String, Department> deptMap = new HashMap<>();

        if (departmentRepository.count() == 0) {
            log.info("No departments found in database. Seeding initial baseline departments...");

            Department hr = new Department("DEP-101", "Human Resources", "HR-01",
                    "Handles recruitment, people operations, benefits, and labor compliance.", "Sarah Jenkins");
            Department eng = new Department("DEP-102", "Software Engineering", "ENG-02",
                    "Core software engineering, cloud systems, and platform products.", "David Chen");
            Department mkt = new Department("DEP-103", "Marketing & Sales", "MKT-03",
                    "Brand expansion, lead generation, digital growth, and client relations.", "Emily Watson");
            Department fin = new Department("DEP-104", "Finance & Accounts", "FIN-04",
                    "Budget planning, accounts payable, fiscal compliance, and tax audits.", "Robert Miller");
            Department ops = new Department("DEP-105", "Operations & IT", "OPS-05",
                    "IT infrastructure support, workstation hardware, logistics and facilities.", "Michael Chang");

            deptMap.put("Human Resources", departmentRepository.save(hr));
            deptMap.put("Software Engineering", departmentRepository.save(eng));
            deptMap.put("Marketing & Sales", departmentRepository.save(mkt));
            deptMap.put("Finance & Accounts", departmentRepository.save(fin));
            deptMap.put("Operations & IT", departmentRepository.save(ops));
        } else {
            departmentRepository.findAll().forEach(d -> deptMap.put(d.getName(), d));
        }

        return deptMap;
    }

    private void seedEmployees(Map<String, Department> deptMap) {
        if (employeeRepository.count() == 0) {
            log.info("No employees found in database. Seeding initial baseline employees...");

            createAndSaveEmployee("EMP-001", "Sarah Jenkins", "Sarah", "Jenkins",
                    "sarah.j@hrgenius.com", "+1 555-0101", deptMap.get("Human Resources"),
                    "HR Director", "HR Director", LocalDate.of(2022, 3, 15), "Active",
                    new BigDecimal("85000.00"), "Female", "Full Time", "Austin", "422 Elm Crest Blvd, Austin, TX");

            createAndSaveEmployee("EMP-002", "David Chen", "David", "Chen",
                    "david.c@hrgenius.com", "+1 555-0102", deptMap.get("Software Engineering"),
                    "Lead Architect", "Lead Architect", LocalDate.of(2021, 6, 10), "Active",
                    new BigDecimal("110000.00"), "Male", "Full Time", "San Francisco", "88 Tech Valley Way, San Francisco, CA");

            createAndSaveEmployee("EMP-003", "Emily Watson", "Emily", "Watson",
                    "emily.w@hrgenius.com", "+1 555-0103", deptMap.get("Marketing & Sales"),
                    "Marketing Head", "Marketing Head", LocalDate.of(2022, 9, 1), "Active",
                    new BigDecimal("78000.00"), "Female", "Full Time", "New York", "12 Madison Ave, New York, NY");

            createAndSaveEmployee("EMP-004", "Robert Miller", "Robert", "Miller",
                    "robert.m@hrgenius.com", "+1 555-0104", deptMap.get("Finance & Accounts"),
                    "Finance Manager", "Finance Manager", LocalDate.of(2020, 1, 20), "Active",
                    new BigDecimal("90000.00"), "Male", "Full Time", "Chicago", "500 Michigan Ave, Chicago, IL");

            createAndSaveEmployee("EMP-005", "Jessica Taylor", "Jessica", "Taylor",
                    "jessica.t@hrgenius.com", "+1 555-0105", deptMap.get("Software Engineering"),
                    "Frontend Developer", "Frontend Developer", LocalDate.of(2023, 2, 14), "Active",
                    new BigDecimal("74000.00"), "Female", "Full Time", "San Jose", "310 Silicon Park, San Jose, CA");

            createAndSaveEmployee("EMP-006", "Alex Rivera", "Alex", "Rivera",
                    "alex.r@hrgenius.com", "+1 555-0106", deptMap.get("Operations & IT"),
                    "IT Support Specialist", "IT Support Specialist", LocalDate.of(2023, 8, 11), "On Leave",
                    new BigDecimal("58000.00"), "Male", "Full Time", "Austin", "104 Riverbend Rd, Austin, TX");
        }
    }

    private void createAndSaveEmployee(String empId, String name, String firstName, String lastName,
                                      String email, String phone, Department department, String job,
                                      String designation, LocalDate joiningDate, String status,
                                      BigDecimal salary, String gender, String employmentType,
                                      String city, String address) {
        Employee emp = new Employee();
        emp.setEmpId(empId);
        emp.setName(name);
        emp.setFirstName(firstName);
        emp.setLastName(lastName);
        emp.setEmail(email);
        emp.setPhone(phone);
        emp.setDepartment(department);
        emp.setJob(job);
        emp.setDesignation(designation);
        emp.setJoiningDate(joiningDate);
        emp.setStatus(status);
        emp.setSalary(salary);
        emp.setGender(gender);
        emp.setEmploymentType(employmentType);
        emp.setCity(city);
        emp.setAddress(address);
        employeeRepository.save(emp);
    }

    private void seedUsers() {
        if (userRepository.count() == 0) {
            log.info("No users found in database. Seeding default system user accounts with BCrypt hashing...");

            userRepository.save(new User("admin", passwordEncoder.encode("admin123"),
                    "admin@hrgenius.com", "Sarah Jenkins", "Admin", "Active"));
            userRepository.save(new User("david_lead", passwordEncoder.encode("manager123"),
                    "david.c@hrgenius.com", "David Chen", "HR Manager", "Active"));
            userRepository.save(new User("emily_mkt", passwordEncoder.encode("recruiter123"),
                    "emily.w@hrgenius.com", "Emily Watson", "Recruiter", "Active"));
            userRepository.save(new User("jessica_dev", passwordEncoder.encode("employee123"),
                    "jessica.t@hrgenius.com", "Jessica Taylor", "Employee", "Active"));
        }
    }

    private void seedJobs() {
        if (jobRepository.count() == 0) {
            log.info("No jobs found in database. Seeding initial baseline jobs...");
            jobRepository.save(new Job("JOB-201", "Senior Full Stack Engineer", "Software Engineering",
                    "Develop enterprise TypeScript and cloud microservice features.", "Open",
                    "San Francisco, CA (Hybrid)", "Full Time", "4+ Years", "$95,000 - $115,000"));
            jobRepository.save(new Job("JOB-202", "HR Specialist", "Human Resources",
                    "Coordinate talent screening, benefits onboarding, and employee engagement.", "Open",
                    "Austin, TX (On-site)", "Full Time", "2+ Years", "$62,000 - $75,000"));
            jobRepository.save(new Job("JOB-203", "UI/UX Product Designer", "Software Engineering",
                    "Lead wireframing, high-fidelity prototypes, and user accessibility testing.", "Open",
                    "Remote", "Full Time", "3+ Years", "$78,000 - $90,000"));
            jobRepository.save(new Job("JOB-204", "Digital Marketing Strategist", "Marketing & Sales",
                    "Execute multi-channel advertising, organic SEO, and conversion analytics.", "Draft",
                    "New York, NY", "Contract", "3+ Years", "$65,000 - $80,000"));
            jobRepository.save(new Job("JOB-205", "Financial Analyst", "Finance & Accounts",
                    "Quarterly forecasting, variance analysis, and cash flow balance sheets.", "Closed",
                    "Chicago, IL", "Full Time", "2+ Years", "$70,000 - $85,000"));
        }
    }

    private void seedCandidates() {
        if (candidateRepository.count() == 0) {
            log.info("No candidates found in database. Seeding initial baseline candidates...");
            candidateRepository.save(new Candidate("CAN-301", "Alex Morgan", "Alex", "Morgan",
                    "alex.m@gmail.com", "+1 555-0211", "Senior Full Stack Engineer", "Interviewing",
                    "Master's in Computer Science", "5 years", "https://hrgenius.local/resumes/alex-morgan.pdf",
                    LocalDate.of(2026, 9, 22)));
            candidateRepository.save(new Candidate("CAN-302", "Chloe Bennett", "Chloe", "Bennett",
                    "chloe.b@gmail.com", "+1 555-0212", "UI/UX Product Designer", "Offered",
                    "B.Des Interaction Design", "4 years", "https://hrgenius.local/resumes/chloe-bennett.pdf",
                    LocalDate.of(2026, 9, 18)));
            candidateRepository.save(new Candidate("CAN-303", "Daniel Brooks", "Daniel", "Brooks",
                    "daniel.b@gmail.com", "+1 555-0213", "HR Specialist", "Applied",
                    "B.B.A Human Resources", "2 years", "https://hrgenius.local/resumes/daniel-brooks.pdf",
                    LocalDate.of(2026, 9, 28)));
            candidateRepository.save(new Candidate("CAN-304", "Sophia Patel", "Sophia", "Patel",
                    "sophia.p@gmail.com", "+1 555-0214", "Digital Marketing Strategist", "Screening",
                    "B.A. Digital Media", "3 years", "https://hrgenius.local/resumes/sophia-patel.pdf",
                    LocalDate.of(2026, 9, 27)));
            candidateRepository.save(new Candidate("CAN-305", "Ryan Cooper", "Ryan", "Cooper",
                    "ryan.c@gmail.com", "+1 555-0215", "Senior Full Stack Engineer", "Hired",
                    "B.S. Software Engineering", "4 years", "https://hrgenius.local/resumes/ryan-cooper.pdf",
                    LocalDate.of(2026, 9, 10)));
        }
    }

    private void seedAttendance() {
        if (attendanceRepository.count() == 0) {
            log.info("No attendance records found. Seeding initial baseline attendance...");
            attendanceRepository.save(new Attendance("David Chen", "EMP-002", LocalDate.of(2026, 10, 1),
                    "08:58 AM", "05:32 PM", "Present", "On time"));
            attendanceRepository.save(new Attendance("Sarah Jenkins", "EMP-001", LocalDate.of(2026, 10, 1),
                    "08:52 AM", "05:15 PM", "Present", "On time"));
            attendanceRepository.save(new Attendance("Jessica Taylor", "EMP-005", LocalDate.of(2026, 10, 1),
                    "09:28 AM", "06:05 PM", "Late", "Metro transit delay"));
            attendanceRepository.save(new Attendance("Alex Rivera", "EMP-006", LocalDate.of(2026, 10, 1),
                    "--", "--", "Absent", "Sick leave approved"));
            attendanceRepository.save(new Attendance("Emily Watson", "EMP-003", LocalDate.of(2026, 10, 1),
                    "09:00 AM", "01:30 PM", "Half Day", "Medical appointment"));
            attendanceRepository.save(new Attendance("Robert Miller", "EMP-004", LocalDate.of(2026, 10, 1),
                    "08:48 AM", "05:20 PM", "Present", "On time"));
        }
    }

    private void seedLeaves() {
        if (leaveRepository.count() == 0) {
            log.info("No leaves found. Seeding initial baseline leave requests...");
            leaveRepository.save(new LeaveRequest("Alex Rivera", "EMP-006", "Sick Leave",
                    LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 3), 3,
                    LocalDate.of(2026, 9, 30), "Viral infection medical rest", "Pending"));
            leaveRepository.save(new LeaveRequest("Emily Watson", "EMP-003", "Casual Leave",
                    LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 6), 2,
                    LocalDate.of(2026, 9, 29), "Attending family anniversary", "Approved"));
            leaveRepository.save(new LeaveRequest("David Chen", "EMP-002", "Earned Leave",
                    LocalDate.of(2026, 9, 12), LocalDate.of(2026, 9, 16), 5,
                    LocalDate.of(2026, 9, 1), "Annual holiday vacation", "Approved"));
            leaveRepository.save(new LeaveRequest("Jessica Taylor", "EMP-005", "Emergency Leave",
                    LocalDate.of(2026, 9, 24), LocalDate.of(2026, 9, 24), 1,
                    LocalDate.of(2026, 9, 24), "Plumbing home emergency", "Approved"));
        }
    }

    private void seedPayrolls() {
        if (payrollRepository.count() == 0) {
            log.info("No payroll records found. Seeding initial baseline payroll...");
            payrollRepository.save(new Payroll("David Chen", "EMP-002", new BigDecimal("9166.00"),
                    new BigDecimal("800.00"), new BigDecimal("600.00"), new BigDecimal("1000.00"),
                    new BigDecimal("10366.00"), LocalDate.of(2026, 9, 30), "September 2026", "Paid"));
            payrollRepository.save(new Payroll("Sarah Jenkins", "EMP-001", new BigDecimal("7083.00"),
                    new BigDecimal("650.00"), new BigDecimal("450.00"), new BigDecimal("500.00"),
                    new BigDecimal("7783.00"), LocalDate.of(2026, 9, 30), "September 2026", "Paid"));
            payrollRepository.save(new Payroll("Emily Watson", "EMP-003", new BigDecimal("6500.00"),
                    new BigDecimal("500.00"), new BigDecimal("400.00"), new BigDecimal("400.00"),
                    new BigDecimal("7000.00"), LocalDate.of(2026, 9, 30), "September 2026", "Paid"));
            payrollRepository.save(new Payroll("Robert Miller", "EMP-004", new BigDecimal("7500.00"),
                    new BigDecimal("600.00"), new BigDecimal("500.00"), new BigDecimal("500.00"),
                    new BigDecimal("8100.00"), LocalDate.of(2026, 9, 30), "September 2026", "Paid"));
            payrollRepository.save(new Payroll("Jessica Taylor", "EMP-005", new BigDecimal("6166.00"),
                    new BigDecimal("400.00"), new BigDecimal("350.00"), new BigDecimal("300.00"),
                    new BigDecimal("6516.00"), LocalDate.of(2026, 9, 30), "September 2026", "Paid"));
            payrollRepository.save(new Payroll("Alex Rivera", "EMP-006", new BigDecimal("4833.00"),
                    new BigDecimal("300.00"), new BigDecimal("280.00"), new BigDecimal("200.00"),
                    new BigDecimal("5053.00"), LocalDate.of(2026, 10, 5), "September 2026", "Pending"));
        }
    }

    private void seedPerformance() {
        if (performanceRepository.count() == 0) {
            log.info("No performance reviews found. Seeding initial baseline performance reviews...");
            performanceRepository.save(new PerformanceReview("David Chen", "EMP-002",
                    LocalDate.of(2026, 9, 25), 5,
                    "Architect real-time streaming pipelines",
                    "Reduced service latency by 42% across core endpoints",
                    "Exceptional technical vision and collaborative leadership across team."));
            performanceRepository.save(new PerformanceReview("Jessica Taylor", "EMP-005",
                    LocalDate.of(2026, 9, 20), 4,
                    "Revamp design tokens and component accessibility",
                    "Delivered WCAG 2.1 AA compliant UI library 1 week early",
                    "Outstanding attention to user experience details and high work quality."));
            performanceRepository.save(new PerformanceReview("Sarah Jenkins", "EMP-001",
                    LocalDate.of(2026, 9, 18), 5,
                    "Implement streamlined applicant tracking workflow",
                    "Shortened average time-to-hire from 42 days to 24 days",
                    "Strategic HR partner who excels at talent retention and team culture."));
            performanceRepository.save(new PerformanceReview("Emily Watson", "EMP-003",
                    LocalDate.of(2026, 9, 15), 4,
                    "Drive 25% growth in qualified pipeline leads",
                    "Achieved 31% Q3 expansion with optimized webinar campaigns",
                    "Creative, metrics-driven approach to inbound marketing growth."));
        }
    }

    private void seedInterviews() {
        if (interviewRepository.count() == 0) {
            log.info("No interviews found. Seeding initial baseline interviews...");
            interviewRepository.save(new Interview("Alex Morgan", "Senior Full Stack Engineer",
                    "2026-10-03 14:00", "David Chen", "Online", "Scheduled",
                    "System architecture and coding challenge evaluation."));
            interviewRepository.save(new Interview("Chloe Bennett", "UI/UX Product Designer",
                    "2026-09-28 11:30", "Sarah Jenkins & Emily Watson", "In-Person", "Completed",
                    "Stunning portfolio presentation. High aesthetic standards and strong user empathy."));
            interviewRepository.save(new Interview("Daniel Brooks", "HR Specialist",
                    "2026-10-04 10:00", "Sarah Jenkins", "Online", "Scheduled",
                    "HR policy review and situational behavioral questions."));
            interviewRepository.save(new Interview("Sophia Patel", "Digital Marketing Strategist",
                    "2026-10-02 15:30", "Emily Watson", "In-Person", "Scheduled",
                    "Campaign case study presentation and performance metrics review."));
        }
    }

    private void seedTrainings() {
        if (trainingRepository.count() == 0) {
            log.info("No trainings found. Seeding initial baseline training programs...");
            trainingRepository.save(new TrainingProgram("Jessica Taylor", "Cloud Solutions Architecture",
                    "AWS Learning Team", LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 24), "Upcoming"));
            trainingRepository.save(new TrainingProgram("David Chen", "Advanced Microservices Security & Zero Trust",
                    "CyberSec Global Institute", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 28), "Completed"));
            trainingRepository.save(new TrainingProgram("Sarah Jenkins", "Modern People Leadership & DEI Strategies",
                    "SHRM Executive Faculty", LocalDate.of(2026, 9, 15), LocalDate.of(2026, 10, 15), "In Progress"));
            trainingRepository.save(new TrainingProgram("Emily Watson", "AI-Powered Growth Marketing & SEO",
                    "HubSpot Academy Certified", LocalDate.of(2026, 10, 12), LocalDate.of(2026, 10, 26), "Upcoming"));
        }
    }

    private void seedDocuments() {
        if (documentRepository.count() == 0) {
            log.info("No documents found. Seeding initial baseline documents...");
            documentRepository.save(new DocumentRecord("David Chen", "Executive Employment Agreement",
                    "Contract", "/documents/david_chen_contract.pdf", LocalDate.of(2021, 6, 10), "Verified"));
            documentRepository.save(new DocumentRecord("Sarah Jenkins", "Official Passport & ID Verification",
                    "ID Proof", "/documents/sarah_id_proof.pdf", LocalDate.of(2022, 3, 15), "Verified"));
            documentRepository.save(new DocumentRecord("Jessica Taylor", "B.S. Computer Science Degree Certificate",
                    "Certificate", "/documents/jessica_degree.pdf", LocalDate.of(2023, 2, 14), "Verified"));
            documentRepository.save(new DocumentRecord("Alex Rivera", "Medical Leave Physician Certificate",
                    "Policy", "/documents/alex_medical_cert.pdf", LocalDate.of(2026, 10, 1), "Pending"));
        }
    }
}

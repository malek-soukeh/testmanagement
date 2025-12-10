package com.example.testmanagement;

import com.example.testmanagement.Entities.*;
import com.example.testmanagement.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TestSuiteRepository testSuiteRepository;
    private final TestCaseRepository testCaseRepository;
    private final TestCaseStepRepository testCaseStepRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        User admin = createUserIfNotFound("admin@test.com", "Admin", "User", "admin123", Role.ROLE_ADMIN);
        User tester = createUserIfNotFound("tester@test.com", "Tester", "User", "tester123", Role.ROLE_TESTER);
        User auditor = createUserIfNotFound("auditor@test.com", "Auditor", "User", "auditor123", Role.ROLE_AUDIT);

        if (projectRepository.count() == 0) {
            seedDemoData(tester);
        }
    }

    private User createUserIfNotFound(String email, String firstName, String lastName, String password, Role role) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User user = new User();
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole(role);
            user.setEnabled(true);
            var saved = userRepository.save(user);
            System.out.println("Seeded User: " + email);
            return saved;
        });
    }

    private void seedDemoData(User creator) {
        System.out.println("Starting Demo Data Seeding...");

        // Project
        Project project = new Project();
        project.setProjectName("Demo E-Commerce");
        project.setDescription("A demo project for testing automation and performance.");
        project.setCreatedBy(creator);
        project.setAssignedTo(creator);
        project.setStatus(Project.Status.ACTIVE);
        project = projectRepository.save(project);

        // Suite 1: Frontend
        TestSuite suiteFe = new TestSuite();
        suiteFe.setSuiteName("Frontend Automation");
        suiteFe.setDescription("UI Tests for the storefront.");
        suiteFe.setProject(project);
        suiteFe.setCreatedBy(creator);
        suiteFe = testSuiteRepository.save(suiteFe);

        // Case 1: Automated (Selenium)
        TestCase tcAuto = new TestCase();
        tcAuto.setTitle("User Login Flow");
        tcAuto.setDescription("Verify that a user can login with valid credentials.");
        tcAuto.setTestType(TestCase.TestType.AUTOMATED);
        tcAuto.setPriority(TestCase.Priority.CRITICAL);
        tcAuto.setStatus(TestCase.Status.READY);
        tcAuto.setTestSuite(suiteFe);
        tcAuto.setCreatedBy(creator);
        tcAuto = testCaseRepository.save(tcAuto);

        // Steps for TC Auto
        List<TestCaseStep> steps = new ArrayList<>();
        steps.add(createStep("Open Login Page", "OPEN_URL", "", "http://localhost:3000/login", tcAuto, creator));
        steps.add(createStep("Enter Username", "TYPE", "id=username", "testuser", tcAuto, creator));
        steps.add(createStep("Enter Password", "TYPE", "id=password", "password123", tcAuto, creator));
        steps.add(createStep("Click Login", "CLICK", "id=login-btn", "", tcAuto, creator));
        steps.add(createStep("Verify Dashboard", "ASSERT_TEXT", "id=welcome-msg", "Welcome", tcAuto, creator));
        testCaseStepRepository.saveAll(steps);

        // Suite 2: Performance
        TestSuite suitePerf = new TestSuite();
        suitePerf.setSuiteName("Backend Performance");
        suitePerf.setDescription("Load testing for API endpoints.");
        suitePerf.setProject(project);
        suitePerf.setCreatedBy(creator);
        suitePerf = testSuiteRepository.save(suitePerf);

        // Case 2: Performance (JMeter)
        TestCase tcPerf = new TestCase();
        tcPerf.setTitle("API Load Test");
        tcPerf.setDescription("Load test the product search API.");
        tcPerf.setTestType(TestCase.TestType.PERFORMANCE);
        tcPerf.setPriority(TestCase.Priority.HIGH);
        tcPerf.setStatus(TestCase.Status.READY);
        tcPerf.setTestSuite(suitePerf);
        tcPerf.setCreatedBy(creator);
        // JMeter / Performance Config JSON
        tcPerf.setPerformanceConfig(
                "{\"users\": 100, \"rampUpSeconds\": 60, \"loopCount\": 10, \"durationSeconds\": 300, \"targetUrl\": \"http://localhost:8080/api/products\"}");
        testCaseRepository.save(tcPerf);

        // Case 3: Manual
        TestCase tcMan = new TestCase();
        tcMan.setTitle("Visual Inspection of Landing Page");
        tcMan.setDescription("Check for alignment issues on the homepage.");
        tcMan.setTestType(TestCase.TestType.MANUAL);
        tcMan.setPriority(TestCase.Priority.LOW);
        tcMan.setStatus(TestCase.Status.DRAFT);
        tcMan.setTestSuite(suiteFe);
        tcMan.setCreatedBy(creator);
        tcMan.setPrecondition("Browser must be Chrome v90+");
        testCaseRepository.save(tcMan);

        System.out.println("Seeded Demo Project Data Successfully.");
    }

    private TestCaseStep createStep(String name, String actionType, String target, String value, TestCase tc,
            User creator) {
        TestCaseStep step = new TestCaseStep();
        step.setStepName(name);
        step.setActionType(actionType);
        step.setActionTarget(target);
        step.setActionValue(value);
        step.setTestCase(tc);
        step.setCreatedBy(creator);
        step.setExpectedResult("Step should succeed");
        return step;
    }
}

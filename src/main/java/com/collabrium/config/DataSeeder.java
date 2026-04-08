package com.collabrium.config;

import com.collabrium.model.*;
import com.collabrium.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final MilestoneRepository milestoneRepository;
    private final DocumentRepository documentRepository;
    private final AnnouncementRepository announcementRepository;
    private final DiscussionThreadRepository threadRepository;
    private final MessageRepository messageRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, ProjectRepository projectRepository,
                      MilestoneRepository milestoneRepository, DocumentRepository documentRepository,
                      AnnouncementRepository announcementRepository, DiscussionThreadRepository threadRepository,
                      MessageRepository messageRepository, NotificationRepository notificationRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.milestoneRepository = milestoneRepository;
        this.documentRepository = documentRepository;
        this.announcementRepository = announcementRepository;
        this.threadRepository = threadRepository;
        this.messageRepository = messageRepository;
        this.notificationRepository = notificationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (projectRepository.count() > 0) {
            return; // Data already exists
        }

        // 1. Create Extra Users
        User admin = userRepository.findByEmail("2400031420@kluniversity.in").orElse(null);
        if (admin == null) {
            admin = User.builder()
                    .name("Venu Madhav")
                    .email("2400031420@kluniversity.in")
                    .password(passwordEncoder.encode("Venuvenven@12313"))
                    .role(Role.ADMIN)
                    .build();
            admin = userRepository.save(admin);
        }

        User researcher1 = User.builder()
                .name("Dr. Sarah Smith")
                .email("researcher@collabrium.edu")
                .password(passwordEncoder.encode("Demo@1234"))
                .role(Role.RESEARCHER)
                .build();
        researcher1 = userRepository.save(researcher1);

        User researcher2 = User.builder()
                .name("John Doe")
                .email("john.doe@collabrium.edu")
                .password(passwordEncoder.encode("Demo@1234"))
                .role(Role.RESEARCHER)
                .build();
        researcher2 = userRepository.save(researcher2);

        // 2. Create Projects
        Project p1 = Project.builder()
                .title("AI-Driven Healthcare Diagnostics")
                .description("A collaborative research project focused on building deep learning models for early cancer detection.")
                .status(ProjectStatus.ACTIVE)
                .createdBy(admin)
                .build();
        p1 = projectRepository.save(p1);

        Project p2 = Project.builder()
                .title("Quantum Computing in Finance")
                .description("Exploring quantum algorithms for portfolio optimization and high-frequency trading simulation.")
                .status(ProjectStatus.AT_RISK)
                .createdBy(researcher1)
                .build();
        p2 = projectRepository.save(p2);

        Project p3 = Project.builder()
                .title("Smart City Sustainability Initiative")
                .description("Designing IoT sensors for real-time monitoring of air quality and energy consumption in urban areas.")
                .status(ProjectStatus.COMPLETED)
                .createdBy(researcher2)
                .build();
        p3 = projectRepository.save(p3);

        Project p4 = Project.builder()
                .title("Neuroscience and Brain-Machine Interfaces")
                .description("Integrating non-invasive neural sensors with robotic prosthetic controls.")
                .status(ProjectStatus.ACTIVE)
                .createdBy(admin)
                .build();
        p4 = projectRepository.save(p4);

        // 3. Create Milestones
        seedMilestones(p1, Arrays.asList(
                createMilestone(p1, "Data Collection Phase", LocalDate.now().plusMonths(1), MilestoneStatus.COMPLETED, 100),
                createMilestone(p1, "Model Architecture Selection", LocalDate.now().plusMonths(2), MilestoneStatus.IN_PROGRESS, 45),
                createMilestone(p1, "Clinical Validation Study", LocalDate.now().plusMonths(6), MilestoneStatus.PENDING, 0)
        ));

        seedMilestones(p2, Arrays.asList(
                createMilestone(p2, "Literature Review", LocalDate.now().minusMonths(1), MilestoneStatus.COMPLETED, 100),
                createMilestone(p2, "Quantum Circuit Design", LocalDate.now().plusWeeks(2), MilestoneStatus.IN_PROGRESS, 60),
                createMilestone(p2, "Budget Approval (Pending)", LocalDate.now().plusWeeks(1), MilestoneStatus.PENDING, 10)
        ));

        seedMilestones(p3, Arrays.asList(
                createMilestone(p3, "Initial Research", LocalDate.now().minusMonths(12), MilestoneStatus.COMPLETED, 100),
                createMilestone(p3, "Hardware Deployment", LocalDate.now().minusMonths(6), MilestoneStatus.COMPLETED, 100),
                createMilestone(p3, "Final Report", LocalDate.now().minusMonths(1), MilestoneStatus.COMPLETED, 100)
        ));

        // 4. Create Documents
        seedDocument(p1, admin, "cancer_detection_dataset_v1.pdf", "Dataset", "2.4 MB");
        seedDocument(p1, admin, "resnet_arch_proposal.docx", "Proposal", "850 KB");
        seedDocument(p2, researcher1, "quantum_finance_intro.pptx", "Slides", "5.1 MB");
        seedDocument(p4, researcher2, "neural_interface_spec.pdf", "Technical Docs", "1.2 MB");

        // 5. Create Announcements
        createAnnouncement("System Maintenance Scheduled", "The Collabrium platform will be undergoing maintenance this weekend from Saturday 10 PM to Sunday 4 AM UTC.", admin, "high");
        createAnnouncement("Grant Writing Workshop", "Join us for a workshop on writing effective research grants on Friday at 2 PM.", researcher1, "medium");
        createAnnouncement("Welcome to Collabrium!", "We are excited to launch our new research management and collaboration platform.", admin, "low");

        // 6. Create Discussions and Messages
        DiscussionThread t1 = createThread(p1, admin, "Model Architecture Ideas");
        createMessage(t1, researcher1, "I think we should try the new Vision Transformer (ViT) architecture instead of ResNet.");
        createMessage(t1, admin, "Agreed. Let's set up a comparison study for next week.");
        createMessage(t1, researcher2, "I can help with the data preprocessing for ViT.");

        DiscussionThread t2 = createThread(p2, researcher1, "Budget Constraints");
        createMessage(t2, researcher1, "We are hitting significant roadblocks with hardware costs. Any ideas?");
        createMessage(t2, admin, "I'll look into additional institutional grants from the department.");

        // 7. Create Notifications
        createNotification(admin, "New Message", "Dr. Sarah Smith replied to your thread 'Model Architecture Ideas'", "discussion");
        createNotification(researcher1, "Project Update", "Your project 'Quantum Computing in Finance' is now marked as AT RISK.", "system");
        createNotification(researcher2, "Task Completed", "Hardware Deployment milestone was successful.", "system");
        createNotification(admin, "Welcome", "Welcome to the research portal! Explore and collaborate.", "system");
    }

    private Milestone createMilestone(Project project, String title, LocalDate dueDate, MilestoneStatus status, int progress) {
        Milestone m = new Milestone();
        m.setProject(project);
        m.setTitle(title);
        m.setDueDate(dueDate);
        m.setStatus(status);
        m.setProgressPercentage(progress);
        return m;
    }

    private void seedMilestones(Project project, List<Milestone> milestones) {
        milestoneRepository.saveAll(milestones);
    }

    private void seedDocument(Project project, User uploader, String fileName, String tag, String size) {
        Document doc = Document.builder()
                .project(project)
                .uploadedBy(uploader)
                .fileName(fileName)
                .fileUrl("")
                .version("1.0.0")
                .build();
        doc.setTag(tag);
        doc.setFileSize(size);
        documentRepository.save(doc);
    }

    private void createAnnouncement(String title, String content, User author, String priority) {
        Announcement ann = new Announcement();
        ann.setTitle(title);
        ann.setContent(content);
        ann.setAuthor(author);
        ann.setPriority(priority);
        announcementRepository.save(ann);
    }

    private DiscussionThread createThread(Project project, User creator, String title) {
        DiscussionThread thread = new DiscussionThread();
        thread.setProject(project);
        thread.setCreatedBy(creator);
        thread.setTitle(title);
        thread.setStatus("ACTIVE");
        return threadRepository.save(thread);
    }

    private void createMessage(DiscussionThread thread, User sender, String content) {
        Message msg = new Message();
        msg.setThread(thread);
        msg.setSender(sender);
        msg.setContent(content);
        messageRepository.save(msg);
        
        // Update thread's updatedAt via a touch
        thread.setUpdatedAt(LocalDateTime.now());
        threadRepository.save(thread);
    }

    private void createNotification(User user, String title, String message, String type) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRead(false);
        notificationRepository.save(notification);
    }
}

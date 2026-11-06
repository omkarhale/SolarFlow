package com.solarflow.project;

import com.solarflow.common.exception.BadRequestException;
import com.solarflow.common.exception.NotFoundException;
import com.solarflow.common.security.CurrentUser;
import com.solarflow.customer.domain.CustomerRepository;
import com.solarflow.project.domain.Project;
import com.solarflow.project.domain.ProjectPhoto;
import com.solarflow.project.domain.ProjectPhotoRepository;
import com.solarflow.project.domain.ProjectRepository;
import com.solarflow.project.domain.ProjectTask;
import com.solarflow.project.domain.ProjectTaskRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

  private static final Set<String> PHOTO_TYPES = Set.of("image/png", "image/jpeg");

  private final ProjectRepository projects;
  private final ProjectTaskRepository tasks;
  private final ProjectPhotoRepository photos;
  private final CustomerRepository customers;
  private final CurrentUser currentUser;
  private final Path storageDir;

  public ProjectController(
      ProjectRepository projects,
      ProjectTaskRepository tasks,
      ProjectPhotoRepository photos,
      CustomerRepository customers,
      CurrentUser currentUser,
      @Value("${solarflow.storage-dir}") String storageDir
  ) {
    this.projects = projects;
    this.tasks = tasks;
    this.photos = photos;
    this.customers = customers;
    this.currentUser = currentUser;
    this.storageDir = Path.of(storageDir);
  }

  @GetMapping
  @Transactional(readOnly = true)
  public List<ProjectView> list() {
    return projects.findByCompanyIdOrderByCreatedAtDesc(companyId()).stream().map(ProjectView::from).toList();
  }

  @GetMapping("/{id}")
  @Transactional(readOnly = true)
  public ProjectDetail get(@PathVariable UUID id) {
    Project project = require(id);
    var taskViews = tasks.findByProjectIdAndCompanyIdOrderBySortOrderAsc(id, companyId()).stream().map(TaskView::from).toList();
    var photoViews = photos.findByProjectIdAndCompanyIdOrderByCreatedAtDesc(id, companyId()).stream().map(PhotoView::from).toList();
    return new ProjectDetail(ProjectView.from(project), taskViews, photoViews);
  }

  @PostMapping(value = "/{id}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Transactional
  public ProjectDetail upload(
      @PathVariable UUID id,
      @RequestPart("file") MultipartFile file,
      @RequestParam(required = false) String caption
  ) throws IOException {
    Project project = require(id);
    if (file.isEmpty() || file.getContentType() == null || !PHOTO_TYPES.contains(file.getContentType())) {
      throw new BadRequestException("Upload a PNG or JPG photo");
    }
    UUID photoId = UUID.randomUUID();
    String extension = "image/png".equals(file.getContentType()) ? "png" : "jpg";
    Path directory = storageDir.resolve(project.getCompanyId().toString()).resolve(project.getId().toString());
    Files.createDirectories(directory);
    Path target = directory.resolve(photoId + "." + extension);
    Files.copy(file.getInputStream(), target);
    ProjectPhoto photo = new ProjectPhoto();
    photo.setId(photoId);
    photo.setCompanyId(project.getCompanyId());
    photo.setProjectId(project.getId());
    photo.setFileName(file.getOriginalFilename() == null ? target.getFileName().toString() : file.getOriginalFilename());
    photo.setStorageKey(storageDir.relativize(target).toString());
    photo.setCaption(caption == null || caption.isBlank() ? null : caption.trim());
    photos.save(photo);
    return get(id);
  }

  @GetMapping("/{id}/photos/{photoId}")
  @Transactional(readOnly = true)
  public ResponseEntity<Resource> download(@PathVariable UUID id, @PathVariable UUID photoId) throws IOException {
    require(id);
    ProjectPhoto photo = photos.findByIdAndCompanyId(photoId, companyId())
        .filter(item -> id.equals(item.getProjectId()))
        .orElseThrow(NotFoundException::new);
    Path path = storageDir.resolve(photo.getStorageKey()).normalize();
    if (!path.startsWith(storageDir.normalize())) {
      throw new NotFoundException();
    }
    Resource resource = new UrlResource(path.toUri());
    String type = photo.getFileName().toLowerCase().endsWith(".png") ? "image/png" : "image/jpeg";
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + photo.getFileName().replace("\"", "") + "\"")
        .contentType(MediaType.parseMediaType(type))
        .body(resource);
  }

  @PostMapping
  @Transactional
  public ProjectView create(@Valid @RequestBody ProjectInput input) {
    if (customers.findByIdAndCompanyId(input.customerId(), companyId()).isEmpty()) {
      throw new NotFoundException();
    }
    Project project = new Project();
    project.setCompanyId(companyId());
    apply(project, input);
    return ProjectView.from(projects.save(project));
  }

  @PatchMapping("/{id}")
  @Transactional
  public ProjectView update(@PathVariable UUID id, @Valid @RequestBody ProjectInput input) {
    Project project = require(id);
    apply(project, input);
    return ProjectView.from(project);
  }

  @PostMapping("/{id}/tasks")
  @Transactional
  public TaskView addTask(@PathVariable UUID id, @Valid @RequestBody TaskInput input) {
    Project project = require(id);
    ProjectTask task = new ProjectTask();
    task.setCompanyId(project.getCompanyId());
    task.setProjectId(project.getId());
    task.setTitle(input.title().trim());
    task.setDescription(input.description());
    task.setStatus(input.status());
    task.setAssignedTo(input.assignedTo());
    task.setDueOn(input.dueOn());
    task.setSortOrder(input.sortOrder() == null ? 0 : input.sortOrder());
    return TaskView.from(tasks.save(task));
  }

  @PatchMapping("/{id}/tasks/{taskId}")
  @Transactional
  public TaskView updateTask(@PathVariable UUID id, @PathVariable UUID taskId, @Valid @RequestBody TaskInput input) {
    require(id);
    ProjectTask task = tasks.findByIdAndCompanyId(taskId, companyId())
        .filter(item -> id.equals(item.getProjectId()))
        .orElseThrow(NotFoundException::new);
    task.setTitle(input.title().trim());
    task.setDescription(input.description());
    task.setStatus(input.status());
    task.setAssignedTo(input.assignedTo());
    task.setDueOn(input.dueOn());
    task.setSortOrder(input.sortOrder() == null ? task.getSortOrder() : input.sortOrder());
    return TaskView.from(task);
  }

  private void apply(Project project, ProjectInput input) {
    if (input.progressPercent() < 0 || input.progressPercent() > 100) {
      throw new com.solarflow.common.exception.BadRequestException("Progress must be between 0 and 100");
    }
    project.setCustomerId(input.customerId());
    project.setName(input.name().trim());
    project.setStatus(input.status());
    project.setCapacityKw(input.capacityKw());
    project.setSiteAddress(input.siteAddress());
    project.setStartDate(input.startDate());
    project.setTargetDate(input.targetDate());
    project.setCompletedDate(input.completedDate());
    project.setLeadTechnicianId(input.leadTechnicianId());
    project.setProgressPercent(input.progressPercent());
  }

  private Project require(UUID id) {
    return projects.findByIdAndCompanyId(id, companyId()).orElseThrow(NotFoundException::new);
  }

  private UUID companyId() {
    return currentUser.require().companyId();
  }

  public record ProjectInput(
      @NotNull UUID customerId,
      @NotBlank @Size(max = 160) String name,
      @NotNull Project.Status status,
      BigDecimal capacityKw,
      @Size(max = 240) String siteAddress,
      LocalDate startDate,
      LocalDate targetDate,
      LocalDate completedDate,
      UUID leadTechnicianId,
      @NotNull Integer progressPercent
  ) {
  }

  public record TaskInput(
      @NotBlank @Size(max = 160) String title,
      @Size(max = 2000) String description,
      @NotNull ProjectTask.Status status,
      UUID assignedTo,
      LocalDate dueOn,
      Integer sortOrder
  ) {
  }

  public record ProjectView(
      UUID id, UUID customerId, UUID quotationId, String name, Project.Status status, BigDecimal capacityKw,
      String siteAddress, LocalDate startDate, LocalDate targetDate, LocalDate completedDate,
      UUID leadTechnicianId, int progressPercent
  ) {
    static ProjectView from(Project project) {
      return new ProjectView(
          project.getId(), project.getCustomerId(), project.getQuotationId(), project.getName(), project.getStatus(),
          project.getCapacityKw(), project.getSiteAddress(), project.getStartDate(), project.getTargetDate(),
          project.getCompletedDate(), project.getLeadTechnicianId(), project.getProgressPercent()
      );
    }
  }

  public record TaskView(UUID id, String title, String description, ProjectTask.Status status, UUID assignedTo, LocalDate dueOn, int sortOrder) {
    static TaskView from(ProjectTask task) {
      return new TaskView(task.getId(), task.getTitle(), task.getDescription(), task.getStatus(), task.getAssignedTo(), task.getDueOn(), task.getSortOrder());
    }
  }

  public record PhotoView(UUID id, String fileName, String caption) {
    static PhotoView from(ProjectPhoto photo) {
      return new PhotoView(photo.getId(), photo.getFileName(), photo.getCaption());
    }
  }

  public record ProjectDetail(ProjectView project, List<TaskView> tasks, List<PhotoView> photos) {
  }
}

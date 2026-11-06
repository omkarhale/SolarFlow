package com.solarflow.survey;

import com.solarflow.common.exception.BadRequestException;
import com.solarflow.common.exception.NotFoundException;
import com.solarflow.common.security.CurrentUser;
import com.solarflow.lead.domain.LeadRepository;
import com.solarflow.survey.domain.SiteSurvey;
import com.solarflow.survey.domain.SiteSurveyRepository;
import com.solarflow.survey.domain.SurveyPhoto;
import com.solarflow.survey.domain.SurveyPhotoRepository;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/surveys")
public class SurveyController {

  private static final Set<String> TYPES = Set.of("image/jpeg", "image/png");

  private final SiteSurveyRepository surveys;
  private final SurveyPhotoRepository photos;
  private final LeadRepository leads;
  private final CurrentUser currentUser;
  private final Path storageDir;

  public SurveyController(
      SiteSurveyRepository surveys,
      SurveyPhotoRepository photos,
      LeadRepository leads,
      CurrentUser currentUser,
      @Value("${solarflow.storage-dir}") String storageDir
  ) {
    this.surveys = surveys;
    this.photos = photos;
    this.leads = leads;
    this.currentUser = currentUser;
    this.storageDir = Path.of(storageDir);
  }

  @GetMapping
  @Transactional(readOnly = true)
  public List<SurveyView> list() {
    return surveys.findByCompanyIdOrderByCreatedAtDesc(companyId()).stream().map(this::view).toList();
  }

  @GetMapping("/{id}")
  @Transactional(readOnly = true)
  public SurveyView get(@PathVariable UUID id) {
    return view(require(id));
  }

  @PostMapping
  @Transactional
  public SurveyView create(@Valid @RequestBody SurveyInput input) {
    SiteSurvey survey = new SiteSurvey();
    survey.setCompanyId(companyId());
    apply(survey, input);
    return view(surveys.save(survey));
  }

  @PatchMapping("/{id}")
  @Transactional
  public SurveyView update(@PathVariable UUID id, @Valid @RequestBody SurveyInput input) {
    SiteSurvey survey = require(id);
    apply(survey, input);
    return view(survey);
  }

  @PostMapping(value = "/{id}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Transactional
  public SurveyView upload(@PathVariable UUID id, @RequestPart("file") MultipartFile file) throws IOException {
    SiteSurvey survey = require(id);
    if (file.isEmpty() || file.getContentType() == null || !TYPES.contains(file.getContentType())) {
      throw new BadRequestException("Upload a PNG or JPG photo");
    }
    UUID photoId = UUID.randomUUID();
    String extension = "image/png".equals(file.getContentType()) ? "png" : "jpg";
    Path directory = storageDir.resolve(survey.getCompanyId().toString()).resolve(survey.getId().toString());
    Files.createDirectories(directory);
    Path target = directory.resolve(photoId + "." + extension);
    Files.copy(file.getInputStream(), target);
    SurveyPhoto photo = new SurveyPhoto();
    photo.setId(photoId);
    photo.setCompanyId(survey.getCompanyId());
    photo.setSurveyId(survey.getId());
    photo.setFileName(file.getOriginalFilename() == null ? target.getFileName().toString() : file.getOriginalFilename());
    photo.setStorageKey(storageDir.relativize(target).toString());
    photo.setContentType(file.getContentType());
    photos.save(photo);
    return view(survey);
  }

  @GetMapping("/{id}/photos/{photoId}")
  @Transactional(readOnly = true)
  public ResponseEntity<Resource> download(@PathVariable UUID id, @PathVariable UUID photoId) throws IOException {
    require(id);
    SurveyPhoto photo = photos.findByIdAndCompanyId(photoId, companyId())
        .filter(item -> id.equals(item.getSurveyId()))
        .orElseThrow(NotFoundException::new);
    Path path = storageDir.resolve(photo.getStorageKey()).normalize();
    if (!path.startsWith(storageDir.normalize())) {
      throw new NotFoundException();
    }
    Resource resource = new UrlResource(path.toUri());
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + photo.getFileName() + "\"")
        .contentType(MediaType.parseMediaType(photo.getContentType()))
        .body(resource);
  }

  private void apply(SiteSurvey survey, SurveyInput input) {
    if (leads.findByIdAndCompanyId(input.leadId(), survey.getCompanyId()).isEmpty()) {
      throw new NotFoundException();
    }
    survey.setLeadId(input.leadId());
    survey.setCustomerId(input.customerId());
    survey.setStatus(input.status());
    survey.setScheduledOn(input.scheduledOn());
    survey.setSurveyedBy(input.surveyedBy());
    survey.setRoofType(input.roofType());
    survey.setRoofAreaSqft(input.roofAreaSqft());
    survey.setOrientation(input.orientation());
    survey.setShadingNotes(input.shadingNotes());
    survey.setMonthlyBill(input.monthlyBill());
    survey.setSanctionedLoadKw(input.sanctionedLoadKw());
    survey.setConnectionType(input.connectionType());
    survey.setNotes(input.notes());
  }

  private SurveyView view(SiteSurvey survey) {
    var images = photos.findBySurveyIdAndCompanyIdOrderByCreatedAtDesc(survey.getId(), survey.getCompanyId()).stream()
        .map(photo -> new PhotoView(photo.getId(), photo.getFileName(), photo.getContentType()))
        .toList();
    return SurveyView.from(survey, images);
  }

  private SiteSurvey require(UUID id) {
    return surveys.findByIdAndCompanyId(id, companyId()).orElseThrow(NotFoundException::new);
  }

  private UUID companyId() {
    return currentUser.require().companyId();
  }

  public record SurveyInput(
      @NotNull UUID leadId,
      UUID customerId,
      @NotNull SiteSurvey.Status status,
      LocalDate scheduledOn,
      UUID surveyedBy,
      @Size(max = 80) String roofType,
      BigDecimal roofAreaSqft,
      @Size(max = 32) String orientation,
      @Size(max = 500) String shadingNotes,
      BigDecimal monthlyBill,
      BigDecimal sanctionedLoadKw,
      @Size(max = 32) String connectionType,
      @Size(max = 2000) String notes
  ) {
  }

  public record PhotoView(UUID id, String fileName, String contentType) {
  }

  public record SurveyView(
      UUID id,
      UUID leadId,
      UUID customerId,
      SiteSurvey.Status status,
      LocalDate scheduledOn,
      UUID surveyedBy,
      String roofType,
      BigDecimal roofAreaSqft,
      String orientation,
      String shadingNotes,
      BigDecimal monthlyBill,
      BigDecimal sanctionedLoadKw,
      String connectionType,
      String notes,
      List<PhotoView> photos
  ) {
    static SurveyView from(SiteSurvey survey, List<PhotoView> photos) {
      return new SurveyView(
          survey.getId(), survey.getLeadId(), survey.getCustomerId(), survey.getStatus(), survey.getScheduledOn(),
          survey.getSurveyedBy(), survey.getRoofType(), survey.getRoofAreaSqft(), survey.getOrientation(),
          survey.getShadingNotes(), survey.getMonthlyBill(), survey.getSanctionedLoadKw(), survey.getConnectionType(),
          survey.getNotes(), photos
      );
    }
  }
}

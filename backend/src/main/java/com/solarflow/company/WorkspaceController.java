package com.solarflow.company;

import com.solarflow.auth.UserResponse;
import com.solarflow.common.exception.BadRequestException;
import com.solarflow.common.exception.NotFoundException;
import com.solarflow.common.security.AuthUser;
import com.solarflow.common.security.CurrentUser;
import com.solarflow.company.domain.Company;
import com.solarflow.company.domain.CompanyRepository;
import com.solarflow.user.domain.UserAccount;
import com.solarflow.user.domain.UserAccountRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class WorkspaceController {

  private final CompanyRepository companies;
  private final UserAccountRepository users;
  private final CurrentUser currentUser;
  private final PasswordEncoder passwordEncoder;

  public WorkspaceController(
      CompanyRepository companies,
      UserAccountRepository users,
      CurrentUser currentUser,
      PasswordEncoder passwordEncoder
  ) {
    this.companies = companies;
    this.users = users;
    this.currentUser = currentUser;
    this.passwordEncoder = passwordEncoder;
  }

  @GetMapping("/company")
  @Transactional(readOnly = true)
  public CompanyView company() {
    return CompanyView.from(requireCompany());
  }

  @PatchMapping("/company")
  @Transactional
  public CompanyView updateCompany(@Valid @RequestBody CompanyInput input) {
    requireManager();
    Company company = requireCompany();
    company.setName(input.name().trim());
    company.setLegalName(input.legalName());
    company.setGstin(input.gstin());
    company.setEmail(input.email());
    company.setPhone(input.phone());
    company.setAddressLine(input.addressLine());
    company.setCity(input.city());
    company.setState(input.state());
    company.setPincode(input.pincode());
    return CompanyView.from(company);
  }

  @GetMapping("/team")
  @Transactional(readOnly = true)
  public List<UserResponse> team() {
    return users.findByCompanyIdOrderByFirstNameAsc(currentUser.require().companyId()).stream().map(UserResponse::from).toList();
  }

  @PostMapping("/team")
  @Transactional
  public UserResponse createMember(@Valid @RequestBody MemberInput input) {
    requireManager();
    String email = input.email().trim().toLowerCase();
    if (users.existsByEmailIgnoreCase(email)) {
      throw new BadRequestException("A person with that email already exists");
    }
    UserAccount user = new UserAccount();
    user.setCompanyId(currentUser.require().companyId());
    user.setEmail(email);
    user.setPasswordHash(passwordEncoder.encode(input.password()));
    user.setFirstName(input.firstName().trim());
    user.setLastName(input.lastName().trim());
    user.setPhone(input.phone());
    user.setRole(input.role());
    user.setStatus(UserAccount.Status.ACTIVE);
    return UserResponse.from(users.save(user));
  }

  private Company requireCompany() {
    return companies.findById(currentUser.require().companyId()).orElseThrow(NotFoundException::new);
  }

  private void requireManager() {
    AuthUser actor = currentUser.require();
    if (actor.role() != UserAccount.Role.OWNER && actor.role() != UserAccount.Role.ADMIN) {
      throw new AccessDeniedException("Managers only");
    }
  }

  public record CompanyInput(
      @NotBlank @Size(max = 160) String name,
      @Size(max = 200) String legalName,
      @Size(max = 15) String gstin,
      @Size(max = 160) String email,
      @Size(max = 20) String phone,
      @Size(max = 240) String addressLine,
      @Size(max = 80) String city,
      @Size(max = 80) String state,
      @Size(max = 10) String pincode
  ) {
  }

  public record CompanyView(
      java.util.UUID id, String name, String legalName, String slug, String gstin, String email, String phone,
      String addressLine, String city, String state, String pincode
  ) {
    static CompanyView from(Company company) {
      return new CompanyView(
          company.getId(), company.getName(), company.getLegalName(), company.getSlug(), company.getGstin(),
          company.getEmail(), company.getPhone(), company.getAddressLine(), company.getCity(), company.getState(), company.getPincode()
      );
    }
  }

  public record MemberInput(
      @NotBlank @Size(max = 80) String firstName,
      @NotBlank @Size(max = 80) String lastName,
      @NotBlank @Email String email,
      @Size(max = 20) String phone,
      @NotNull UserAccount.Role role,
      @NotBlank @Size(min = 10, max = 72) String password
  ) {
  }
}

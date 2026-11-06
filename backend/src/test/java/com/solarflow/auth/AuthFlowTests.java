package com.solarflow.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solarflow.PostgresTestBase;
import com.solarflow.company.domain.Company;
import com.solarflow.company.domain.CompanyRepository;
import com.solarflow.customer.domain.Customer;
import com.solarflow.customer.domain.CustomerRepository;
import com.solarflow.user.domain.UserAccount;
import com.solarflow.user.domain.UserAccountRepository;
import jakarta.servlet.http.Cookie;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@AutoConfigureMockMvc
class AuthFlowTests extends PostgresTestBase {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper json;
  @Autowired
  private PasswordEncoder passwordEncoder;
  @Autowired
  private CompanyRepository companies;
  @Autowired
  private UserAccountRepository users;
  @Autowired
  private CustomerRepository customers;

  private UUID heliosId;

  @BeforeEach
  void user() {
    Company helios = companies.findBySlug("helios").orElseThrow();
    heliosId = helios.getId();
    if (users.findByEmailIgnoreCase("owner@helios.example").isEmpty()) {
      UserAccount owner = new UserAccount();
      owner.setCompanyId(heliosId);
      owner.setEmail("owner@helios.example");
      owner.setPasswordHash(passwordEncoder.encode("Solarflow#2026"));
      owner.setFirstName("Asha");
      owner.setLastName("Rao");
      owner.setRole(UserAccount.Role.OWNER);
      owner.setStatus(UserAccount.Status.ACTIVE);
      users.save(owner);
    }
  }

  @Test
  void rotatesRefreshTokensAndRevokesTheFamilyOnReuse() throws Exception {
    Cookie first = login();
    MvcResult refreshed = mockMvc.perform(post("/api/v1/auth/refresh").cookie(first).with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").isNotEmpty())
        .andReturn();
    Cookie second = refreshed.getResponse().getCookie(AuthService.COOKIE);
    assertThat(second).isNotNull();
    assertThat(second.getValue()).isNotEqualTo(first.getValue());

    mockMvc.perform(post("/api/v1/auth/refresh").cookie(first).with(csrf()))
        .andExpect(status().isUnauthorized());
    mockMvc.perform(post("/api/v1/auth/refresh").cookie(second).with(csrf()))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void logoutAndLogoutAllInvalidateRefreshTokens() throws Exception {
    Cookie session = login();
    String access = accessToken(session);
    mockMvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + access))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("owner@helios.example"));

    mockMvc.perform(post("/api/v1/auth/logout").cookie(session).with(csrf()))
        .andExpect(status().isOk());
    mockMvc.perform(post("/api/v1/auth/refresh").cookie(session).with(csrf()))
        .andExpect(status().isUnauthorized());

    Cookie again = login();
    String token = accessToken(again);
    mockMvc.perform(post("/api/v1/auth/logout-all").cookie(again).header("Authorization", "Bearer " + token).with(csrf()))
        .andExpect(status().isOk());
    mockMvc.perform(post("/api/v1/auth/refresh").cookie(again).with(csrf()))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void changePasswordRevokesSessions() throws Exception {
    Cookie session = login();
    String access = accessToken(session);
    mockMvc.perform(post("/api/v1/auth/change-password")
            .header("Authorization", "Bearer " + access)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"currentPassword\":\"Solarflow#2026\",\"newPassword\":\"Solarflow#2027\"}")
            .with(csrf()))
        .andExpect(status().isOk());
    mockMvc.perform(post("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"owner@helios.example\",\"password\":\"Solarflow#2026\"}")
            .with(csrf()))
        .andExpect(status().isUnauthorized());
    mockMvc.perform(post("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"owner@helios.example\",\"password\":\"Solarflow#2027\"}")
            .with(csrf()))
        .andExpect(status().isOk());
    users.findByEmailIgnoreCase("owner@helios.example").ifPresent(user -> {
      user.setPasswordHash(passwordEncoder.encode("Solarflow#2026"));
      users.save(user);
    });
  }

  @Test
  void hidesAnotherCompanysCustomer() throws Exception {
    Customer customer = new Customer();
    customer.setCompanyId(heliosId);
    customer.setDisplayName("Private household");
    customer.setStatus(Customer.Status.ACTIVE);
    customers.save(customer);

    Company other = new Company();
    other.setName("Other Solar");
    other.setSlug("other-" + UUID.randomUUID().toString().substring(0, 8));
    companies.save(other);
    UserAccount outsider = new UserAccount();
    outsider.setCompanyId(other.getId());
    outsider.setEmail("outsider-" + UUID.randomUUID() + "@example.com");
    outsider.setPasswordHash(passwordEncoder.encode("Solarflow#2026"));
    outsider.setFirstName("Out");
    outsider.setLastName("Sider");
    outsider.setRole(UserAccount.Role.OWNER);
    outsider.setStatus(UserAccount.Status.ACTIVE);
    users.save(outsider);

    MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"" + outsider.getEmail() + "\",\"password\":\"Solarflow#2026\"}")
            .with(csrf()))
        .andExpect(status().isOk())
        .andReturn();
    String access = json.readTree(login.getResponse().getContentAsString()).get("accessToken").asText();
    mockMvc.perform(get("/api/v1/customers/" + customer.getId()).header("Authorization", "Bearer " + access))
        .andExpect(status().isNotFound());
  }

  @Test
  void storesAProjectPhotoInsideTheCompany() throws Exception {
    String access = accessToken(null);
    MvcResult customer = mockMvc.perform(post("/api/v1/customers")
            .header("Authorization", "Bearer " + access)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"displayName\":\"Photo household\",\"status\":\"ACTIVE\"}")
            .with(csrf()))
        .andExpect(status().isOk())
        .andReturn();
    String customerId = json.readTree(customer.getResponse().getContentAsString()).get("id").asText();
    MvcResult project = mockMvc.perform(post("/api/v1/projects")
            .header("Authorization", "Bearer " + access)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"customerId\":\"" + customerId + "\",\"name\":\"Roof photos\",\"status\":\"PLANNED\",\"progressPercent\":0}")
            .with(csrf()))
        .andExpect(status().isOk())
        .andReturn();
    String projectId = json.readTree(project.getResponse().getContentAsString()).get("id").asText();
    byte[] png = java.util.Base64.getDecoder().decode(
        "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg=="
    );
    MvcResult uploaded = mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/projects/" + projectId + "/photos")
            .file(new MockMultipartFile("file", "roof.png", "image/png", png))
            .header("Authorization", "Bearer " + access)
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.photos[0].fileName").value("roof.png"))
        .andReturn();
    String photoId = json.readTree(uploaded.getResponse().getContentAsString()).get("photos").get(0).get("id").asText();
    mockMvc.perform(get("/api/v1/projects/" + projectId + "/photos/" + photoId).header("Authorization", "Bearer " + access))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isNotEmpty());
  }

  private Cookie login() throws Exception {
    MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"owner@helios.example\",\"password\":\"Solarflow#2026\"}")
            .with(csrf()))
        .andExpect(status().isOk())
        .andReturn();
    Cookie cookie = result.getResponse().getCookie(AuthService.COOKIE);
    assertThat(cookie).isNotNull();
    assertThat(cookie.isHttpOnly()).isTrue();
    return cookie;
  }

  private String accessToken(Cookie ignored) throws Exception {
    MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"owner@helios.example\",\"password\":\"Solarflow#2026\"}")
            .with(csrf()))
        .andExpect(status().isOk())
        .andReturn();
    return json.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
  }

  private String accessTokenFrom(MvcResult result) throws Exception {
    JsonNode node = json.readTree(result.getResponse().getContentAsString());
    return node.get("accessToken").asText();
  }
}

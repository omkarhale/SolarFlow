package com.solarflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.solarflow.company.domain.CompanyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SolarFlowApplicationTests extends PostgresTestBase {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private CompanyRepository companies;

  @Test
  void healthReportsDatabaseUp() throws Exception {
    mockMvc.perform(get("/api/v1/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("ok"))
        .andExpect(jsonPath("$.service").value("solarflow"))
        .andExpect(jsonPath("$.database").value("up"));
  }

  @Test
  void protectedRoutesRequireAuthentication() throws Exception {
    mockMvc.perform(get("/api/v1/auth/me")).andExpect(status().isUnauthorized());
  }

  @Test
  void flywaySeedsDemoCompany() {
    assertThat(companies.findBySlug("helios"))
        .get()
        .extracting("name", "city")
        .containsExactly("Helios Rooftop Solar", "Pune");
  }
}

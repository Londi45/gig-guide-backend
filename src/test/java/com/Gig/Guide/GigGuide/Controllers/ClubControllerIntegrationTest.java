package com.Gig.Guide.GigGuide.Controllers;

import com.Gig.Guide.GigGuide.DTO.Club.AddressDTO;
import com.Gig.Guide.GigGuide.DTO.Club.ClubDTO;
import com.Gig.Guide.GigGuide.DTO.Club.SocialsDTO;
import com.Gig.Guide.GigGuide.Repositories.ClubRepository;
import com.Gig.Guide.GigGuide.Repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ClubController.
 *
 * Loads the full Spring context against a real database (test profile).
 * Each test runs in a transaction that is rolled back after completion
 * so tests are isolated and leave no data behind.
 *
 * Requires:
 *   - A running PostgreSQL instance (configured in application-test.properties)
 *   - Kafka is NOT required — the test profile disables Kafka auto-start
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("ClubController — Integration Tests")
class ClubControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClubRepository clubRepository;

    // ── Helpers ───────────────────────────────────────────────────────────────

    private ClubDTO buildRequest() {
        return ClubDTO.builder()
                .name("Integration Test Club")
                .description("Created during integration test")
                .email("integration@test.co.za")
                .phone("+27111112222")
                .website("https://integration.test")
                .openingHours("20:00")
                .closingHours("04:00")
                .dressCode("Casual")
                .hasParking(false)
                .hasVIPArea(true)
                .capacity(200)
                .active(true)
                .address(AddressDTO.builder()
                        .location("1 Test Street")
                        .city("Cape Town")
                        .province("Western Cape")
                        .country("South Africa")
                        .postalCode("8001")
                        .build())
                .socials(SocialsDTO.builder()
                        .instagramLink("https://instagram.com/testclub")
                        .facebookLink("https://facebook.com/testclub")
                        .build())
                .build();
    }

    // ── Create club ───────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/clubs — persists club and returns UUID id")
    void createClub_persistsToDb_returnsUuid() throws Exception {
        String json = objectMapper.writeValueAsString(buildRequest());

        MvcResult result = mockMvc.perform(post("/api/clubs")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.id", matchesPattern(
                        "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")))
                .andExpect(jsonPath("$.name").value("Integration Test Club"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.address.city").value("Cape Town"))
                .andReturn();

        // Confirm it actually hit the DB
        String responseJson = result.getResponse().getContentAsString();
        ClubDTO created = objectMapper.readValue(responseJson, ClubDTO.class);
        Assertions.assertTrue(clubRepository.findById(created.getId()).isPresent(),
                "Club should be persisted in the database");
    }

    // ── Get all clubs ─────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/clubs — returns paginated response")
    void getAllClubs_returnsPage() throws Exception {
        // Create a club first
        mockMvc.perform(post("/api/clubs")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/clubs")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.content[0].name").isString());
    }

    // ── Get club by id ────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/clubs/{id} — returns correct club")
    void getClubById_returnsClub() throws Exception {
        // Create
        MvcResult created = mockMvc.perform(post("/api/clubs")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isCreated())
                .andReturn();

        ClubDTO createdClub = objectMapper.readValue(
                created.getResponse().getContentAsString(), ClubDTO.class);

        // Fetch by id
        mockMvc.perform(get("/api/clubs/{id}", createdClub.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdClub.getId()))
                .andExpect(jsonPath("$.name").value("Integration Test Club"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/clubs/{id} — 404 for unknown id")
    void getClubById_unknownId_returns404() throws Exception {
        mockMvc.perform(get("/api/clubs/{id}", "00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound());
    }

    // ── Update club ───────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/clubs/{id} — updates and returns updated DTO")
    void updateClub_updatesFields() throws Exception {
        // Create
        MvcResult created = mockMvc.perform(post("/api/clubs")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isCreated())
                .andReturn();

        ClubDTO createdClub = objectMapper.readValue(
                created.getResponse().getContentAsString(), ClubDTO.class);

        // Modify
        createdClub.setName("Updated Integration Club");
        createdClub.setCapacity(999);

        mockMvc.perform(put("/api/clubs/{id}", createdClub.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createdClub)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Integration Club"))
                .andExpect(jsonPath("$.capacity").value(999));
    }

    // ── Delete club ───────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/clubs/{id} — removes from DB")
    void deleteClub_removesFromDb() throws Exception {
        // Create
        MvcResult created = mockMvc.perform(post("/api/clubs")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isCreated())
                .andReturn();

        ClubDTO createdClub = objectMapper.readValue(
                created.getResponse().getContentAsString(), ClubDTO.class);
        String id = createdClub.getId();

        // Delete
        mockMvc.perform(delete("/api/clubs/{id}", id)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        // Confirm gone
        mockMvc.perform(get("/api/clubs/{id}", id))
                .andExpect(status().isNotFound());

        Assertions.assertFalse(clubRepository.findById(id).isPresent(),
                "Club should be deleted from the database");
    }

    // ── Deactivate club ───────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PATCH /api/clubs/{id}/deactivate — marks club inactive")
    void deactivateClub_setsActiveFalse() throws Exception {
        // Create
        MvcResult created = mockMvc.perform(post("/api/clubs")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isCreated())
                .andReturn();

        ClubDTO createdClub = objectMapper.readValue(
                created.getResponse().getContentAsString(), ClubDTO.class);
        String id = createdClub.getId();

        // Deactivate
        mockMvc.perform(patch("/api/clubs/{id}/deactivate", id)
                        .with(csrf()))
                .andExpect(status().isOk());

        // Confirm no longer returned by active-only GET /api/clubs
        mockMvc.perform(get("/api/clubs/{id}", id))
                .andExpect(status().isNotFound());

        // DB-level confirmation
        Assertions.assertTrue(
                clubRepository.findById(id).map(c -> !c.isActive()).orElse(false),
                "Club should be marked inactive in the database");
    }
}

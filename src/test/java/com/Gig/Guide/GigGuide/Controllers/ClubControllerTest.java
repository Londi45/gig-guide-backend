package com.Gig.Guide.GigGuide.Controllers;

import com.Gig.Guide.GigGuide.Config.TestSecurityConfig;
import com.Gig.Guide.GigGuide.Controllers.ClubsControllers.ClubController;
import com.Gig.Guide.GigGuide.DTO.Club.AddressDTO;
import com.Gig.Guide.GigGuide.DTO.Club.ClubDTO;
import com.Gig.Guide.GigGuide.DTO.Club.SocialsDTO;
import com.Gig.Guide.GigGuide.Exceptions.ResourceNotFoundException;
import com.Gig.Guide.GigGuide.Service.ClubService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for ClubController.
 *
 * Uses @WebMvcTest to load only the web layer (no DB, no Kafka).
 * ClubService is mocked with Mockito — we test controller logic only:
 *   - routing, status codes, request/response mapping, security rules.
 *
 * TestSecurityConfig replaces the production SecurityConfig so that
 * @WithMockUser drives auth in tests without needing JWT infrastructure.
 */
@WebMvcTest(ClubController.class)
@Import(TestSecurityConfig.class)
@DisplayName("ClubController")
class ClubControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClubService clubService;

    // JwtAuthFilter is a @Component and gets picked up by the web slice scan.
    // It won't run (TestSecurityConfig doesn't register it), but Spring still
    // needs its dependencies satisfied to create the bean.
    @MockBean
    private com.Gig.Guide.GigGuide.utils.JwtTokenUtil jwtTokenUtil;

    @MockBean
    private com.Gig.Guide.GigGuide.Service.MyAppUserService myAppUserService;

    // ── Helpers ──────────────────────────────────────────────────────────────

    private static final String CLUB_ID = UUID.randomUUID().toString();

    private ClubDTO buildClubDTO() {
        return ClubDTO.builder()
                .id(CLUB_ID)
                .name("Taboo Nightclub")
                .description("JHB premier nightclub")
                .email("info@taboo.co.za")
                .phone("+27112345678")
                .website("https://taboo.co.za")
                .openingHours("22:00")
                .closingHours("04:00")
                .dressCode("Smart Casual")
                .hasParking(true)
                .hasVIPArea(true)
                .capacity(500)
                .active(true)
                .address(AddressDTO.builder()
                        .location("12 Fox Street")
                        .city("Johannesburg")
                        .province("Gauteng")
                        .country("South Africa")
                        .postalCode("2001")
                        .build())
                .socials(SocialsDTO.builder()
                        .instagramLink("https://instagram.com/taboo")
                        .facebookLink("https://facebook.com/taboo")
                        .build())
                .build();
    }

    // ── POST /api/clubs ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/clubs")
    class CreateClub {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("201 - creates club and returns DTO")
        void createClub_adminRole_returns201() throws Exception {
            ClubDTO request = buildClubDTO();
            request.setId(null); // client doesn't send id
            ClubDTO response = buildClubDTO();

            when(clubService.createClub(any(ClubDTO.class))).thenReturn(response);

            mockMvc.perform(post("/api/clubs")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(CLUB_ID))
                    .andExpect(jsonPath("$.name").value("Taboo Nightclub"))
                    .andExpect(jsonPath("$.email").value("info@taboo.co.za"))
                    .andExpect(jsonPath("$.address.city").value("Johannesburg"))
                    .andExpect(jsonPath("$.socials.instagramLink").value("https://instagram.com/taboo"))
                    .andExpect(jsonPath("$.active").value(true))
                    .andExpect(jsonPath("$.capacity").value(500));

            verify(clubService).createClub(any(ClubDTO.class));
        }

        @Test
        @WithMockUser(roles = "CLUB_OWNER")
        @DisplayName("403 - non-admin role is rejected")
        void createClub_nonAdminRole_returns403() throws Exception {
            mockMvc.perform(post("/api/clubs")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildClubDTO())))
                    .andExpect(status().isForbidden());

            verifyNoInteractions(clubService);
        }

        @Test
        @DisplayName("401 - unauthenticated request is rejected")
        void createClub_unauthenticated_returns401() throws Exception {
            mockMvc.perform(post("/api/clubs")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildClubDTO())))
                    .andExpect(status().isUnauthorized());

            verifyNoInteractions(clubService);
        }
    }

    // ── GET /api/clubs ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/clubs")
    class GetAllClubs {

        @Test
        @WithMockUser
        @DisplayName("200 - returns paginated clubs")
        void getAllClubs_returns200WithPage() throws Exception {
            ClubDTO dto = buildClubDTO();
            Page<ClubDTO> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);

            when(clubService.getAllClubs(any())).thenReturn(page);

            mockMvc.perform(get("/api/clubs")
                            .param("page", "0")
                            .param("size", "10")
                            .param("sort", "name"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].name").value("Taboo Nightclub"))
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.totalPages").value(1));

            verify(clubService).getAllClubs(any());
        }

        @Test
        @WithMockUser
        @DisplayName("200 - returns empty page when no clubs")
        void getAllClubs_noClubs_returnsEmptyPage() throws Exception {
            Page<ClubDTO> empty = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
            when(clubService.getAllClubs(any())).thenReturn(empty);

            mockMvc.perform(get("/api/clubs"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)))
                    .andExpect(jsonPath("$.totalElements").value(0));
        }

        @Test
        @DisplayName("200 - public access allowed without authentication")
        void getAllClubs_noAuth_stillReturns200() throws Exception {
            Page<ClubDTO> empty = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
            when(clubService.getAllClubs(any())).thenReturn(empty);

            mockMvc.perform(get("/api/clubs"))
                    .andExpect(status().isOk());
        }
    }

    // ── GET /api/clubs/{id} ───────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/clubs/{id}")
    class GetClubById {

        @Test
        @WithMockUser
        @DisplayName("200 - returns club for valid id")
        void getClubById_validId_returns200() throws Exception {
            when(clubService.getClubById(CLUB_ID)).thenReturn(buildClubDTO());

            mockMvc.perform(get("/api/clubs/{id}", CLUB_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(CLUB_ID))
                    .andExpect(jsonPath("$.name").value("Taboo Nightclub"));

            verify(clubService).getClubById(CLUB_ID);
        }

        @Test
        @WithMockUser
        @DisplayName("404 - throws ResourceNotFoundException for unknown id")
        void getClubById_unknownId_returns404() throws Exception {
            String unknownId = UUID.randomUUID().toString();
            when(clubService.getClubById(unknownId))
                    .thenThrow(new ResourceNotFoundException("Club not found"));

            mockMvc.perform(get("/api/clubs/{id}", unknownId))
                    .andExpect(status().isNotFound());
        }
    }

    // ── PUT /api/clubs/{id} ───────────────────────────────────────────────────

    @Nested
    @DisplayName("PUT /api/clubs/{id}")
    class UpdateClub {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("200 - admin can update a club")
        void updateClub_adminRole_returns200() throws Exception {
            ClubDTO updated = buildClubDTO();
            updated.setName("Updated Club");

            when(clubService.updateClub(eq(CLUB_ID), any(ClubDTO.class))).thenReturn(updated);

            mockMvc.perform(put("/api/clubs/{id}", CLUB_ID)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updated)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Updated Club"));

            verify(clubService).updateClub(eq(CLUB_ID), any(ClubDTO.class));
        }

        @Test
        @WithMockUser(roles = "CLUB_OWNER")
        @DisplayName("200 - club owner can update their club")
        void updateClub_clubOwnerRole_returns200() throws Exception {
            ClubDTO updated = buildClubDTO();
            when(clubService.updateClub(eq(CLUB_ID), any(ClubDTO.class))).thenReturn(updated);

            mockMvc.perform(put("/api/clubs/{id}", CLUB_ID)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updated)))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "STAFF")
        @DisplayName("403 - staff role is rejected")
        void updateClub_staffRole_returns403() throws Exception {
            mockMvc.perform(put("/api/clubs/{id}", CLUB_ID)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildClubDTO())))
                    .andExpect(status().isForbidden());

            verifyNoInteractions(clubService);
        }
    }

    // ── DELETE /api/clubs/{id} ────────────────────────────────────────────────

    @Nested
    @DisplayName("DELETE /api/clubs/{id}")
    class DeleteClub {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("204 - admin can delete a club")
        void deleteClub_adminRole_returns204() throws Exception {
            doNothing().when(clubService).deleteClub(CLUB_ID);

            mockMvc.perform(delete("/api/clubs/{id}", CLUB_ID)
                            .with(csrf()))
                    .andExpect(status().isNoContent());

            verify(clubService).deleteClub(CLUB_ID);
        }

        @Test
        @WithMockUser(roles = "CLUB_OWNER")
        @DisplayName("403 - club owner cannot delete a club")
        void deleteClub_clubOwnerRole_returns403() throws Exception {
            mockMvc.perform(delete("/api/clubs/{id}", CLUB_ID)
                            .with(csrf()))
                    .andExpect(status().isForbidden());

            verifyNoInteractions(clubService);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("404 - delete non-existent club returns 404")
        void deleteClub_notFound_returns404() throws Exception {
            String unknownId = UUID.randomUUID().toString();
            doThrow(new ResourceNotFoundException("Club not found"))
                    .when(clubService).deleteClub(unknownId);

            mockMvc.perform(delete("/api/clubs/{id}", unknownId)
                            .with(csrf()))
                    .andExpect(status().isNotFound());
        }
    }

    // ── PATCH /api/clubs/{id}/deactivate ─────────────────────────────────────

    @Nested
    @DisplayName("PATCH /api/clubs/{id}/deactivate")
    class DeactivateClub {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("200 - admin can deactivate a club")
        void deactivateClub_adminRole_returns200() throws Exception {
            doNothing().when(clubService).deactivateClub(CLUB_ID);

            mockMvc.perform(patch("/api/clubs/{id}/deactivate", CLUB_ID)
                            .with(csrf()))
                    .andExpect(status().isOk());

            verify(clubService).deactivateClub(CLUB_ID);
        }

        @Test
        @WithMockUser(roles = "CLUB_OWNER")
        @DisplayName("403 - club owner cannot deactivate a club")
        void deactivateClub_clubOwnerRole_returns403() throws Exception {
            mockMvc.perform(patch("/api/clubs/{id}/deactivate", CLUB_ID)
                            .with(csrf()))
                    .andExpect(status().isForbidden());

            verifyNoInteractions(clubService);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("404 - deactivate non-existent club returns 404")
        void deactivateClub_notFound_returns404() throws Exception {
            doThrow(new ResourceNotFoundException("Club not found"))
                    .when(clubService).deactivateClub(CLUB_ID);

            mockMvc.perform(patch("/api/clubs/{id}/deactivate", CLUB_ID)
                            .with(csrf()))
                    .andExpect(status().isNotFound());
        }
    }
}

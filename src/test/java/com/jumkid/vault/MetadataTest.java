package com.jumkid.vault;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jumkid.vault.enums.MediaFileField;
import com.jumkid.vault.model.MediaFileMetadata;
import com.jumkid.vault.repository.LocalFileStorage;
import com.jumkid.vault.repository.MetadataStorage;
import com.jumkid.vault.service.MediaFileSecurityService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(properties = { "jwt.token.enable = false" })
@AutoConfigureMockMvc
public class MetadataTest extends TestsSetup {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MetadataStorage metadataStorage;

    @MockBean
    private LocalFileStorage localFileStorage;

    @Autowired
    private MediaFileSecurityService securityService;

    private MediaFileMetadata mediaFileMetadata;

    @Before
    public void setup() {
        try {
            mediaFileMetadata = buildMetadata(null);

            when(metadataStorage.getMetadata(DUMMY_ID)).thenReturn(Optional.of(mediaFileMetadata));
            when(localFileStorage.getFileBinary(mediaFileMetadata))
                    .thenReturn(Optional.of(mediaFileMetadata.getContent().getBytes()));
        } catch (Exception e) {
            Assert.fail();
        }

    }

    @Test
    @WithMockUser(username="demo1",
            password="demo",
            authorities="user")
    public void shouldGetMetadata_whenGivenId() throws Exception {
        mockMvc.perform(get("/metadata/"+DUMMY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(DUMMY_ID))
                .andExpect(jsonPath("$.title").value("test.title"));
    }

    @Test
    @WithMockUser(username="demo1",
            password="demo",
            authorities="user")
    public void shouldGetListOfMetadata_whenSearch() throws Exception {
        when(metadataStorage.searchMetadata(anyString(), anyInt(), anyList(), eq("demo1"))).thenReturn(buildListOfMetadata());

        mockMvc.perform(get("/metadata?q=test&size=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").exists())
                .andExpect(jsonPath("$[0].uuid").value(DUMMY_ID))
                .andExpect(jsonPath("$[1]").exists());
    }

    @Test
    @WithMockUser(username="admin",
            password="admin",
            authorities="admin")
    public void shouldSaveContentWithPros_whenGivenMetadata() throws Exception {
        when(metadataStorage.saveMetadata(any(MediaFileMetadata.class))).thenReturn(mediaFileMetadata);

        mockMvc.perform(post("/metadata")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsBytes(mediaFileMetadata)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(mediaFileMetadata.getTitle()));
    }

    @Test
    @WithMockUser(username="demo1",
            password="demo",
            authorities="user")
    public void shouldUpdateMetadata_whenGivenMetadata() throws Exception {
        when(metadataStorage.updateMetadata(DUMMY_ID, mediaFileMetadata)).thenReturn(mediaFileMetadata);

        mockMvc.perform(put("/metadata/"+DUMMY_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsBytes(mediaFileMetadata)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(DUMMY_ID))
                .andExpect(jsonPath("$.title").value("test.title"));
    }

    @Test
    @WithMockUser(username="demo1",
            password="demo",
            authorities="user")
    public void shouldDeleteMetadata_whenGivenId() throws Exception {
        mockMvc.perform(delete("/metadata/"+DUMMY_ID))
                .andExpect(status().isNoContent());
    }



}

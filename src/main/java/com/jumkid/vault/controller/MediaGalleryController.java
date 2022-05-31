package com.jumkid.vault.controller;

import com.jumkid.vault.controller.dto.MediaFile;
import com.jumkid.vault.controller.dto.MediaFileProp;
import com.jumkid.vault.exception.FileStoreServiceException;
import com.jumkid.vault.service.MediaFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.jumkid.vault.util.Constants.PROP_FEATURED_ID;

@Slf4j
@RestController
@RequestMapping("/gallery")
public class MediaGalleryController {

    private final MediaFileService fileService;

    @Autowired
    public MediaGalleryController(MediaFileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('user', 'admin')")
    public MediaFile add(@NotNull @RequestParam("title") String title,
                         @RequestParam(value = "content", required = false) String content,
                         @RequestParam(value = "tags", required = false) List<String> tags,
                         @RequestParam(value = "files", required = false) MultipartFile[] files) {
        MediaFile gallery = MediaFile.builder()
                                    .title(title)
                                    .content(content)
                                    .tags(tags)
                                    .build();

        List<MediaFile> mediaFileList = new ArrayList<>();
        MediaFile mediaFile = null;
        try {
            if (files != null) {
                for (MultipartFile file : files) {
                    mediaFile = MediaFile.builder()
                            .filename(file.getOriginalFilename())
                            .size((int)file.getSize())
                            .mimeType(file.getContentType())
                            .build();

                    mediaFile.setFile(file.getBytes());

                    mediaFileList.add(mediaFile);
                }

                gallery.setChildren(mediaFileList);
                log.debug("media gallery {} created with {} children", gallery.getTitle(), gallery.getChildren().size());
            }
        } catch (IOException ioe) {
            throw new FileStoreServiceException("Failed to update file ", mediaFile);
        }

        gallery = fileService.addMediaGallery(gallery);

        return gallery;
    }

    @PostMapping(value = "{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public MediaFile update(@NotNull @PathVariable("id") String galleryId,
                            @RequestParam(value = "featuredId", required = false) String featuredId,
                            @RequestParam(value = "mediaFileIds", required = false) List<String> childIds,
                            @RequestParam(value = "files", required = false) MultipartFile[] files) {
        MediaFile partialMediaFile = MediaFile.builder().uuid(galleryId).build();

        boolean hasUpdate = false;
        if (childIds != null && !childIds.isEmpty()) {
            List<MediaFile> mediaFileList = childIds.stream()
                    .map(childId -> MediaFile.builder().uuid(childId).build())
                    .collect(Collectors.toList());

            partialMediaFile.setChildren(mediaFileList);

            hasUpdate = true;
        }

        if (files != null) {
            List<MediaFile> childList = processNewChildren(files);
            if (partialMediaFile.getChildren() != null) {
                partialMediaFile.getChildren().addAll(childList);
            } else {
                partialMediaFile.setChildren(childList);
            }
            hasUpdate = true;
        }

        if (featuredId != null) {
            partialMediaFile.setProps(List.of(MediaFileProp.builder()
                    .name(PROP_FEATURED_ID).textValue(featuredId)
                    .build()));
            hasUpdate = true;
        }

        if (hasUpdate) return fileService.updateMediaGallery(galleryId, partialMediaFile);
        else return null;
    }

    @PutMapping("{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasAuthority('admin')" +
            " || (hasAnyAuthority('user') && @securityService.isOwner(authentication, #galleryId))")
    public MediaFile update(@PathVariable(value = "id") String galleryId,
                            @NotNull @RequestBody MediaFile partialMediaFile) {
        return fileService.updateMediaGallery(galleryId, partialMediaFile);
    }

    private List<MediaFile> processNewChildren(MultipartFile[] files){
        List<MediaFile> newChildList = new ArrayList<>();
        try {
            if (files != null) {
                for (MultipartFile file : files) {
                    MediaFile newChild = MediaFile.builder()
                            .filename(file.getOriginalFilename())
                            .size((int)file.getSize())
                            .mimeType(file.getContentType())
                            .build();
                    newChild.setFile(file.getBytes());
                    newChildList.add(newChild);
                }
            }
        } catch (IOException ioe) {
            log.error("Failed to update file");
        }
        return newChildList;
    }

}

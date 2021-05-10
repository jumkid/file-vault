package com.jumkid.vault.controller;

import com.jumkid.vault.controller.dto.MediaFile;
import com.jumkid.vault.enums.MediaFileField;
import com.jumkid.vault.enums.MediaFileModule;
import com.jumkid.vault.exception.FileNotFoundException;
import com.jumkid.vault.exception.FileStoreServiceException;
import com.jumkid.vault.service.MediaFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/metadata")
public class MediaMetadataController {

    private final MediaFileService fileService;

    @Autowired
    public MediaMetadataController(MediaFileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('user', 'admin')")
    public List<MediaFile> searchMetadata(@RequestParam(required = false) String q,
                                          @RequestParam(required = false) Integer size){
        if (q == null || q.isBlank()) q = "*";
        return fileService.searchMediaFile(q, size);
    }

    @GetMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    public MediaFile getMetadata(@PathVariable("id") String mediaFileId){
        MediaFile mediaFile = fileService.getMediaFile(mediaFileId);
        if (mediaFile != null) {
            return mediaFile;
        } else {
            throw new FileNotFoundException(mediaFileId);
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public MediaFile addMetadata(@NotNull @Valid @RequestBody MediaFile mediaFile,
                                 @NotNull MediaFileModule mediaFileModule){
        return fileService.addMediaFile(mediaFile, null, mediaFileModule);
    }

    @PutMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('admin')" +
            " || (hasAnyAuthority('user') && @securityService.isOwner(authentication, #mediaFileId))")
    public MediaFile updateMetadata(@PathVariable("id") String mediaFileId,
                                    @RequestParam(required = false) MediaFileField fieldName,
                                    @RequestParam(required = false) String fieldValue,
                                    @RequestBody(required = false) MediaFile mediaFile){
        if (fieldName != null && fieldValue != null) {
            if (!fileService.updateMediaFileFieldValue(mediaFileId, fieldName, fieldValue)) {
                throw new FileStoreServiceException(String.join("Failed to update media file field %s value", fieldName.value()));
            }
        } else if (mediaFile != null) {
            return fileService.updateMediaFile(mediaFileId, mediaFile, null);
        }
        return null;
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('admin')" +
            " || (hasAnyAuthority('user') && @securityService.isOwner(authentication, #mediaFileId))")
    public void deleteMetadata(@PathVariable("id") String mediaFileId) {
        fileService.trashMediaFile(mediaFileId);
    }

}

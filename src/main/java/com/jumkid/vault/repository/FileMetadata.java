package com.jumkid.vault.repository;
/*
 * This software is written by Jumkid and subject
 * to a contract between Jumkid and its customer.
 *
 * This software stays property of Jumkid unless differing
 * arrangements between Jumkid and its customer apply.
 *
 *
 * (c)2019 Jumkid Innovation All rights reserved.
 */

import com.jumkid.vault.enums.MediaFileField;
import com.jumkid.vault.model.MediaFileMetadata;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created at Sep2018$
 *
 * @author chooliyip
 **/
public interface FileMetadata<T> {

    /**
     * Persist file metadata in search repository
     *
     * @param t generic type of file
     */
    T saveMetadata(T t);

    /**
     * Get type from repository by given identifier
     *
     * @param mediaFileId identity of media
     */
    Optional<T> getMetadata(String mediaFileId);

    /**
     * Get file from repository
     *
     * @param mediaFileId identity of media
     */
    Optional<byte[]> getBinary(String mediaFileId);

    /**
     * Remove file from search
     *
     * @param mediaFileId identity of media file
     */
    boolean deleteMetadata(String mediaFileId);

    /**
     * update the metadata of give type
     *
     * @param t type
     * @return type
     */
    public T updateMetadata(String id, T t) throws IOException;

    /**
     * update metadata active status
     *
     * @param mediaFileId identity of media file
     */
    void updateMetadataStatus(String mediaFileId, boolean active);

    /**
     * Update metadata file storage path
     *
     * @param mediaFileId identity of media file
     * @param logicalPath file path on the storage
     */
    void updateLogicalPath(String mediaFileId, String logicalPath);

    /**
     * Search media files with given query string
     *
     * @param query query keyword
     * @param size size of result set
     * @param currentUserRole current user roles
     * @param currentUsername current username
     * @return List of type
     */
    List<T> searchMetadata(String query, Integer size, List<String> currentUserRole, String currentUsername);

    /**
     * Get all trashed metadata from repository
     *
     * @return List of type
     */
    List<T> getInactiveMetadata();

    /**
     * Remove all inactive metadata
     */
    Long deleteInactiveMetadata();

}

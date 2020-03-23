package com.jumkid.vault.model;

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

import com.jumkid.vault.util.Constants;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MediaFileMetadata {

	private String id;

	private String filename;

	private String mimeType;
	
	private Integer size;

	private String module = Constants.MODULE_MFILE;

	private String title;

	private String content;

	private Boolean activated;

	private String logicalPath;

	private LocalDateTime creationDate;

	private String createdBy;

	private LocalDateTime modificationDate;

	private String modifiedBy;

}

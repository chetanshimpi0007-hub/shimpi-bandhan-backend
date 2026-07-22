package com.shimpimilan.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PublicProfileDTO {
    private Long id;
    private String displayName;
    private Integer age;
    private String profilePhoto;
    private String profilePhotoUrl;
}

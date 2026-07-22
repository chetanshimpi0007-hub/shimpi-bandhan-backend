package com.shimpimilan.model.profile;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lifestyle {
    private String hobbies;
    private String interests;
    private String skills;
    private String languagesKnown;
}

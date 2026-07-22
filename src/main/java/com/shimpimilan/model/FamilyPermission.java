package com.shimpimilan.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "family_permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FamilyPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "family_member_id", nullable = false)
    private FamilyMember familyMember;

    @Builder.Default
    private Boolean canEditProfile = true;

    @Builder.Default
    private Boolean canUploadPhotos = true;

    @Builder.Default
    private Boolean canUploadVideo = true;

    @Builder.Default
    private Boolean canSendInterests = true;

    @Builder.Default
    private Boolean canAcceptInterests = true;

    @Builder.Default
    private Boolean canChat = true;

    @Builder.Default
    private Boolean canVoiceCall = true;

    @Builder.Default
    private Boolean canVideoCall = true;

    @Builder.Default
    private Boolean canPurchasePremium = true;

    @Builder.Default
    private Boolean canManageGallery = true;
}

package com.chris.printmatcher.fingerprintmatcher.Model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Users")
@ApiModel
public class AppUsers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @ApiModelProperty(notes = "AUTO GENERATED USER ID")
    private Long id;

    @Column(name = "userId", unique = true, nullable = false)
    @ApiModelProperty(notes = "AppUser Unique Identity", dataType = "String", required = true, name = "userId")
    private String userId;

    @Lob
    @Column(name = "rightThumb", nullable = false, unique = true)
    @ApiModelProperty(notes = "AppUser Unique Right Thumb Print", dataType = "Byte []", name = "rightThumb")
    private byte[] rightThumb;

    @Lob
    @Column(name = "leftThumb", nullable = false, unique = true)
    @ApiModelProperty(notes = "AppUser Unique Left Thumb Print", dataType = "Byte []", name = "leftThumb")
    private byte[] leftThumb;

    @Transient
    @ApiModelProperty(notes = "AppUser Unique Right Thumb base64 String", dataType = "String", required = true, name = "rightThumbString")
    private String rightThumbString;

    @Transient
    @ApiModelProperty(notes = "AppUser Unique Left Thumb base64 String", dataType = "String", required = true, name = "leftThumbString")
    private String leftThumbString;

    @Transient
    @ApiModelProperty(notes = "AppUser Incoming Finger Print To Scan in base64 String", dataType = "String", required = true, name = "fingerPrintToVerify")
    private String fingerPrintToVerify;
}

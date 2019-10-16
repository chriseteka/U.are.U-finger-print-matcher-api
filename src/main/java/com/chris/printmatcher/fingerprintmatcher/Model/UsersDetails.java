package com.chris.printmatcher.fingerprintmatcher.Model;

import com.machinezoo.sourceafis.FingerprintTemplate;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Data
@NoArgsConstructor
public class UsersDetails implements Serializable {

    private static final long serialVersionUID = -8740036305336606869L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "userName", nullable = false)
    private String name;

    @Lob
    @Column(name = "fingerTemplate", nullable = false)
    private String fingerPrintString;

    @Transient
    public FingerprintTemplate template;

    public UsersDetails(String name, String fingerPrintString) {
        this.name = name;
        this.fingerPrintString = fingerPrintString;
    }

    public UsersDetails(String name, FingerprintTemplate template) {
        this.name = name;
        this.template = template;
    }
}

package com.chris.printmatcher.fingerprintmatcher.Model;

import com.machinezoo.sourceafis.FingerprintTemplate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsersDetails {

    private Long id;

    private String name;

    public FingerprintTemplate template;
}

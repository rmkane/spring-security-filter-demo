package org.acme.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AppInfo {
    private String name;
    private String version;
    private String description;
    private String buildTime;
    private String buildNumber;
    private String buildVersion;
    private String buildDate;
    
}

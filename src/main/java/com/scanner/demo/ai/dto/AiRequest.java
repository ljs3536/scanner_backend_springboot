package com.scanner.demo.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiRequest {
    @JsonProperty("issue_seq")
    private Integer issueSeq;
    @JsonProperty("vulnerability_type")
    private String vulnerabilityType;
    @JsonProperty("cwe_id")
    private String cweId;
    private String severity;
    @JsonProperty("file_path")
    private String filePath;
    @JsonProperty("line_number")
    private Integer lineNumber;
    @JsonProperty("code_snippet")
    private String codeSnippet;
    private String framework;
    private String language;
}
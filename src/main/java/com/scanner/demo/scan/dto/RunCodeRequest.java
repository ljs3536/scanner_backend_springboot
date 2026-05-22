package com.scanner.demo.scan.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RunCodeRequest {
    private String code;
    private String filename;
    private String profile;
}
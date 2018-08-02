package com.mavensurefire.report.model;

import java.util.List;

import lombok.Data;

@Data
public class SurefireConfiguration {

    List<String> inclusions;
    
    List<String> exclusions;
}

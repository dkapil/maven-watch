package com.mavensurefire.report.model;

import lombok.Data;

@Data
public class Project {

	String id;

	String path;

	String parentId;

	SurefireConfiguration surefireConfiguration;
}

package com.mavenwatch.core;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class AppTest {

	@Test
	public void app() 
	{
		App app=new App();
		assertNotNull(app);
	}
}

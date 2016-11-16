package com.springboottest;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableAutoConfiguration
public class TestRootControllerOne {
	
	@RequestMapping("/")
	public String getData(){
		return "Hello";
	}
	
	@RequestMapping("/test")
	public String getTest(){
		return "check";
	}
}

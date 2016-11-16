package com.springboottest;

import java.util.ArrayList;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.testcontroller.TestEntityOne;

@RestController
@EnableAutoConfiguration
@ComponentScan({ "com.testcontroller"})
public class TestRootControllerOne {
	
	private ArrayList<TestEntityOne> teoList;
	
	@RequestMapping("/")
	public String getData(){
		return "Hello";
	}
	
	public ArrayList<TestEntityOne> getTeoList() {
		return teoList;
	}

	public void setTeoList(ArrayList<TestEntityOne> teoList) {
		this.teoList = teoList;
	}

	@RequestMapping("/test")
	public ArrayList<TestEntityOne> getTest(){
		return teoList;
	}
}

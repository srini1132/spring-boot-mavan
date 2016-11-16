package com.testcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestEntityOne {
	private String topoOne;
	private TestEntityTwo topoTwo;
	public String getTopoOne() {
		return topoOne;
	}
	public void setTopoOne(String topoOne) {
		this.topoOne = topoOne;
	}
	public TestEntityTwo getTopoTwo() {
		return topoTwo;
	}
	@Autowired
	public void setTopoTwo(TestEntityTwo topoTwo) {
		this.topoTwo = topoTwo;
	}
}

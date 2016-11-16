package com.testcontroller;

import org.springframework.stereotype.Component;

@Component
public class TestEntityTwo {
	private String toptOne;
	private String toptTwo;
	public String getToptOne() {
		return toptOne;
	}
	public void setToptOne(String toptOne) {
		this.toptOne = toptOne;
	}
	public String getToptTwo() {
		return toptTwo;
	}
	public void setToptTwo(String toptTwo) {
		this.toptTwo = toptTwo;
	}
}

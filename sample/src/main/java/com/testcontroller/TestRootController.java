package com.testcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableAutoConfiguration
public class TestRootController {
	private TestEntityOne teo;
	private TestEntityTwo tet;

	
	public TestEntityOne getTeo() {
		return teo;
	}

	@Autowired
	public void setTeo(TestEntityOne teo) {
		this.teo = teo;
	}

	public TestEntityTwo getTet() {
		return tet;
	}
	
	@Autowired
	public void setTet(TestEntityTwo tet) {
		this.tet = tet;
	}

	@RequestMapping("/a")
	public TestEntityOne getData(){
		tet.setToptOne("tetOne");
		tet.setToptTwo("tetTwo");
		teo.setTopoOne("teoOne");
		teo.setTopoTwo(tet);
		return teo;
	}
	
	@RequestMapping("/b")
	public String getTest(){
		return "check";
	}
}

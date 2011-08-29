package com.force.test.model;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Entity;

@Entity
public class JarEntity {

	// The Force.com JPA provider only supports annotating fields. Don't annotate
	// the getter and setter properties below.
	
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
	private String id;
    
	private String name;

	// TODO: Add additional commented-out sample fields
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


}

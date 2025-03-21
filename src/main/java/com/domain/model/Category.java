package com.domain.model;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "category", schema="domain")
public class Category {
	
	@Id
	@SequenceGenerator(name="categoryseq", initialValue=1, allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="categoryseq")
	private Long id;
	
	@Column(name="name", nullable=false, unique=true)
	private String name;
	
	private String description;
	private String reference;
	
	@Enumerated( EnumType.STRING )
	private DbSync dbSynch;
	private String dbSynchToken;
	
	public Category() {
		super();
		this.dbSynch = DbSync.ADD; 
	}
    public Category( String name, String reference, String description) {
    	this.name = name;
    	this.reference = reference;
    	this.description = description;
		this.dbSynch = DbSync.ADD; 
    }
    public Category( String name, String reference, String description, DbSync dbSynch, String dbSynchToken ) {
    	this.name = name;
    	this.reference = reference;
    	this.description = description;
    	this.dbSynch = dbSynch;
    	this.dbSynchToken = dbSynchToken;
    }
    
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getReference() {
		return reference;
	}
	public void setReference(String reference) {
		this.reference = reference;
	}

    public DbSync getDbSynch() {
		return dbSynch;
	}
	public void setDbSynch(DbSync dbSynch) {
		this.dbSynch = dbSynch;
	}

	public String getDbSynchToken() {
		return dbSynchToken;
	}
	public void setDbSynchToken(String dbSynchToken) {
		this.dbSynchToken = dbSynchToken;
	}
	
	@Override
	public String toString() {
		return "Category [id=" + id + ", name=" + name + ", description=" + description + ", reference=" + reference 
				+ ", dbSynch=" + dbSynch + ", dbSynchToken=" + dbSynchToken
				+ "]";
	}
	
}

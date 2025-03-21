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
@Table(name = "domain", schema="domain")
public class Domain {
	
	@Id
	@SequenceGenerator(name="domainseq", initialValue=1, allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="domainseq")
	private Long id;

	private int position;
	
	@Column(name="name", nullable=false, unique=true)
	private String name;
	
	private String image;
	private String description;
	private String categoryLabel;
	private String roleLabel;
	
	@Enumerated( EnumType.STRING )
	private DbSync dbSynch;
	private String dbSynchToken;
	
	public Domain() {
		super();
		this.dbSynch = DbSync.ADD; 
	}

    public Domain(Long id, int position, String name, String image, String description, String categoryLabel, String roleLabel, DbSync dbSynch, String dbSynchToken) {
		super();
		this.id = id;
		this.position = position;
		this.name = name;
		this.image = image;
		this.description = description;
		this.categoryLabel = categoryLabel;
		this.roleLabel = roleLabel;
		this.dbSynch = dbSynch;
		this.dbSynchToken = dbSynchToken;
	}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getCategoryLabel() {
		return categoryLabel;
	}
	public void setCategoryLabel(String categoryLabel) {
		this.categoryLabel = categoryLabel;
	}

	public String getRoleLabel() {
		return roleLabel;
	}
	public void setRoleLabel(String roleLabel) {
		this.roleLabel = roleLabel;
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
		return "Domain [id=" + id + ", position=" + position + ", name=" + name + ", image=" + image + ", description=" + description
				+ ", categoryLabel=" + categoryLabel + ", roleLabel=" + roleLabel + ", dbSynch=" + dbSynch
				+ ", dbSynchToken=" + dbSynchToken + "]";
	}
	
}

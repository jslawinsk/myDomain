package com.domain.model;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "process", schema="domain")
public class Process {
	
	@Id
	private String code;
	private String name;
	private boolean voiceAssist;
	
	@Enumerated( EnumType.STRING )
	private DbSync dbSynch;

	public Process() {
		super();
		this.dbSynch = DbSync.ADD; 
	}
	public Process( String code, String name) {
		super();
		this.code = code;
		this.name = name;
		this.voiceAssist = false;
		this.dbSynch = DbSync.ADD; 
	}
	public Process( String code, String name, DbSync dbSynch ) {
		super();
		this.code = code;
		this.name = name;
		this.voiceAssist = false;
    	this.dbSynch = dbSynch;
	}
	public Process( String code, String name, boolean voiceAssist, DbSync dbSynch ) {
		super();
		this.code = code;
		this.name = name;
		this.voiceAssist = voiceAssist;
    	this.dbSynch = dbSynch;
	}

	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

    public boolean isVoiceAssist() {
		return voiceAssist;
	}
	public void setVoiceAssist(boolean voiceAssist) {
		this.voiceAssist = voiceAssist;
	}
	
	public DbSync getDbSynch() {
		return dbSynch;
	}
	public void setDbSynch(DbSync dbSynch) {
		this.dbSynch = dbSynch;
	}
	
	@Override
	public String toString() {
		return "Process [code=" + code + ", name=" + name + ", voiceAssist=" + voiceAssist + ", dbSynch=" + dbSynch + "]";
	}
}

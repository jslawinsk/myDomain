package com.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name = "user", schema="domain")
public class User {

    @Id
    @SequenceGenerator(name="userseq", initialValue=1, allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="userseq")
	private Long id;

    @Column( name="username", nullable = false, unique = true )
	private String username;
    private String email;

    private String password;
    
	//@Enumerated( EnumType.STRING )
	//private UserRoles roles;
    private String roles;    
    
	private String token;
	
	//
	//	Set validated to true for updating existing data
	//
	@Column(name = "validated", nullable = false, columnDefinition = "boolean default true")	
	private boolean validated;
    
	@Enumerated( EnumType.STRING )
	private DbSync dbSynch;
    
	public User() {
		super();
		// this.roles = UserRoles.GUEST;
		this.dbSynch = DbSync.ADD; 
	}

	public User(String username, String password, String roles) {
		super();
		this.username = username;
		this.password = password;
		this.roles = roles;
		this.dbSynch = DbSync.ADD;
	}
	
	public User(String username, String password, DbSync dbSynch, String roles) {
		super();
		this.username = username;
		this.password = password;
		this.roles = roles;
		this.dbSynch = dbSynch;
	}

	public User(String username, String email, String password, DbSync dbSynch, String roles, boolean validated ) {
		super();
		this.username = username;
		this.email = email;
		this.password = password;
		this.roles = roles;
		this.dbSynch = dbSynch;
		this.validated = validated;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRoles() {
		return roles;
	}

	public void setRoles(String roles) {
		this.roles = roles;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
	
	public boolean isValidated() {
		return validated;
	}

	public void setValidated(boolean validated) {
		this.validated = validated;
	}

	public DbSync getDbSynch() {
		return dbSynch;
	}

	public void setDbSynch(DbSync dbSynch) {
		this.dbSynch = dbSynch;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", username=" + username + ", password=" + password + ", roles=" + roles
				+ ", email=" + email
				+ ", validated=" + validated
				+ ", token= " + token + ", dbSynch=" + dbSynch + "]";
	}
	
}

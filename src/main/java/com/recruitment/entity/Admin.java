package com.recruitment.entity;

import java.io.Serializable;

import jakarta.persistence.*;

@Entity
@Table(name = "admins")
public class Admin implements Serializable { // Add Serializable implementation
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)  // Auto-generate UUID
    @Column(name = "id", updatable = false, nullable = false)
    private String adminId;

    @Column(name = "name", nullable = false)
    private String name;

//    @Column(name = "last_name", nullable = false)
//    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "admin_key", nullable = false) // ✅ New field
    private String adminKey;

    // Constructors
    public Admin() {}

    

    public Admin(String adminId, String name, String email, String password, String adminKey) {
		super();
		this.adminId = adminId;
		this.name = name;
		//this.lastName = lastName;
		this.email = email;
		this.password = password;
		this.adminKey = adminKey;
	}



	public String getAdminId() {
		return adminId;
	}



	public void setAdminId(String adminId) {
		this.adminId = adminId;
	}



	public String getName() {
		return name;
	}



	public void setFirstName(String name) {
		this.name = name;
	}



//	public String getLastName() {
//		return lastName;
//	}
//
//
//
//	public void setLastName(String lastName) {
//		this.lastName = lastName;
//	}



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



	public String getAdminKey() {
		return adminKey;
	}



	public void setAdminKey(String adminKey) {
		this.adminKey = adminKey;
	}



	public static long getSerialversionuid() {
		return serialVersionUID;
	}



	// Getters and Setters
   
}

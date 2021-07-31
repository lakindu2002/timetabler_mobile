package com.cb007787.timetabler.model;

import java.util.Date;
import java.util.List;

public class User {
    private String username;
    private String firstName;
    private String lastName;
    private String emailAddress;
    private String contactNumber;
    private Date dateOfBirth;
    private Date memberSince;
    private int age;
    private String password;
    private String reEnterPassword;
    private Role userRole;
    private BatchShow theBatch; //used when dealing with student info
    private List<Module> theModule; //used when returning the modules taught by the lecturer

    public User() {
    }

    public BatchShow getTheBatch() {
        return theBatch;
    }

    public List<Module> getTheModule() {
        return theModule;
    }

    public void setTheModule(List<Module> theModule) {
        this.theModule = theModule;
    }

    public void setTheBatch(BatchShow theBatch) {
        this.theBatch = theBatch;
    }

    public Role getUserRole() {
        return userRole;
    }

    public void setUserRole(Role userRole) {
        this.userRole = userRole;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Date getMemberSince() {
        return memberSince;
    }

    public void setMemberSince(Date memberSince) {
        this.memberSince = memberSince;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getReEnterPassword() {
        return reEnterPassword;
    }

    public void setReEnterPassword(String reEnterPassword) {
        this.reEnterPassword = reEnterPassword;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", emailAddress='" + emailAddress + '\'' +
                ", contactNumber='" + contactNumber + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", memberSince=" + memberSince +
                ", age=" + age +
                ", password='" + password + '\'' +
                ", reEnterPassword='" + reEnterPassword + '\'' +
                ", userRole=" + userRole +
                '}';
    }
}

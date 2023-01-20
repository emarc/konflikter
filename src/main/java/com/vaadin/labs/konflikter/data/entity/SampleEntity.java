package com.vaadin.labs.konflikter.data.entity;

import java.time.LocalDate;

import javax.annotation.Nonnull;

import jakarta.persistence.Entity;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Email;

@Entity
public class SampleEntity extends AbstractEntity {
    @Version
    private Long version = 0l;

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Nonnull
    private String firstName = "Jordan";
    @Nonnull
    private String lastName = "Doe";
    @Email
    @Nonnull
    private String email = "jd@email.com";
    @Nonnull
    private String phone = "+358 5551234567890";
    private LocalDate dateOfBirth = LocalDate.of(2000, 11, 15);
    @Nonnull
    private String occupation = "Vaadiner";
    private boolean important = true;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public boolean isImportant() {
        return important;
    }

    public void setImportant(boolean important) {
        this.important = important;
    }

    @Nonnull
    private String street = "Ruukinkatu 2-4";
    @Nonnull
    private String postalCode = "20520";
    @Nonnull
    private String city = "Åbo";
    @Nonnull
    private String state = "-";
    @Nonnull
    private String country = "Finland";

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Nonnull
    private String cardNumber = "1234567890123456";
    @Nonnull
    private String cardholderName = lastName.toUpperCase() + " " + firstName.toUpperCase();
    @Nonnull
    private Integer validmonth = 11;
    @Nonnull
    private Integer validyear = 25;
    @Nonnull
    private String csc = "1234";

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardholderName() {
        return cardholderName;
    }

    public void setCardholderName(String cardholderName) {
        this.cardholderName = cardholderName;
    }

    public Integer getValidMonth() {
        return validmonth;
    }

    public void setValidMonth(Integer month) {
        this.validmonth = month;
    }

    public Integer getVAlidYear() {
        return validyear;
    }

    public void setValidYear(Integer year) {
        this.validyear = year;
    }

    public String getCsc() {
        return csc;
    }

    public void setCsc(String csc) {
        this.csc = csc;
    }

    public void createConflict() {
        if (Math.random() > 0.2)
            this.phone = "+44 " + Math.round(Math.random() * 9999999) + 1000000;
        if (Math.random() > 0.2)
            this.cardNumber = this.cardNumber + "1234";
        if (Math.random() > 0.2)
            this.city = "Turku";
        if (Math.random() > 0.2)
            this.csc = "" + Math.round(Math.random() * 1000 + 1000);
        if (this.dateOfBirth != null && Math.random() > 0.2)
            this.dateOfBirth = this.dateOfBirth.plusDays(1);
        if (Math.random() > 0.2)
            this.email += ".com";
        if (Math.random() > 0.2)
            this.postalCode = "" + Math.round(Math.random() * 1000 + 1000);
        if (Math.random() > 0.2)
            this.validyear = Double.valueOf(Math.random() * 5 + 20).intValue();
    }

}
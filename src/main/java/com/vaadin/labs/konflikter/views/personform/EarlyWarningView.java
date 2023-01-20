package com.vaadin.labs.konflikter.views.personform;

import java.time.LocalDate;

import javax.annotation.Nonnull;

import com.vaadin.collaborationengine.CollaborationAvatarGroup;
import com.vaadin.collaborationengine.CollaborationEngine;
import com.vaadin.collaborationengine.CollaborationMap;
import com.vaadin.collaborationengine.UserInfo;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.AvatarGroup;
import com.vaadin.flow.component.avatar.AvatarGroup.AvatarGroupItem;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.labs.konflikter.ConflictResolutionBinder;
import com.vaadin.labs.konflikter.data.entity.AbstractEntity;
import com.vaadin.labs.konflikter.views.MainLayout;

import jakarta.persistence.Version;
import jakarta.validation.constraints.Email;

@PageTitle("Early Warning Form")
@Route(value = "early-warning", layout = MainLayout.class)
@Uses(Icon.class)
public class EarlyWarningView extends Div {

    private TextField firstName = new TextField("First name");
    private TextField lastName = new TextField("Last name");
    private EmailField email = new EmailField("Email address");
    private DatePicker dateOfBirth = new DatePicker("Birthday");
    private PhoneNumberField phone = new PhoneNumberField("Phone number");
    private TextField occupation = new TextField("Occupation");

    private TextField street = new TextField("Street address");
    private TextField postalCode = new TextField("Postal code");
    private TextField city = new TextField("City");
    private ComboBox<String> state = new ComboBox<>("State");
    private ComboBox<String> country = new ComboBox<>("Country");

    private TextField cardNumber = new TextField("Credit card number");
    private TextField cardholderName = new TextField("Cardholder name");
    private Select<Integer> month = new Select<>();
    private Select<Integer> year = new Select<>();
    private ExpirationDateField expiration = new ExpirationDateField("Expiration date", month, year);
    private PasswordField csc = new PasswordField("CSC");

    private Button cancel = new Button("Cancel");
    private Button save = new Button("Save");

    private ConflictResolutionBinder<SampleEntity> binder = new ConflictResolutionBinder<>(SampleEntity.class);

    // CollaborationKit
    String userId = System.identityHashCode(UI.getCurrent()) + "";
    UserInfo localUser = new UserInfo(userId, "User " + userId);
    CollaborationAvatarGroup avatarGroup = new CollaborationAvatarGroup(
            localUser, "early-warning");
    CollaborationMap fieldValues;

    AvatarGroup editors = new AvatarGroup();
    Label warningMessage = new Label(" made changes while you were editing");
    HorizontalLayout earlyWarning = new HorizontalLayout(editors, warningMessage);

    SampleEntity sampleEntity = new SampleEntity();
    SampleEntity conflictEntity = new SampleEntity();
    Button resolveButton = new Button("Apply all their non-conflicting changes.");

    private Div conflictMessage = new Div(new Label(
            " made changes while you were editing. Please review and use MY EDIT or keep THEIR CHANGE. "),
            resolveButton) {
        @Override
        public void setVisible(boolean visible) {
            super.setVisible(visible);
            resolveButton.setVisible(visible);

        }
    };

    public EarlyWarningView() {
        addClassName("person-form-view");

        add(createTitle());
        add(avatarGroup);
        add(createFormLayout());

        avatarGroup.setOwnAvatarVisible(false);
        

        CollaborationEngine.getInstance().openTopicConnection(this, "early-warning",
                localUser, topic -> {
                    fieldValues = topic
                            .getNamedMap("whodonnit");
                            return fieldValues.subscribe(event -> {
                                if ("user".equals(event.getKey())) {
                                    UserInfo user = event.getValue(UserInfo.class);
                                    if (user.equals(localUser)) return;

                                    AvatarGroupItem item = new AvatarGroupItem();
                                    item.setName(user.getName());
                                    item.setAbbreviation(user.getAbbreviation());
                                    item.setImage(user.getImage());
                                    item.setColorIndex(CollaborationEngine.getInstance().getUserColorIndex(user));
                                    editors.add(item);

                                    earlyWarning.setVisible(true);
                                    save.setText("Resolve...");
                                }
                            });
                });

        resolveButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        resolveButton.addClickListener(click -> {
            binder.resolveNonconflicting();
            if (binder.isResolved()) {
                conflictMessage.setVisible(false);
            } else {
                resolveButton.setVisible(false);
            }
        });

        conflictMessage.setVisible(false);
        conflictMessage.setClassName("text-xs p-s");
        conflictMessage.getStyle().set("background-color", "orange");
        conflictMessage.getStyle().set("color", "white");
        add(conflictMessage);

        earlyWarning.setVisible(false);
        editors.setWidth("auto");
        editors.getStyle().set("display", "inline-block");
        add(earlyWarning);

        add(createButtonLayout());

        binder.bindInstanceFields(this);
        binder.setConflictMessage(conflictMessage);
        binder.setBean(sampleEntity);
        //conflictEntity.createConflict();

        cancel.addClickListener(e -> clearForm());
        save.addClickListener(e -> {

            // TODO need to save the new bean? 
            // Idea: singleton

            if (sampleEntity.getVersion() != conflictEntity.getVersion()) {
                sampleEntity.setVersion(conflictEntity.getVersion());
                binder.merge(conflictEntity);

            } else {
                Notification.show("Saved!");
                binder.refreshFields();
                //conflictEntity.createConflict();

                fieldValues.put("user", localUser);
            }
            save.setText("Save");
        });
    }

    private void clearForm() {
        binder.refreshFields();
    }

    private Component createTitle() {
        return new H3("Personal information");
    }

    private Component createFormLayout() {
        FormLayout formLayout = new FormLayout();
        email.setErrorMessage("Please enter a valid email address");
        formLayout.add(firstName, lastName, dateOfBirth, phone, email, occupation);

        formLayout.add(street, 2);
        postalCode.setPattern("\\d*");
        postalCode.setPreventInvalidInput(true);
        country.setItems("Country 1", "Country 2", "Country 3");
        state.setItems("State A", "State B", "State C", "State D");
        formLayout.add(postalCode, city, state, country);

        formLayout.add(cardNumber, cardholderName, expiration, csc);
        cardNumber.setPlaceholder("1234 5678 9123 4567");
        cardNumber.setPattern("[\\d ]*");
        cardNumber.setPreventInvalidInput(true);
        cardNumber.setRequired(true);
        cardNumber.setErrorMessage("Please enter a valid credit card number");
        month.setPlaceholder("Month");
        month.setItems(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
        year.setPlaceholder("Year");
        year.setItems(20, 21, 22, 23, 24, 25);

        return formLayout;
    }

    private Component createButtonLayout() {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.addClassName("button-layout");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save);
        buttonLayout.add(cancel);
        return buttonLayout;
    }

    private static class PhoneNumberField extends CustomField<String> {
        private ComboBox<String> countryCode = new ComboBox<>();
        private TextField number = new TextField();

        public PhoneNumberField(String label) {
            setLabel(label);
            countryCode.setWidth("120px");
            countryCode.setPlaceholder("Country");
            countryCode.setPattern("\\+\\d*");
            countryCode.setPreventInvalidInput(true);
            countryCode.setItems("+358", "+91", "+62", "+98", "+964", "+353", "+44", "+972", "+39", "+225");
            countryCode.addCustomValueSetListener(e -> countryCode.setValue(e.getDetail()));
            number.setPattern("\\d*");
            number.setPreventInvalidInput(true);
            HorizontalLayout layout = new HorizontalLayout(countryCode, number);
            layout.setFlexGrow(1.0, number);
            add(layout);
        }

        @Override
        protected String generateModelValue() {
            if (countryCode.getValue() != null && number.getValue() != null) {
                String s = countryCode.getValue() + " " + number.getValue();
                return s;
            }
            return "";
        }

        @Override
        protected void setPresentationValue(String phoneNumber) {
            String[] parts = phoneNumber != null ? phoneNumber.split(" ", 2) : new String[0];
            if (parts.length == 1) {
                countryCode.clear();
                number.setValue(parts[0]);
            } else if (parts.length == 2) {
                countryCode.setValue(parts[0]);
                number.setValue(parts[1]);
            } else {
                countryCode.clear();
                number.clear();
            }
        }
    }

    private class ExpirationDateField extends CustomField<String> {
        public ExpirationDateField(String label, Select<Integer> month, Select<Integer> year) {
            setLabel(label);
            HorizontalLayout layout = new HorizontalLayout(month, year);
            layout.setFlexGrow(1.0, month, year);
            month.setWidth("100px");
            year.setWidth("100px");
            add(layout);
        }

        @Override
        protected String generateModelValue() {
            // Unused as month and year fields part are of the outer class
            return "";
        }

        @Override
        protected void setPresentationValue(String newPresentationValue) {
            // Unused as month and year fields part are of the outer class
        }

    }

    public static class SampleEntity extends AbstractEntity {
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
        private Integer month = 11;
        @Nonnull
        private Integer year = 25;
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

        public Integer getMonth() {
            return month;
        }

        public void setMonth(Integer month) {
            this.month = month;
        }

        public Integer getYear() {
            return year;
        }

        public void setYear(Integer year) {
            this.year = year;
        }

        public String getCsc() {
            return csc;
        }

        public void setCsc(String csc) {
            this.csc = csc;
        }

        protected void createConflict() {
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
                this.year = Double.valueOf(Math.random() * 5 + 20).intValue();
            this.version++;
        }

    }

}

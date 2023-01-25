package com.vaadin.labs.konflikter.views.personform;

import java.time.Duration;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import com.vaadin.collaborationengine.CollaborationAvatarGroup;
import com.vaadin.collaborationengine.CollaborationEngine;
import com.vaadin.collaborationengine.CollaborationMap;
import com.vaadin.collaborationengine.UserInfo;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.AvatarGroup;
import com.vaadin.flow.component.avatar.AvatarGroupVariant;
import com.vaadin.flow.component.avatar.AvatarGroup.AvatarGroupItem;
import com.vaadin.flow.component.avatar.AvatarVariant;
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
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.labs.konflikter.ConflictResolutionBinder;
import com.vaadin.labs.konflikter.data.entity.SampleEntity;
import com.vaadin.labs.konflikter.data.service.SampleEntityService;
import com.vaadin.labs.konflikter.views.MainLayout;

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
    Label warningMessage = new Label("made some changes you need to resolve before saving.");
    HorizontalLayout earlyWarning = new HorizontalLayout(editors, warningMessage);

    SampleEntity sampleEntity = new SampleEntity();

    Button resolveButton = new Button("Apply all their non-conflicting changes");

    private Div conflictMessage = new Div(new Label(
            "Please review changes and use MY EDIT or keep THEIR CHANGE. "),
            resolveButton) {
        @Override
        public void setVisible(boolean visible) {
            super.setVisible(visible);
            resolveButton.setVisible(visible);

        }
    };

    SampleEntityService sampleEntityService;

    @Autowired
    public EarlyWarningView(SampleEntityService sampleEntityService) {
        addClassName("person-form-view");

        this.sampleEntityService = sampleEntityService;
        sampleEntityService.get(1l).ifPresentOrElse(entity -> {
            sampleEntity = entity;
        }, () -> {
            Notification notification = new Notification("Something is wrong with the sample data!");
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.open();
        });

        Button generateConfict = new Button("Generate conflict", click -> {
            sampleEntityService.get(1l).ifPresentOrElse(entity -> {
                entity.createConflict();
                sampleEntityService.update(entity);
            }, () -> {
                Notification notification = new Notification("Something is wrong with the sample data!");
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                notification.open();
            });

            int r = new Random().nextInt(26);
            String userId = "Bot" + r;
            char randomChar = (char)(r + 'A');
            UserInfo botUser = new UserInfo(userId, "Bot " + randomChar);
            fieldValues.put("user", botUser);
            fieldValues.put("user", null);
        });
        generateConfict.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        add(generateConfict);

        add(createTitle());

        add(avatarGroup);

        add(createFormLayout());

        avatarGroup.setOwnAvatarVisible(false);

        CollaborationEngine.getInstance().openTopicConnection(this, "early-warning",
                localUser, topic -> {
                    fieldValues = topic
                            .getNamedMap("whodonnit");
                    fieldValues.setExpirationTimeout(Duration.ZERO); // Zero timeout here?
                    return fieldValues.subscribe(event -> {
                        if ("user".equals(event.getKey())) {
                            UserInfo user = event.getValue(UserInfo.class);
                            if (user == null || user.equals(localUser))
                                return;

                            AvatarGroupItem item = new AvatarGroupItem();
                            item.setName(user.getName());
                            item.setAbbreviation(user.getAbbreviation());
                            item.setImage(user.getImage());
                            item.setColorIndex(CollaborationEngine.getInstance().getUserColorIndex(user));
                            // Let's make sure the last one is "on top"; TODO feature request for CollabKit
                            List<AvatarGroupItem> oldItems = editors.getItems();
                            editors.setItems(item);
                            editors.add(oldItems.toArray(new AvatarGroupItem[0]));

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
                editors.getItems().forEach(editors::remove);
                earlyWarning.setVisible(false);
            } else {
                resolveButton.setVisible(false);
            }
        });

        conflictMessage.setVisible(false);
        conflictMessage.setClassName("flex flex-row items-center justify-between p-xs text-xs font-bold");
        conflictMessage.getStyle().set("border", "1px solid var(--lumo-primary-color)");
        conflictMessage.getStyle().set("color", "var(--lumo-primary-color)");
        add(conflictMessage);

        add(createButtonLayout());

        binder.bindInstanceFields(this);
        binder.setConflictMessage(conflictMessage);
        binder.setBean(sampleEntity);

        cancel.addClickListener(e -> {
            UI.getCurrent().navigate("/");
        });
        save.addClickListener(e -> {
            if (!binder.isResolved()) {
                Notification.show("Please resolve all changed fields", 3000, Position.MIDDLE);
                return;
            }
            try {
                binder.writeBean(this.sampleEntity);
                sampleEntityService.update(this.sampleEntity);
                Notification.show("Saved!");
                fieldValues.put("user", localUser);
                fieldValues.put("user", null);
                UI.getCurrent().navigate("/");

            } catch (ValidationException validationException) {
                Notification.show("An exception happened while trying to store the samplePerson details.");

            } catch (ObjectOptimisticLockingFailureException lockingException) {
                this.sampleEntity = sampleEntityService.get((Long) lockingException.getIdentifier()).get();
                binder.merge(this.sampleEntity);
                earlyWarning.setVisible(false);
            }

            save.setText("Save");
        });
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
        buttonLayout.setAlignItems(Alignment.CENTER);
        buttonLayout.addClassName("button-layout");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save);

        buttonLayout.add(cancel);

        earlyWarning.setVisible(false);
        earlyWarning.setClassName("text-xs font-bold p-xs");
        earlyWarning.getStyle().set("color", "var(--lumo-primary-color)");
        earlyWarning.setAlignItems(Alignment.CENTER);
        editors.setWidth("auto");
        editors.getStyle().set("display", "inline-block");
        editors.addThemeVariants(AvatarGroupVariant.LUMO_SMALL);
        buttonLayout.add(earlyWarning);

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

}

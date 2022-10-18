package com.vaadin.labs.konflikter;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.HasHelper;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.shared.Registration;

/**
 * Binder with enhancements for conflict resolution.
 */
public class ConflictResolutionBinder<BEAN> extends BeanValidationBinder<BEAN> {

    protected Set<Binding<BEAN, ?>> changedBindings = new LinkedHashSet<>();
    Map<Binding<BEAN, ?>, Object> originalValues;
    Component conflictMessage;

    public ConflictResolutionBinder(Class<BEAN> beanType) {
        this(beanType, false);
    }

    public ConflictResolutionBinder(Class<BEAN> beanType, boolean scanNestedDefinitions) {
        super(beanType, scanNestedDefinitions);
    }

    public void setConflictMessage(Component message) {
        this.conflictMessage = message;
    }

    public Optional<Component> getConflictMessage() {
        return Optional.ofNullable(conflictMessage);
    }

    protected void setConflictMessageVisible(boolean visible) {
        getConflictMessage().ifPresent(component -> component.setVisible(visible));
    }

    @Override
    protected void handleFieldValueChange(Binding<BEAN, ?> binding) {
        this.changedBindings.add(binding);

        HasValue<?, ?> field = binding.getField();
        applyEditedStyle(field);
        super.handleFieldValueChange(binding);
    }

    protected void applyEditedStyle(HasValue<?, ?> field) {
        if (field instanceof HasStyle) {
            // TODO theme variant or something?
            ((HasStyle) field).getStyle().set("font-style", "italic");
            ((HasStyle) field).getStyle().set("color", "var(--lumo-primary-text-color");
        }
    }

    protected void removeEditedStyle(HasValue<?, ?> field) {
        if (field instanceof HasStyle) {
            // TODO theme variant or something?
            ((HasStyle) field).getStyle().remove("font-style");
            ((HasStyle) field).getStyle().remove("color");
        }
    }

    protected void resetEditedStyle() {
        getBindings().forEach(binding -> {
            removeEditedStyle(binding.getField());
        });
    }

    public boolean hasMergableChanges() {
        return !this.changedBindings.isEmpty();
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void readBean(BEAN bean) {
        this.changedBindings.clear();
        setConflictMessageVisible(false);
        removeMergeHelpers();
        resetEditedStyle();
        super.readBean(bean);
        this.originalValues = readFieldValues();
    }

    protected Map<Binding<BEAN, ?>, Object> readFieldValues() {
        HashMap<Binding<BEAN, ?>, Object> map = new HashMap<>();
        getBindings().forEach(binding -> {
            Object val = binding.getField().getValue();
            map.put(binding, val);
        });
        return map;
    }

    protected void writeFieldValues(Map<Binding<BEAN, ?>, Object> values) {
        values.forEach((binding, value) -> {
            ((HasValue<?, Object>) binding.getField()).setValue(value);
        });
    }

    @Override
    public void refreshFields() {
        this.changedBindings.clear();
        setConflictMessageVisible(false);
        resetEditedStyle();
        super.refreshFields();
    }

    @Override
    public void removeBean() {
        this.changedBindings.clear();
        this.originalValues = null;
        setConflictMessageVisible(false);
        resetEditedStyle();
        removeMergeHelpers();
        super.removeBean();
    }

    @Override
    public void setBean(BEAN bean) {
        this.changedBindings.clear();
        setConflictMessageVisible(false);
        resetEditedStyle();
        removeMergeHelpers();
        super.setBean(bean);
        this.originalValues = readFieldValues();
    }

    public void removeMergeHelpers() {
        getBindings().forEach(binding -> {
            if (binding.getField() instanceof HasHelper) {
                HasHelper hf = (HasHelper) binding.getField();
                if (hf.getHelperComponent() instanceof MergeHelper) {
                    hf.setHelperComponent(null);
                }
            }
        });
    }

    public void merge(BEAN otherBean) {
        removeMergeHelpers();

        // Save values currently in fields
        Map<Binding<BEAN, ?>, Object> currentValues = readFieldValues();
        // Load value to merge into fields so we get converted values, then save
        super.readBean(otherBean);
        Map<Binding<BEAN, ?>, Object> otherValues = readFieldValues();
        // Restore saved current values
        writeFieldValues(currentValues);

        getBindings().forEach(binding -> {
            HasValue<?, ?> field = binding.getField();
            Object current = field.getValue();
            Object orig = this.originalValues.get(binding);
            Object other = otherValues.get(binding);

            if (!Objects.equals(current, orig) || !Objects.equals(orig, other)) {
                // Some values differ, let's show the helper
                if (field instanceof HasHelper) {
                    ((HasHelper) field).setHelperComponent(new MergeHelper(field, other, orig));
                } else {
                    // TODO e.g checkbox does not work
                }
            }

        });

        setConflictMessageVisible(true);
    }

    public static class MergeHelper<V> extends Span {

        HasValue<?, V> field;
        HasHelper helperField;

        String originalHelperText;
        Component originalHelperComponent;

        V dbValue;
        V currentValue;
        V origValue;

        Button orig = new Button(VaadinIcon.CLOCK.create());
        Button db = new Button(VaadinIcon.DATABASE.create());
        Button current = new Button(VaadinIcon.EDIT.create());
        Button active = current;

        Registration changeRegistration;

        public MergeHelper(HasValue<?, V> field, V dbValue, V origValue) {
            this.field = field;
            currentValue = field.getValue();
            this.origValue = origValue;
            this.dbValue = dbValue;

            if (field instanceof HasHelper) {
                this.helperField = (HasHelper) field;
                this.originalHelperText = this.helperField.getHelperText();
                this.originalHelperComponent = this.helperField.getHelperComponent();
            }

            orig.addClickListener(click -> {
                activate(orig);
            });

            db.addClickListener(click -> {
                activate(db);
            });

            current.addClickListener(click -> {
                activate(current);
            });

            orig.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
            db.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
            current.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
            orig.setClassName("m-0 text-xs rounded-none");
            db.setClassName("m-0 text-xs rounded-none");
            current.setClassName("m-0 text-xs rounded-none");
            orig.getStyle().set("color", "var(--lumo-secondary-text-color)");
            db.getStyle().set("color", "var(--lumo-error-text-color)");
            current.getStyle().set("color", "var(--lumo-primary-text-color)");

            updateButtons();

            add(current, db, orig);

            orig.getElement().setProperty("title", "Original value: " + origValue);
            db.getElement().setProperty("title", "Value in database: " + dbValue);
            current.getElement().setProperty("title", "Your value: " + currentValue);

            changeRegistration = field.addValueChangeListener((ValueChangeListener<ValueChangeEvent<V>>) change -> {
                if (change.isFromClient()) {
                    currentValue = change.getValue();
                    updateButtons();
                    activate(current);
                }
            });
        }

        private void updateButtons() {
            current.setVisible(true);
            orig.setVisible(true);
            db.setVisible(true);
            if (Objects.equals(origValue, currentValue)) {
                // Not edited
                current.setVisible(false);
                if (Objects.equals(origValue, dbValue)) {
                    // no conflict
                    db.setVisible(false);
                    activate(orig);
                } else {
                    activate(db);
                }
            } else {
                // Edited
                if (Objects.equals(currentValue, dbValue)) {
                    // no conflict, but odd to hide the edit
                    // TODO needs UX thought
                    // current.setVisible(false);
                    activate(current);
                } else if (Objects.equals(dbValue, origValue)) {
                    // not changed in db
                    db.setVisible(false);
                    activate(current);
                } else {
                    activate(current);
                }

            }
        }

        private void activate(Button b) {
            if (active != null) {
                active.getStyle().remove("border-top");
            }
            active = b;

            if (b == orig) {
                field.setValue(origValue);
                if (field instanceof TextField) {
                    ((TextField) field).getStyle().remove("color");
                }
                b.getStyle().set("border-top", "2px solid var(--lumo-secondary-text-color)");
            } else if (b == db) {
                field.setValue(dbValue);
                if (field instanceof TextField) {
                    ((TextField) field).getStyle().set("color", "var(--lumo-error-text-color)");
                }
                b.getStyle().set("border-top", "2px solid var(--lumo-error-text-color)");
            } else {
                field.setValue(currentValue);
                if (field instanceof TextField) {
                    ((TextField) field).getStyle().set("color", "var(--lumo-primary-text-color)");
                }
                b.getStyle().set("border-top", "2px solid var(--lumo-primary-text-color)");
            }
        }

        @Override
        protected void onDetach(DetachEvent detachEvent) {
            if (this.helperField != null) {
                if (this.originalHelperText != null) {
                    this.helperField.setHelperText(this.originalHelperText);
                }
                if (this.originalHelperComponent != null) {
                    this.helperField.setHelperComponent(this.originalHelperComponent);
                }
            }
            if (changeRegistration != null) {
                changeRegistration.remove();
                changeRegistration = null;
            }
            this.field = null;
            super.onDetach(detachEvent);
        }

    }

}

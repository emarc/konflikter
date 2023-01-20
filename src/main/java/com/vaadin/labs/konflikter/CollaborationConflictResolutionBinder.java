package com.vaadin.labs.konflikter;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasHelper;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.select.SelectVariant;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.shared.Registration;

/**
 * Binder with enhancements for conflict resolution.
 */
public class CollaborationConflictResolutionBinder<BEAN> extends BeanValidationBinder<BEAN> {

    public static final String CLASSNAME_EDITED = "edited";
    public static final String CLASSNAME_UPDATED = "updated";
    public static final String CLASSNAME_MATCH = "match";
    public static final String CLASSNAME_CONFLICT = "conflict";

    protected Set<Binding<BEAN, ?>> changedBindings = new LinkedHashSet<>();
    Map<Binding<BEAN, ?>, Object> originalValues;
    Component conflictMessage;

    public CollaborationConflictResolutionBinder(Class<BEAN> beanType) {
        this(beanType, false);
    }

    public CollaborationConflictResolutionBinder(Class<BEAN> beanType, boolean scanNestedDefinitions) {
        super(beanType, scanNestedDefinitions);
    }

    /*
     * Analog to setStatusLabel() but intended to be hidden/shown in the form,
     * because I'm not sure how you're supposed to style it in a sane way otherwise.
     */
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
        // This is really just visually indicating fields and making
        // hasMergableChanges() work – and the latter should be renamed.
        this.changedBindings.add(binding);
        HasValue<?, ?> field = binding.getField();
        applyEditedStyle(field);
        super.handleFieldValueChange(binding);
    }

    protected void applyEditedStyle(HasValue<?, ?> field) {

        if (field instanceof HasStyle) {
            ((HasStyle) field).addClassName(CLASSNAME_EDITED);
        }
    }

    protected void removeEditedStyle(HasValue<?, ?> field) {
        if (field instanceof HasStyle) {
            ((HasStyle) field).removeClassName(CLASSNAME_EDITED);
        }
    }

    protected void resetEditedStyle() {
        getBindings().forEach(binding -> {
            removeEditedStyle(binding.getField());
        });
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

    /*
     * TODO probably put MergeHelpers in Map to aid removing and fiddling
     */
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
            Object orig = this.originalValues.get(binding);
            Object other = otherValues.get(binding);

            if (field instanceof HasHelper) {
                ((HasHelper) field).setHelperComponent(new MergeHelperDropdown(field, other, orig));
            } else {
                // TODO e.g checkbox does not work
            }

        });

        setConflictMessageVisible(true);
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

    public boolean isEdited() {
        return !this.changedBindings.isEmpty();
    }

    public boolean isResolved() {
        return !getBindings().stream().anyMatch(binding -> {
            if (binding.getField() instanceof HasHelper) {
                HasHelper hf = (HasHelper) binding.getField();
                if (hf.getHelperComponent() instanceof MergeHelper) {
                    MergeHelper<?> mh = (MergeHelper<?>) hf.getHelperComponent();
                    return !mh.isResolved();
                }
            }
            return false;
        });
    }

    public void resolveNonconflicting() {
        getBindings().forEach(binding -> {
            if (binding.getField() instanceof HasHelper) {
                HasHelper hf = (HasHelper) binding.getField();
                if (hf.getHelperComponent() instanceof MergeHelper) {
                    ((MergeHelper<?>) hf.getHelperComponent()).resolveNonconflicting();
                }
            }
        });
    }

    public static abstract class MergeHelper<V> extends Span {
        protected HasValue<?, V> field;
        protected HasHelper helperField;

        protected String originalHelperText;
        protected Component originalHelperComponent;

        protected V dbValue;
        protected V currentValue;
        protected V origValue;

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
        }

        protected boolean isEdited() {
            return !Objects.equals(origValue, currentValue);
        }

        protected boolean isUpdated() {
            return !Objects.equals(dbValue, origValue);
        }

        protected boolean isConflict() {
            return isUpdated() && isEdited() && !Objects.equals(currentValue, dbValue);
        }

        protected boolean isMatch() {
            return isUpdated() && isEdited() && Objects.equals(currentValue, dbValue);
        }

        abstract void resolveNonconflicting();

        abstract boolean isResolved();
    }

    public static class MergeHelperDropdown<V> extends MergeHelper<V> {
        enum Resolution {
            KEEP("My edit"),
            REFRESH("Their change"),
            ORIGINAL("Original value");

            private String name;

            Resolution(String name) {
                this.name = name;
            }

            @Override
            public String toString() {
                return name;
            }
        }

        protected Select<Resolution> select = new Select<>();
        Registration changeRegistration;

        public MergeHelperDropdown(HasValue<?, V> field, V dbValue, V origValue) {
            super(field, dbValue, origValue);

            select.addThemeVariants(SelectVariant.LUMO_SMALL);
            select.setItems(Resolution.values());
            select.addValueChangeListener(change -> {        
                // TODO Not sure why the phone field sends null here...
                if (change.getValue() == null) {
                    return;
                }
                switch (change.getValue()) {
                    case KEEP:
                        field.setValue(currentValue);
                        applyFieldClass(CLASSNAME_EDITED);
                        break;
                    case REFRESH:
                        field.setValue(dbValue);
                        applyFieldClass(CLASSNAME_UPDATED);
                        break;
                    case ORIGINAL:
                        field.setValue(origValue);
                        applyFieldClass("");
                        break;
                }
            });
            add(select);

            changeRegistration = field.addValueChangeListener((ValueChangeListener<ValueChangeEvent<V>>) change -> {
                if (change.isFromClient()) {
                    currentValue = change.getValue();
                    update();
                }
            });

            update();
        }

        private void update() {
            select.setClassName(null);
            select.setVisible(true);
            select.setValue(null);
            // Disable choices that are not available
            select.setItemEnabledProvider(item -> {
                switch (item) {
                    case KEEP:
                        return isEdited();
                    case REFRESH:
                        return isUpdated();
                    default:
                        return true;
                }
            });

            if (isEdited()) {
                // Edited
                if (isMatch()) {
                    // edit = change => no conflict, but odd to hide the edit?
                    select.setPlaceholder("Changed");
                    applyFieldClass(CLASSNAME_MATCH);
                } else if (!isUpdated()) {
                    // edited, not changed in db
                    select.setPlaceholder("Edited");
                    applyFieldClass(CLASSNAME_EDITED);
                    select.setValue(Resolution.KEEP);
                } else {
                    // conflict, all differs
                    select.setPlaceholder("Conflict");
                    applyFieldClass(CLASSNAME_CONFLICT);
                }

            } else {
                // Not edited
                if (Objects.equals(origValue, dbValue)) {
                    // not edited, not changed
                    select.setVisible(false);
                } else {
                    // not edited, but changed
                    select.setPlaceholder("Changed");
                    applyFieldClass(CLASSNAME_UPDATED);
                }

            }
        }

        protected void applyFieldClass(String className) {
            select.setClassName("mergehelper " + className);
            if (field instanceof HasStyle) {
                HasStyle hs = (HasStyle) field;
                hs.removeClassName(CLASSNAME_EDITED);
                hs.removeClassName(CLASSNAME_CONFLICT);
                hs.removeClassName(CLASSNAME_MATCH);
                hs.removeClassName(CLASSNAME_UPDATED);
                hs.addClassName(className);
            }
        }

        @Override
        void resolveNonconflicting() {
            if (isConflict()) {
                return;
            } else if (isEdited()) {
                select.setValue(Resolution.KEEP);
            } else if (isUpdated()) {
                select.setValue(Resolution.REFRESH);
            }
        }

        @Override
        boolean isResolved() {
            return !select.isVisible() || select.getValue() != null;
        }
    }

}

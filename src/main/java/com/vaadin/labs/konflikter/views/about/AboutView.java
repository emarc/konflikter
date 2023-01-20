package com.vaadin.labs.konflikter.views.about;

import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.labs.konflikter.views.MainLayout;

@PageTitle("Conflict resolution demo")
@Route(value = "", layout = MainLayout.class)
public class AboutView extends VerticalLayout {
    
    public AboutView() {
        add(new Span("Different versions of conflict resolution with optimistic locking on the database level."));
        add(new Span("To maintain accountability and validity, many applications need an explicit save-action that guarantees the user is saving exactly what they think."));
        add(new Span("1. Enter resolve-mode if save throws (no CollaborationKit)"));
        add(new Span("2. Warn immediately when the item has been changed."));
        add(new Span("3. Indicate each field that has been changed."));
        add(new Span("Press 'Generate conflict' or use a separate browser window to create a conflict."));
    }

}

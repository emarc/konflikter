package com.vaadin.labs.konflikter.views;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.labs.konflikter.components.appnav.AppNav;
import com.vaadin.labs.konflikter.components.appnav.AppNavItem;
import com.vaadin.labs.konflikter.views.about.AboutView;
import com.vaadin.labs.konflikter.views.personform.CollabView;
import com.vaadin.labs.konflikter.views.personform.EarlyWarningView;
import com.vaadin.labs.konflikter.views.personform.LiveConflictView;
import com.vaadin.labs.konflikter.views.personform.PersonFormView;

/**
 * The main view is a top-level placeholder for other views.
 */
public class MainLayout extends AppLayout {

    private H2 viewTitle;

    public MainLayout() {
        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.getElement().setAttribute("aria-label", "Menu toggle");

        viewTitle = new H2();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        addToNavbar(true, toggle, viewTitle);
    }

    private void addDrawerContent() {
        H1 appName = new H1("Konflikter");
        appName.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
        Header header = new Header(appName);

        Scroller scroller = new Scroller(createNavigation());

        addToDrawer(header, scroller, createFooter());
    }

    private AppNav createNavigation() {
        // AppNav is not yet an official component.
        // For documentation, visit https://github.com/vaadin/vcf-nav#readme
        AppNav nav = new AppNav();

        nav.addItem(new AppNavItem("About", AboutView.class, "la la-info-circle"));
        nav.addItem(new AppNavItem("Conflict Resolution", PersonFormView.class, "la la-handshake"));
        nav.addItem(new AppNavItem("Early Warning", EarlyWarningView.class, "la la-hands-helping"));
        nav.addItem(new AppNavItem("Live Conflict", LiveConflictView.class, "la la-users"));
        return nav;
    }

    private Footer createFooter() {
        Footer layout = new Footer();
        layout.add(new Html(
                "<div>Edit some stuff, but <b>before you save</b> click <i>Generate confict</i> OR save some changes in a separate browser window.</div>"));
        return layout;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }
}

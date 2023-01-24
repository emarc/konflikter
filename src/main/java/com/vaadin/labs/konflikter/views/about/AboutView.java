package com.vaadin.labs.konflikter.views.about;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.labs.konflikter.views.MainLayout;

@PageTitle("Conflict resolution demo")
@Route(value = "", layout = MainLayout.class)
public class AboutView extends VerticalLayout {
    
    public AboutView() {
        add(new Html("""
            <div>
            <h3>Imagine you are updating some information in a complex form.</h3>
            <i>Unfortunately, someone else happens to be updating the same information at the same time.</i>
            <p>
            All these examples use an <b>drop-in Binder replacement for in-form conflict resolution</b>, 
            but implement slightly different strategies (“eagerness”) to indicate when a conflict occurs.<br/>
            The first example invokes conflict resolution only when a optimistic locking exception occurs during save, 
            while the other two use <b>Collaboration Kit</b> to help the anticipate and resolve conflicts more “eagerly”. 
            </p>
            <p>
            <b>Compared to the live collaborative form</b> editing functionality provided by default in the Collaboration Kit, 
            these approaches may be more appropriate for situations where <b>accountability</b> and validity is important. 
            When the user clicks Save, they know that they are saving exactly what they see in the form.<br/> 
            This makes it possible to audit who changed what, and expect that to match with reality. 
            </p>
            <p>
            Explicit Save w/o collaborative editing can also be easier to understand for some users, 
            but resolving conflicts then becomes the usability challenge. More user testing of the solutions is needed. 
            Luckily the user can always Cancel and start over if things get confusing. 
            </p>
            <p>
            Depending on the use case, automatically applying non-conflicting changes might be desirable. 
            The current implementation defaults to "safety first", requiring the user to make explicit choices.
            </p>
            </div>
                """));
    }

}

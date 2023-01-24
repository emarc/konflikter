# Konflikter

_Imagine you are updating some information in a complex form.
Unfortunately, someone else happens to be updating the same information at the same time._

This application shows a few examples of conflict resolution.

All the examples use an drop-in Binder replacement for conflict-resolutions, but implement slightly different strategies (“eagerness”) to indicate when a conflict occurs. 
The first example invokes conflict-resolution only when a optimistic locking exception occurs during save, while the other two use Collaboration Kit to give the user to anticipate and resolve conflicts more “eagerly”. 

Compared to the live collaborative form editing functionality provided by default in the Collaboration Kit, these approaches may be more appropriate for situations where accountability and validity is important. An explicit Save that requires the user to review all changes makes it possible to audit who changed what, and expect that to match with reality. 

Explicit Save w/o collaborative editing can also be easier to understand for some users, but resolving conflicts then becomes the usability challenge. More user testing of the solutions is needed. Luckily the user can always Cancel and start over if things get scary – essentially reverting to the (almost industry standard) “the data has changes, please re-enter your information and try again”. 

Depending on the use case, automatically applying non-conflicting changes might be desirable. The current implementation defaults to the “safe” solution, requiring the user to make explicit choices.


## Running the application

The project is a standard Maven project. To run it from the command line,
type `mvnw` (Windows), or `./mvnw` (Mac & Linux), then open
http://localhost:8080 in your browser.

## How does it work?
All example forms make use of `ConflictResolutionBinder`, which is a drop-in replacement for `BeanValidationBinder`.


```
private ConflictResolutionBinder<SampleEntity> binder = new ConflictResolutionBinder<>(SampleEntity.class);
// ...
binder.bindInstanceFields(this);
binder.setBean(sampleEntity);
```

The conflict resolution UI is provided by the Binder, and happens in the form, allowing the form to be completely custom. The conflic resolution UI is triggered by giving the Binder a second bean:

```
binder.merge(freshEntityFromDB);
```

In all cases, database optimistic locking is in use so that the update operation throws if the data was changed. This triggers the conflic resolution UI in all cases.

The more "eager" examples use **Collaboration Kit** to message in real time when the form is saved. A `CollaborationAvatarGroup` is also added to make the user aware that someone else is editing the form.
A `topic` specific to the entity being edited is used to signal when the entity is saved.

The semi-eager example shows a message when someone else has made changes, but requires the user to explicitly press a button to enter conflict resolution.

The most eager example enters conflict resolution mode immediately when someone else saves a change.

## Future improvements

### UX
The UX of the conflict resolution UI is the most cruicial part, and has potential for improvement. One could even imagine multip pre-build versions that can be applied in different situations.

Indicating the state of each field clearly using colors, icons, and other means is worth more exploration.

### Configurability
Sometimes applying non-conflicting changes is the best thing to do, other times optimising for user awareness is cruicial. Different modes could be configurable.

### More variants
Indicating who changed each field as the change is saved is possible and would be an interesting variant.

The conflict resolution dropdown could even indicate multiple versions – though the usefulness of making it more complex is a bit unclear.

Allowing the users to send messages trough the `CollaborationAvatarGroup` (precense indicator) could be useful for this use case ("Hey, Bob, are updating the address?"), but is also a separate compononent useful for other cases.
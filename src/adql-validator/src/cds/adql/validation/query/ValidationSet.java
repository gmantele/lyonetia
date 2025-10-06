package cds.adql.validation.query;

import java.util.*;

/**
 * Representation of a set of Validation Tests.
 *
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 2.0 (05/2025)
 */
public class ValidationSet implements Iterable<ValidationQuery> {

    /** Title of this set. It is mainly used in the validation report to nicely
     * identify each validation set. */
    private String title;

    /** Person/Entity to contact in case of question/comment about this set. */
    private Contact contact = null;

    /** Person/Entity who published this set. */
    private Publisher publisher = null;

    /** Human description of this entire set.*/
    private String description = null;

    /** Definitions of all allowed User Defined Functions. */
    private final Set<UDF> functions = new LinkedHashSet<>();

    /** Set of all Validation Tests included inside this set.
     * <p>
     *     It contains only unique items: no two {@link ValidationQuery}
     *     instances have the same UUID.
     * </p> */
    private final Set<ValidationQuery> queries = new LinkedHashSet<>(5, .85f); // load factor computed so that avoiding too many map resizes, based on the 05/2025 version of all query sets

    /**
     * Create a validation set.
     *
     * <p>
     *     The given title MUST be non-<code>null</code> and non-empty. It is
     *     used as default title in order to have a nice way to identify a
     *     validation set. It can be changed later thanks to
     *     {@link #setTitle(String)}.
     * </p>
     *
     * @param title (required) Default title to identify this set.
     */
    public ValidationSet(final String title) {
        if (title == null || title.isBlank())
            throw new NullPointerException("Missing title for the new Validation Set!");

        this.title = Objects.requireNonNull(title);
    }

    public String getTitle() {
        return title;
    }

    /**
     * Set a new title for this validation set.
     *
     * <p>
     *     This operation fails when the given title is <code>null</code> or
     *     represents an empty string. In such case, this function returns
     *     <code>false</code>.
     * </p>
     *
     * @param title The new title (MUST be non <code>null</code> and not empty).
     *
     * @return  <code>true</code> if the title has been replaced,
     *          <code>false</code> otherwise.
     */
    public boolean setTitle(final String title) {
        if (title != null && !title.isBlank()) {
            this.title = title;
            return true;
        }
        else
            return false;
    }

    public Optional<Contact> getContact() {
        return Optional.ofNullable(contact);
    }

    public void setContact(final Contact contact) {
        this.contact = contact;
    }

    public Optional<Publisher> getPublisher() {
        return Optional.ofNullable(publisher);
    }

    public void setPublisher(final Publisher publisher) {
        this.publisher = publisher;
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    public void setDescription(final String description) {
        if (description == null || description.isBlank())
            this.description = null;
        else
            this.description = description.trim();
    }

    public void add(final UDF function){
        if (function != null)
            functions.add(function);
    }

    public int countFunctions(){
        return functions.size();
    }

    public Iterator<UDF> getFunctions(){
        return functions.iterator();
    }

    public void add(final ValidationQuery query){
        if (query != null)
            queries.add(query);
    }

    public boolean isEmpty(){
        return queries.isEmpty();
    }

    public int countQueries(){
        return queries.size();
    }

    public Iterator<ValidationQuery> getQueries(){
        return queries.iterator();
    }

    @Override
    public Iterator<ValidationQuery> iterator() {
        return getQueries();
    }
}

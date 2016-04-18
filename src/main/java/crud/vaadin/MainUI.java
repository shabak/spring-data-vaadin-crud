package crud.vaadin;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.UI;
import com.vaadin.ui.renderers.HtmlRenderer;
import crud.backend.Person;
import crud.backend.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.vaadin.viritin.GeneratedPropertyListContainer;
import org.vaadin.viritin.button.ConfirmButton;
import org.vaadin.viritin.button.MButton;
import org.vaadin.viritin.grid.MGrid;
import org.vaadin.viritin.label.RichText;
import org.vaadin.viritin.layouts.MHorizontalLayout;
import org.vaadin.viritin.layouts.MVerticalLayout;

/**
 *
 */
@Title("PhoneBook CRUD example")
@Theme("valo")
@SpringUI
public class MainUI extends UI {

    private static final long serialVersionUID = 1L;

    @Autowired
    PersonRepository repo;

    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String EMAIL = "email";
    private static final String BIRTHDAY = "birthDay";
    private static final String PHONE = "phone";
    private static final String BIRTHDAY_PHONE = "birthday_phone";
    String[] columns = {ID, NAME, EMAIL, BIRTHDAY_PHONE};

    private MGrid<Person> grid = new MGrid<>();

    private Button addNew = new MButton(FontAwesome.PLUS, this::add);
    private Button edit = new MButton(FontAwesome.PENCIL_SQUARE_O, this::edit);
    private Button delete = new ConfirmButton(FontAwesome.TRASH_O,
            "Are you sure you want to delete the entry?", this::remove);

    private BirthdayPhoneGenerator birthdayPhoneGenerator = new BirthdayPhoneGenerator();

    @Override
    protected void init(VaadinRequest request) {
        setContent(
                new MVerticalLayout(
                        new RichText().withMarkDownResource("/welcome.md"),
                        new MHorizontalLayout(addNew, edit, delete),
                        grid
                ).expand(grid)
        );

        GeneratedPropertyListContainer<Person> container = new GeneratedPropertyListContainer(Person.class, columns);
        container.addGeneratedProperty(BIRTHDAY_PHONE, birthdayPhoneGenerator);
        grid.setContainerDataSource(container);

        grid.getColumn(BIRTHDAY_PHONE).setHeaderCaption("Details");

        grid.addSelectionListener( event -> onSelectionChanged() );
        grid.setSizeFull();
        listEntities();
    }

    private void onSelectionChanged() {
        boolean oneRowSelected = grid.getSelectedRows().size() == 1;
        edit.setEnabled(oneRowSelected);
        delete.setEnabled(oneRowSelected);
    }

    static final int PAGESIZE = 45;

    private void listEntities() {
        grid.lazyLoadFrom(
                // entity fetching strategy
                (firstRow, asc, sortProperty) -> repo.findAllBy(
                        new PageRequest(
                                firstRow / PAGESIZE, 
                                PAGESIZE,
                                asc ? Sort.Direction.ASC : Sort.Direction.DESC,
                                // fall back to id as "natural order"
                                sortProperty == null ? "id" : sortProperty
                        )
                ),
                // count fetching strategy
                () -> (int) repo.count(),
                PAGESIZE
        );
        onSelectionChanged();

    }

    public void add(ClickEvent clickEvent) {
        edit(new Person());
    }

    public void edit(ClickEvent e) {
        edit(grid.getSelectedRow());
    }

    public void remove(ClickEvent e) {
        repo.delete(grid.getSelectedRow());
        listEntities();
    }

    protected void edit(final Person phoneBookEntry) {
        PhoneBookEntryForm phoneBookEntryForm = new PhoneBookEntryForm(phoneBookEntry);
        phoneBookEntryForm.openInModalPopup();
        phoneBookEntryForm.setSavedHandler(this::saveEntry);
        phoneBookEntryForm.setResetHandler(this::resetEntry);
    }

    public void saveEntry(Person entry) {
        repo.save(entry);
        listEntities();
        closeWindow();
    }

    public void resetEntry(Person entry) {
        listEntities();
        closeWindow();
    }

    protected void closeWindow() {
        getWindows().stream().forEach(w -> removeWindow(w));
    }

}

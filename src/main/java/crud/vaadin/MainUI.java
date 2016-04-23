package crud.vaadin;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.UI;
import crud.backend.Person;
import crud.backend.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.vaadin.viritin.LazyList;
import org.vaadin.viritin.grid.GeneratedPropertyListContainer;
import org.vaadin.viritin.button.ConfirmButton;
import org.vaadin.viritin.button.MButton;
import org.vaadin.viritin.grid.MGrid;
import org.vaadin.viritin.label.RichText;
import org.vaadin.viritin.layouts.MHorizontalLayout;
import org.vaadin.viritin.layouts.MVerticalLayout;

import java.text.SimpleDateFormat;
import java.util.Date;

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

    private MGrid<Person> fashionableApiGrid = new MGrid<>(Person.class)
            .withGeneratedColumn("details", new DetailsGenerator())
            .withGeneratedColumn("zodiac", String.class, person -> Zodiac.is(person.getBirthDay()))
            .withProperties("id", "name", "email", "zodiac", "details")
            .withFullWidth();

    private MGrid<Person> legacyApiGrid = new MGrid<>();

    private Button addNew = new MButton(FontAwesome.PLUS, this::add);
    private Button edit = new MButton(FontAwesome.PENCIL_SQUARE_O, this::edit);
    private Button delete = new ConfirmButton(FontAwesome.TRASH_O,
            "Are you sure you want to delete the entry?", this::remove);

    @Override
    protected void init(VaadinRequest request) {
        setContent(
                new MVerticalLayout(
                        new RichText().withMarkDownResource("/welcome.md"),
                        new MHorizontalLayout(addNew, edit, delete),
                        fashionableApiGrid,
                        legacyApiGrid
                )
        );

        GeneratedPropertyListContainer<Person> container = new
                GeneratedPropertyListContainer(Person.class, "id", "name", "email", "zodiac", "details");
        container.addGeneratedProperty("details", new DetailsGenerator());
        container.addGeneratedProperty("zodiac", String.class, person -> Zodiac.is(person.getBirthDay()));
        legacyApiGrid.setContainerDataSource(container);
        legacyApiGrid.getColumn("details").setHeaderCaption("Details");
        legacyApiGrid.setSizeFull();

        listEntities();
        fashionableApiGrid.addSelectionListener(event -> onSelectionChanged() );
    }

    private void onSelectionChanged() {
        boolean oneRowSelected = fashionableApiGrid.getSelectedRows().size() == 1;
        edit.setEnabled(oneRowSelected);
        delete.setEnabled(oneRowSelected);
    }

    static final int PAGESIZE = 45;

    private void listEntities() {
        fashionableApiGrid.lazyLoadFrom(
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
        legacyApiGrid.lazyLoadFrom(
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
        edit(fashionableApiGrid.getSelectedRow());
    }

    public void remove(ClickEvent e) {
        repo.delete(fashionableApiGrid.getSelectedRow());
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

    public static class Zodiac {

        private static SimpleDateFormat month = new SimpleDateFormat("MM");
        private static SimpleDateFormat day = new SimpleDateFormat("dd");

        public static String is(Date date) {
            if (date == null)
                return "";
            int M = Integer.parseInt(month.format(date));
            int D = Integer.parseInt(day.format(date));
            if ((M == 12 && D >= 22 && D <= 31) || (M ==  1 && D >= 1 && D <= 19))
                return "Capricorn";
            else if ((M ==  1 && D >= 20 && D <= 31) || (M ==  2 && D >= 1 && D <= 17))
                return "Aquarius";
            else if ((M ==  2 && D >= 18 && D <= 29) || (M ==  3 && D >= 1 && D <= 19))
                return "Pisces";
            else if ((M ==  3 && D >= 20 && D <= 31) || (M ==  4 && D >= 1 && D <= 19))
                return "Aries";
            else if ((M ==  4 && D >= 20 && D <= 30) || (M ==  5 && D >= 1 && D <= 20))
                return "Taurus";
            else if ((M ==  5 && D >= 21 && D <= 31) || (M ==  6 && D >= 1 && D <= 20))
                return "Gemini";
            else if ((M ==  6 && D >= 21 && D <= 30) || (M ==  7 && D >= 1 && D <= 22))
                return "Cancer";
            else if ((M ==  7 && D >= 23 && D <= 31) || (M ==  8 && D >= 1 && D <= 22))
                return "Leo";
            else if ((M ==  8 && D >= 23 && D <= 31) || (M ==  9 && D >= 1 && D <= 22))
                return "Virgo";
            else if ((M ==  9 && D >= 23 && D <= 30) || (M == 10 && D >= 1 && D <= 22))
                return "Libra";
            else if ((M == 10 && D >= 23 && D <= 31) || (M == 11 && D >= 1 && D <= 21))
                return "Scorpio";
            else if ((M == 11 && D >= 22 && D <= 30) || (M == 12 && D >= 1 && D <= 21))
                return "Sagittarius";
            else
                return "";

        }

    }
}

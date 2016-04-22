/*
 * Copyright (c) 2016, i-Free. All Rights Reserved.
 * Use is subject to license terms.
 */

package crud.vaadin;

import com.vaadin.data.Item;
import com.vaadin.data.util.PropertyValueGenerator;
import crud.backend.Person;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * StatusGenerator - TODO: description
 *
 * @author Nikolay Shabak (nikolay)
 * @since 12.04.2016
 */
public class DetailsGenerator extends PropertyValueGenerator<String> {

    SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
    @Override
    public String getValue(Item item, Object itemId, Object propertyId) {
        Person p = (Person)itemId;
        Date birthDay = p.getBirthDay();
        StringBuilder displayValue = new StringBuilder();
        if (birthDay != null) {
            displayValue.append("BD: ");
            displayValue.append(dateFormat.format(birthDay));
        }
        String phoneNumber = p.getPhoneNumber();
        if (phoneNumber != null) {
            if (displayValue.length() > 0) {
                displayValue.append("; ");
            }
            displayValue.append(" Phone: ");
            displayValue.append(phoneNumber);
        }
        return displayValue.toString();
    }

    @Override
    public Class<String> getType() {
        return String.class;
    }
}

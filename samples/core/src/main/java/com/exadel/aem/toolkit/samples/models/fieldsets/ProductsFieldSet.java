package com.exadel.aem.toolkit.samples.models.fieldsets;

import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnRef;
import com.exadel.aem.toolkit.api.annotations.widgets.Checkbox;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.samples.utils.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class ProductsFieldSet {

    @DialogField(label = "Milk")
    @Checkbox
    @DependsOnRef(name = "checkbox")
    @ValueMapValue
    private boolean milkChosen;

    @DialogField(label = "Cheese")
    @Checkbox
    @DependsOnRef(name = "checkbox")
    @ValueMapValue
    private boolean cheeseChosen;

    public boolean isMilkChosen() {
        return milkChosen;
    }

    public boolean isCheeseChosen() {
        return cheeseChosen;
    }

    private String getMilk() {
        return milkChosen ? "milk" : StringUtils.EMPTY;
    }

    private String getCheese() {
        return cheeseChosen ? "cheese" : StringUtils.EMPTY;
    }

    public String getProducts() {
        return ListUtils.joinNonBlank(ListUtils.COMMA_SPACE_DELIMITER, getMilk(), getCheese());
    }
}

package com.exadel.aem.toolkit.samples.models;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.exadel.aem.toolkit.api.annotations.container.PlaceOnTab;
import com.exadel.aem.toolkit.api.annotations.container.Tab;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.Password;
import com.exadel.aem.toolkit.api.annotations.widgets.button.Button;
import com.exadel.aem.toolkit.api.annotations.widgets.button.ButtonType;
import com.exadel.aem.toolkit.api.annotations.widgets.textarea.TextArea;
import com.exadel.aem.toolkit.samples.constants.GroupConstants;
import com.exadel.aem.toolkit.samples.constants.PathConstants;

@Dialog(
        name = "content/secret-info-component",
        title = "Secret Info",
        description = "Secret info about the warrior",
        resourceSuperType = PathConstants.FOUNDATION_PARBASE_PATH,
        componentGroup = GroupConstants.COMPONENT_GROUP,
        tabs = {
                @Tab(title = SecretInfoComponent.TAB_PASSWORD),
                @Tab(title = SecretInfoComponent.TAB_SECRET),
        }
)
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class SecretInfoComponent {

    static final String TAB_PASSWORD = "Password";
    static final String TAB_SECRET = "Secret";

    private static final String DEFAULT_SECRET = "This warrior is clear";

    @DialogField(
            description = "Enter a password to unlock secret information about the warrior",
            label = "Enter secret password"
    )
    @Password(retype = "confirmPassword")
    @PlaceOnTab(SecretInfoComponent.TAB_PASSWORD)
    @ValueMapValue
    private String password;

    @DialogField(label = "Confirm your password")
    @Password
    @PlaceOnTab(SecretInfoComponent.TAB_PASSWORD)
    @ValueMapValue
    private String confirmPassword;

    @DialogField(
            label = "Secret info",
            description = "Enter secret information about your warrior"
    )
    @TextArea(autofocus = true, maxlength = 200)
    @PlaceOnTab(SecretInfoComponent.TAB_SECRET)
    @ValueMapValue
    private String info;

    @DialogField
    @Button(
            type = ButtonType.SUBMIT,
            text = "save",
            icon = "edit",
            command = "shift+s",
            block = true
    )
    @PlaceOnTab(SecretInfoComponent.TAB_SECRET)
    private String button;

    public String getPassword() {
        return password;
    }

    public String getInfo() {
        return StringUtils.defaultIfBlank(info, DEFAULT_SECRET);
    }
}

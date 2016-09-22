/*
 * #%L
 * GwtMaterial
 * %%
 * Copyright (C) 2015 - 2016 GwtMaterialDesign
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package gwt.material.design.addins.client.timepicker;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.*;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.user.client.DOM;
import gwt.material.design.addins.client.MaterialAddins;
import gwt.material.design.addins.client.base.constants.AddinsCssName;
import gwt.material.design.addins.client.timepicker.js.JsTimePickerOptions;
import gwt.material.design.client.MaterialDesignBase;
import gwt.material.design.client.base.AbstractValueWidget;
import gwt.material.design.client.base.HasIcon;
import gwt.material.design.client.base.HasOrientation;
import gwt.material.design.client.base.HasPlaceholder;
import gwt.material.design.client.base.mixin.ErrorMixin;
import gwt.material.design.client.base.mixin.ToggleStyleMixin;
import gwt.material.design.client.constants.*;
import gwt.material.design.client.ui.MaterialIcon;
import gwt.material.design.client.ui.MaterialInput;
import gwt.material.design.client.ui.MaterialLabel;
import gwt.material.design.client.ui.MaterialPanel;
import gwt.material.design.client.ui.html.Label;

import java.util.Date;

import static gwt.material.design.addins.client.timepicker.js.JsTimePicker.$;

//@formatter:off

/**
 * Material Time Picker - provide a simple way to select a single value from a pre-determined set.
 *
 * <h3>XML Namespace Declaration</h3>
 * <pre>
 * {@code
 * xmlns:ma='urn:import:gwt.material.design.addins.client'
 * }
 * </pre>
 *
 * <h3>UiBinder Usage:</h3>
 * <pre>
 * {@code <ma:timepicker.MaterialTimePicker placeholder="Time Arrival" />}
 * </pre>
 * @see <a href="http://gwtmaterialdesign.github.io/gwt-material-demo/#timepickers">Material Pickers</a>
 * @author kevzlou7979
 * @author Ben Dol
 */
//@formatter:on
public class MaterialTimePicker extends AbstractValueWidget<Date> implements HasPlaceholder, HasOrientation,
        HasCloseHandlers<Date>, HasOpenHandlers<Date>, HasIcon {

    static {
        if(MaterialAddins.isDebug()) {
            MaterialDesignBase.injectDebugJs(MaterialTimePickerDebugClientBundle.INSTANCE.timepickerJsDebug());
            MaterialDesignBase.injectCss(MaterialTimePickerDebugClientBundle.INSTANCE.timepickerCssDebug());
        } else {
            MaterialDesignBase.injectJs(MaterialTimePickerClientBundle.INSTANCE.timepickerJs());
            MaterialDesignBase.injectCss(MaterialTimePickerClientBundle.INSTANCE.timepickerCss());
        }
    }

    /** Wraps the actual input. */
    private MaterialPanel panel = new MaterialPanel();

    /** The input element for the time picker. */
    private MaterialInput input = new MaterialInput();

    /** Label to display errors messages. */
    private MaterialLabel lblError = new MaterialLabel();

    /** The current value held by the time picker. */
    private Date time;

    private Label label = new Label();

    private MaterialIcon icon = new MaterialIcon();

    private ToggleStyleMixin<MaterialInput> validMixin = new ToggleStyleMixin<>(this.input, CssName.VALID);

    private final ErrorMixin<AbstractValueWidget, MaterialLabel> errorMixin = new ErrorMixin<>(this, this.lblError, this.input);

    private String uniqueId;
    private String placeholder;
    private boolean autoClose;
    private boolean hour24;
    private Orientation orientation = Orientation.PORTRAIT;

    public MaterialTimePicker() {
        super(Document.get().createElement("div"), AddinsCssName.TIMEPICKER, CssName.INPUT_FIELD);
    }

    public MaterialTimePicker(String placeholder) {
        this();
        setPlaceholder(placeholder);
    }

    public MaterialTimePicker(String placeholder, Date value) {
        this(placeholder);
        setValue(value);
    }

    @Override
    protected void onLoad() {
        super.onLoad();

        uniqueId = DOM.createUniqueId();
        input.setType(InputType.TEXT);
        panel.add(input);
        panel.add(label);
        panel.add(lblError);
        add(panel);
        input.getElement().setAttribute("type", "text");
        initialize();
    }

    @Override
    protected void onUnload() {
        super.onUnload();

        $(input.getElement()).lolliclock("remove");
    }

    /**
     * Side effects:
     * <ul>
     *   <li>Resets the time to <i>now<i></li>
     *   <li>Clears errors/success message</li>
     * </ul>
     */
    public void reset() {
        setValue(new Date());
        clearErrorOrSuccess();
    }

    public boolean isAutoClose() {
        return this.autoClose;
    }

    public void setAutoClose(boolean autoClose) {
        this.autoClose = autoClose;
    }

    /**
     * False (default) change to 24 hours system.
     *
     * @return <code>false</code> in case 12 hours mode is set;
     *         <code>true</code> otherwise.
     */
    public boolean isHour24() {
        return this.hour24;
    }

    /**
     * Set the time to 24 hour mode.
     */
    public void setHour24(boolean hour24) {
        this.hour24 = hour24;
    }

    /**
     * @return The placeholder text.
     */
    @Override
    public String getPlaceholder() {
        return this.placeholder;
    }

    /**
     * @param placeholder The placeholder text to set.
     */
    @Override
    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        label.setText(placeholder);
    }

    /**
     * @return The orientation.
     */
    @Override
    public Orientation getOrientation() {
        return this.orientation;
    }

    /**
     * @param orientation The orientation to set: Can be Horizontal or Vertical.
     */
    @Override
    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

    protected void initialize() {
        JsTimePickerOptions options = new JsTimePickerOptions();
        options.autoclose = isAutoClose();
        options.orientation = getOrientation().getCssName();
        options.hour24 = isHour24();
        options.uniqueId = getUniqueId();
        options.beforeShow = this::beforeShow;
        options.afterShow = this::afterShow;
        options.afterHide = this::afterHide;
        $(input.getElement()).lolliclock(options);
        $(input.getElement()).blur();
    }

    /**
     * Called after the lolliclock event <code>afterShow</code>.
     */
    protected void beforeShow() {
        input.getElement().blur();

        // Add class 'valid' for visual feedback.
        validMixin.setOn(true);
    }

    /**
     * Called after the lolliclock event <code>afterShow</code>.
     */
    protected void afterShow() {
        OpenEvent.fire(this, this.time);
    }

    /**
     * Called after the lolliclock event <code>afterHide</code>.
     */
    protected void afterHide() {
        String timeString = getTime();
        Date parsedDate = null;

        if(timeString != null && !timeString.equals("")) {
            try {
                if(this.hour24) {
                    parsedDate = DateTimeFormat.getFormat("HH:mm").parse(timeString);
                } else {
                    parsedDate = DateTimeFormat.getFormat("hh:mm aa").parse(timeString);
                }
            } catch(Exception e) {
                // Silently catch parse errors
            }
        }

        setValue(parsedDate);

        // Remove class 'valid' after hide.
        validMixin.setOn(false);

        CloseEvent.fire(this, this.time);
    }

    protected String getTime() {
        return $(input.getElement()).val().toString();
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.input.setEnabled(enabled);
    }

    @Override
    public HandlerRegistration addCloseHandler(final CloseHandler<Date> handler) {
        return addHandler(handler, CloseEvent.getType());
    }

    @Override
    public HandlerRegistration addOpenHandler(final OpenHandler<Date> handler) {
        return addHandler(handler, OpenEvent.getType());
    }

    @Override
    public void clear() {
        time = null;
        $(input.getElement()).val("");
    }

    @Override
    public Date getValue() {
        return time;
    }

    @Override
    public void setValue(Date time, boolean fireEvents) {
        if(this.time != null && this.time.equals(time)) {
            return;
        }
        this.time = time;

        $(input.getElement()).val(DateTimeFormat.getFormat(hour24 ? "HH:mm" : "hh:mm aa").format(time));

        super.setValue(time, fireEvents);
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    @Override
    public MaterialIcon getIcon() {
        return icon;
    }

    @Override
    public void setIconType(IconType iconType) {
        icon.setIconType(iconType);
        icon.setIconPrefix(true);
        lblError.setPaddingLeft(44);
        panel.insert(icon, 0);
    }

    @Override
    public void setIconPosition(IconPosition position) {
        icon.setIconPosition(position);
    }

    @Override
    public void setIconSize(IconSize size) {
        icon.setIconSize(size);
    }

    @Override
    public void setIconFontSize(double size, Style.Unit unit) {
        icon.setIconFontSize(size, unit);
    }

    @Override
    public void setIconColor(Color iconColor) {
        icon.setIconColor(iconColor);
    }

    @Override
    public void setIconPrefix(boolean prefix) {
        icon.setIconPrefix(prefix);
    }

    @Override
    public boolean isIconPrefix() {
        return icon.isIconPrefix();
    }

    @Override
    public ErrorMixin<AbstractValueWidget, MaterialLabel> getErrorMixin() {
        return errorMixin;
    }
}

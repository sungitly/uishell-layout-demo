package com.sunsuz.adf.uishell.layout.ui.beans;


import static com.sunsuz.adf.uishell.layout.ui.context.ContextNames.DEPARTMENT_NAME;
import com.sunsuz.adf.uishell.layout.ui.tab.TabContext;

import java.io.Serializable;

import javax.faces.event.ActionEvent;

import javax.faces.event.ValueChangeEvent;

import oracle.adf.share.ADFContext;
import oracle.adf.view.rich.component.rich.nav.RichCommandNavigationItem;
import oracle.adf.view.rich.context.AdfFacesContext;

import org.apache.commons.lang.StringUtils;


public class NavigationBean implements Serializable {

    private RichCommandNavigationItem navigationPanelHeader;

    public void onDepartmentSelected(ActionEvent actionEvent) {
        String departmentName = (String)actionEvent.getComponent().getAttributes().get(DEPARTMENT_NAME.name());
        if (!StringUtils.isEmpty(departmentName)) {
            ADFContext.getCurrent().getSessionScope().put(DEPARTMENT_NAME.name(), departmentName);
            AdfFacesContext.getCurrentInstance().addPartialTarget(getNavigationPanelHeader());
        }

        TabContext.getCurrentInstance().addTab(departmentName,
                                               "/WEB-INF/com/sunsuz/adf/uishell/layout/ui/dept-demo-tf.xml#dept-demo-tf");
    }

    public void setNavigationPanelHeader(RichCommandNavigationItem navigationPanelHeader) {
        this.navigationPanelHeader = navigationPanelHeader;
    }

    public RichCommandNavigationItem getNavigationPanelHeader() {
        return navigationPanelHeader;
    }

    public void onDepartmentInfoChange(ValueChangeEvent valueChangeEvent) {
        TabContext.getCurrentInstance().markCurrentTabDirty(true);
    }
}

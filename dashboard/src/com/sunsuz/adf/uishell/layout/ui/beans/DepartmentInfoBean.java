package com.sunsuz.adf.uishell.layout.ui.beans;


import com.sunsuz.adf.uishell.layout.ui.tab.TabContext;

import javax.faces.event.ValueChangeEvent;

public class DepartmentInfoBean {
    public DepartmentInfoBean() {
    }

    public void onDepartmentInfoChange(ValueChangeEvent valueChangeEvent) {
        TabContext.getCurrentInstance().markCurrentTabDirty(true);
    }
}

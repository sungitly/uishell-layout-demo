package com.sunsuz.adf.uishell.layout.ui.beans;

import com.sunsuz.adf.uishell.layout.ui.tab.TabContext;

import oracle.adf.controller.TaskFlowId;

public class DepartmentRegionSwitcher {
    private String taskFlowId = "/WEB-INF/com/sunsuz/adf/uishell/layout/ui/dept-info-tf.xml#dept-info-tf";

    public DepartmentRegionSwitcher() {
    }

    public TaskFlowId getDynamicTaskFlowId() {
        return TaskFlowId.parse(taskFlowId);
    }

    public String deptinfotf() {
        taskFlowId = "/WEB-INF/com/sunsuz/adf/uishell/layout/ui/dept-info-tf.xml#dept-info-tf";
        return null;
    }

    public String deptdetailstf() {
        TabContext.getCurrentInstance();
        taskFlowId = "/WEB-INF/com/sunsuz/adf/uishell/layout/ui/dept-details-tf.xml#dept-details-tf";
        return null;
    }

    public boolean isDeptInfoTabActive() {
        return "/WEB-INF/com/sunsuz/adf/uishell/layout/ui/dept-info-tf.xml#dept-info-tf".equals(taskFlowId);
    }


    public boolean isDeptDetailsTabActive() {
        return "/WEB-INF/com/sunsuz/adf/uishell/layout/ui/dept-details-tf.xml#dept-details-tf".equals(taskFlowId);
    }
}

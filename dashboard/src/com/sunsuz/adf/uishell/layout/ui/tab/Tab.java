package com.sunsuz.adf.uishell.layout.ui.tab;

import java.io.Serializable;

import java.util.List;

import java.util.Map;

import oracle.adf.controller.TaskFlowId;
import oracle.adf.model.binding.DCBindingContainer;
import oracle.adf.model.BindingContext;
import oracle.adf.model.binding.DCExecutableBindingDef;

import oracle.binding.BindingContainer;

import static org.apache.commons.lang.StringUtils.EMPTY;

public class Tab implements Serializable {

    private static final String TAB_TEMPLATE_PAGE_DEF =
        "com.sunsuz.adf.uishell.layout.ui.templates.tabLayoutTemplateDefPageDef";

    private static final TaskFlowId BLANK =
        TaskFlowId.parse("/WEB-INF/com/sunsuz/adf/uishell/layout/ui/blank.xml#blank");

    // The prefix of task flow id which is defined in tabLayoutTemplateDefPageDef.xml
    private static final String PRE_DEF_TASK_FLOW_ID_PREFIX = "r";

    private String name;
    private final int index;
    private TaskFlowId taskflowId;

    // Used in the task flow associated with current tab.
    private Map<String, Object> parameters;

    // Indicate if the tab is rendered.
    private boolean active = false;
    // Indicate if the tab is modified.
    private boolean dirty = false;

    public Tab(int index) {
        this.index = index;
        this.taskflowId = BLANK;
    }

    public void activate(String name, String taskflowId, Map<String, Object> params) {
        setName(name);
        setActive(true);
        setTaskflowId(TaskFlowId.parse(taskflowId));
        setParameters(params);
    }

    public void deactive() {
        setName(EMPTY);
        setActive(false);
        setTaskflowId(BLANK);
        setParameters(null);
    }

    /**
     * This will return the binding container associated with the current tab. And this should return a non-null value
     * unless user invokes it from a execution context whose bindingContainer does't have a reference to dynamic tab shell page template.
     * @return binding container associated with current tab
     */
    public DCBindingContainer getBinding() {

        DCBindingContainer bindingContainer =
            (DCBindingContainer)getCurrentBindingContainer().get(PRE_DEF_TASK_FLOW_ID_PREFIX + index);
        //If tab.getBinding() invoked from actual template then BindingContext.getCurrent().getCurrentBindingsEntry() will return template's pagedefinition file
        //but if it is invoked from a consuming page then BindingContext.getCurrent().getCurrentBindingsEntry() will refer to the consuming page's pagedef file
        //Then we need to look at all executable bindings which are refering to "oracle.ui.pattern.dynamicShell.model.dynamicTabShellDefinition"
        //and use that to retrieve the tab binding.
        if (bindingContainer == null) {
            List execBindings = ((DCBindingContainer)getCurrentBindingContainer()).getExecutableBindings();
            if (execBindings != null) {
                for (Object binding : execBindings) {
                    DCExecutableBindingDef execDef =
                        ((DCExecutableBindingDef)((DCBindingContainer)binding).getExecutableDef());
                    if (execDef != null && TAB_TEMPLATE_PAGE_DEF.equals(execDef.getFullName())) {
                        return (DCBindingContainer)((DCBindingContainer)binding).get(PRE_DEF_TASK_FLOW_ID_PREFIX +
                                                                                     index);
                    }
                }
            }
        }
        return bindingContainer;
    }

    private BindingContainer getCurrentBindingContainer() {
        return BindingContext.getCurrent().getCurrentBindingsEntry();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public int getIndex() {
        return this.index;
    }

    public void setTaskflowId(TaskFlowId id) {
        taskflowId = id;
    }

    public TaskFlowId getTaskflowId() {
        return taskflowId;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setActive(boolean active) {
        this.active = active;

        if (!isActive()) {
            setDirty(false);
        }
    }

    public boolean isActive() {
        return active;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean isDirty() {
        return dirty;
    }
}


package com.sunsuz.adf.uishell.layout.ui.tab;


import com.sunsuz.adf.uishell.layout.ui.tab.exception.TabDirtyException;
import com.sunsuz.adf.uishell.layout.ui.tab.exception.TabOverflowException;

import java.io.Serializable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import oracle.adf.controller.ControllerContext;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.layout.RichPanelStretchLayout;
import oracle.adf.view.rich.component.rich.nav.RichNavigationPane;
import oracle.adf.view.rich.context.AdfFacesContext;
import oracle.adf.view.rich.event.DialogEvent;
import oracle.adf.view.rich.event.ItemEvent;

import org.apache.myfaces.trinidad.context.RequestContext;
import org.apache.myfaces.trinidad.model.ChildPropertyMenuModel;
import org.apache.myfaces.trinidad.model.MenuModel;
import org.apache.myfaces.trinidad.render.ExtendedRenderKitService;
import org.apache.myfaces.trinidad.util.Service;


public final class TabContext implements Serializable {

    private static final String TAB_INDEX_PARAM = "tabIndex";
    private final TabsManager tabManager;

    private static final boolean USE_SESSION_TRACKED_TABS = true;

    private transient RichPanelStretchLayout contentArea;
    private transient RichNavigationPane tabsNavigationPane;
    private transient RichPopup tooManyTabsPopup;
    private transient RichPopup tabDirtyPopup;

    // The name of tabContext managed bean
    private static final String TAB_CONTEXT_KEY = "tabContext";

    public TabContext() {
        if (USE_SESSION_TRACKED_TABS) {
            String viewId = ControllerContext.getInstance().getCurrentRootViewPort().getViewId();
            String currentViewKey = String.format("__tc_%s", viewId);

            TabsManager tabTracker =
                (TabsManager)RequestContext.getCurrentInstance().getPageFlowScope().get(currentViewKey);
            if (tabTracker == null) {
                tabTracker = new TabsManager();
                RequestContext.getCurrentInstance().getPageFlowScope().put(currentViewKey, tabTracker);
            }
            this.tabManager = tabTracker;
        } else {
            tabManager = new TabsManager();
        }
    }

    public static TabContext getCurrentInstance() {
        AdfFacesContext adfFacesContext = AdfFacesContext.getCurrentInstance();

        TabContext tabContext = (TabContext)adfFacesContext.getViewScope().get(TAB_CONTEXT_KEY);

        if (tabContext == null)
            tabContext = (TabContext)adfFacesContext.getPageFlowScope().get(TAB_CONTEXT_KEY);

        return tabContext;
    }

    public void addTab(String name, String taskflowId) {
        this.addTab(name, taskflowId, null);
    }

    public void addTab(String name, String taskflowId, Map<String, Object> parameters) {
        try {
            tabManager.addTab(name, taskflowId, parameters);
        } catch (TabOverflowException e) {
            showDialog(getTooManyTabsPopup());
        }

        refreshTabContent();
    }

    public void addOrSelectTab(String name, String taskflowId) throws TabOverflowException {
        this.addOrSelectTab(name, taskflowId, null);
    }

    public void addOrSelectTab(String name, String taskflowId,
                               Map<String, Object> parameters) throws TabOverflowException {
        Tab existingActiveTab = findActiveTab(taskflowId);
        if (existingActiveTab != null) {
            selectTab(existingActiveTab);
        } else {
            addTab(name, taskflowId, parameters);
        }
    }

    private Tab findActiveTab(String taskflowId) {
        List<Tab> tabs = getTabs();

        for (Tab tab : tabs) {
            if (tab == null || !tab.isActive())
                continue;

            if (tab.getTaskflowId().getFullyQualifiedName().equals(taskflowId))
                return tab;
        }

        return null;
    }

    private void selectTab(Tab tab) {
        if (tab == null) {
            return;
        }

        setSelectedTabIndex(tab.getIndex());
    }

    public void removeCurrentTab() {
        removeTab(getSelectedTabIndex());
    }

    public void removeTab(int index) {
        removeTab(index, false);
    }

    private void removeTab(int index, boolean force) {

        try {
            tabManager.removeTab(index, force);
        } catch (TabDirtyException e) {
            showDialog(getTabDirtyPopup());
        }

        refreshTabContent();
    }

    public void markCurrentTabDirty(boolean isDirty) {
        markTabDirty(getSelectedTabIndex(), isDirty);
    }

    public void markTabDirty(int index, boolean isDirty) {
        Tab tab = getTabs().get(index);
        tab.setDirty(isDirty);
        refreshTabContent();
    }

    public boolean isTagSetDirty() {
        for (Tab t : getTabs()) {
            if (t.isActive() && t.isDirty())
                return true;
        }

        return false;
    }

    public boolean isCurrentTabDirty() {
        int index = getSelectedTabIndex();
        if (index == -1)
            return false;

        return getTabs().get(index).isDirty();
    }

    public MenuModel getTabMenuModel() {
        ChildPropertyMenuModel menuModel =
            new ChildPropertyMenuModel(getTabs(), "children", Collections.singletonList(getSelectedTabIndex()));
        return menuModel;
    }


    public void tabActivatedEvent(ActionEvent action) {
        Object tabIndex = action.getComponent().getAttributes().get(TAB_INDEX_PARAM);
        setSelectedTabIndex((Integer)tabIndex);
    }

    public void tabRemovedEvent(ItemEvent action) {
        if (ItemEvent.Type.remove.equals(action.getType())) {
            Object tabIndex = action.getComponent().getAttributes().get(TAB_INDEX_PARAM);
            if (tabIndex instanceof Integer) {
                removeTab((Integer)tabIndex);
            }
        }
    }

    public void handleDirtyTabDialog(DialogEvent ev) {
        if (ev.getOutcome().equals(DialogEvent.Outcome.yes)) {
            removeTab(getSelectedTabIndex(), true);
        }
    }

    private void refreshTabContent() {
        AdfFacesContext.getCurrentInstance().addPartialTarget(getTabsNavigationPane());
        AdfFacesContext.getCurrentInstance().addPartialTarget(getContentArea());
    }

    private void showDialog(RichPopup popup) {
        FacesContext context = FacesContext.getCurrentInstance();
        StringBuilder toSend = new StringBuilder();
        toSend.append("var popup = AdfPage.PAGE.findComponent('"). // NOTRANS
            append(popup.getClientId(context)).append("'); "). // NOTRANS
            append("if (!popup.isPopupVisible()) { "). // NOTRANS
            append("var hints = {}; "). // NOTRANS
            append("popup.show(hints);}"); // NOTRANS
        ExtendedRenderKitService erks = Service.getService(context.getRenderKit(), ExtendedRenderKitService.class);
        erks.addScript(context, toSend.toString());
    }

    public int getSelectedTabIndex() {
        return tabManager.getSelectedTabIndex();
    }

    public void setSelectedTabIndex(int index) {
        tabManager.setSelectedTabIndex(index);
        refreshTabContent();
    }

    public List<Tab> getTabs() {
        return tabManager.getTabs();
    }

    public void setTabsNavigationPane(RichNavigationPane tabsNavigationPane) {
        this.tabsNavigationPane = tabsNavigationPane;
    }

    public RichNavigationPane getTabsNavigationPane() {
        return tabsNavigationPane;
    }

    public void setTooManyTabsPopup(RichPopup tooManyTabsPopup) {
        this.tooManyTabsPopup = tooManyTabsPopup;
    }

    public RichPopup getTooManyTabsPopup() {
        return tooManyTabsPopup;
    }

    public void setTabDirtyPopup(RichPopup tabDirtyPopup) {
        this.tabDirtyPopup = tabDirtyPopup;
    }

    public RichPopup getTabDirtyPopup() {
        return tabDirtyPopup;
    }

    public void setContentArea(RichPanelStretchLayout contentArea) {
        this.contentArea = contentArea;
    }

    public RichPanelStretchLayout getContentArea() {
        return contentArea;
    }
}


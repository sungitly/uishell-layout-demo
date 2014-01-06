package com.sunsuz.adf.uishell.layout.ui.tab;


import com.sunsuz.adf.uishell.layout.ui.tab.exception.TabDirtyException;
import com.sunsuz.adf.uishell.layout.ui.tab.exception.TabOverflowException;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class TabsManager implements Serializable {
    // We have 15 regions defined in Tab.TAB_TEMPLATE_PAGE_DEF so we have to limit the number of active tabs to 15 here.
    private static final int MAX_TABS_COUNT = 15;
    private static final int FIRST_TAB_INDEX = 0;
    private static final int LAST_TAB_INDEX = MAX_TABS_COUNT - 1;

    private final ArrayList<Tab> tabs;

    private int selectedTabIndex = -1;
    private int activeTabsCount = 0;
    private int nextAvailableTabIndex = 0;


    public TabsManager() {
        this.tabs = new ArrayList<Tab>(MAX_TABS_COUNT);

        for (int i = 0; i < MAX_TABS_COUNT; i++) {
            this.tabs.add(new Tab(i));
        }
    }

    public void addTab(String name, String taskflowId, Map<String, Object> parameters) throws TabOverflowException {
        if (getActiveTabsCount() == MAX_TABS_COUNT) {
            throw new TabOverflowException();
        }

        Tab tab = findNextAvailableTab();
        tab.activate(name, taskflowId, parameters);

        setActiveTabsCount(getActiveTabsCount() + 1);
        setNextAvailableTabIndex(getNextAvailableTabIndex() + 1);

        setSelectedTabIndex(tab.getIndex());
    }

    public void removeTab(int index, boolean force) throws TabDirtyException {
        Tab tab = getTabs().get(index);

        if (tab.isDirty() && !force) {
            throw new TabDirtyException();
        }

        tab.deactive();
        setActiveTabsCount(getActiveTabsCount() - 1);

        if (getSelectedTabIndex() == index) {
            selectNextActiveTab(index);
        }
    }

    private void selectNextActiveTab(int index) {
        setSelectedTabIndex(-1);

        if (getActiveTabsCount() > 0) {
            int nextTabIndex = getNextTabIndex(index);
            while (nextTabIndex != index) {
                Tab nextTab = getTabs().get(nextTabIndex);
                if (nextTab.isActive()) {
                    setSelectedTabIndex(nextTabIndex);
                    return;
                }
                // Move to next Tab.
                nextTabIndex = getNextTabIndex(nextTabIndex);
            }
        }
    }

    private int getNextTabIndex(int index) {
        if (index == LAST_TAB_INDEX) {
            return FIRST_TAB_INDEX;
        } else {
            return index + 1;
        }
    }

    private Tab findNextAvailableTab() {
        // Since we don't reconsolidate the tabs when a tab is removed, we do it once we hit the MAX_TABS_COUNT limit.
        // We will go through the tabs and move all inactive ones to the right end of the list.
        if (getNextAvailableTabIndex() == MAX_TABS_COUNT) {
            for (int i = 0; i < MAX_TABS_COUNT; i++) {
                Tab tab = getTabs().get(i);

                if (!tab.isActive()) {
                    int j = i + 1;
                    Tab toSwap = null;
                    while (j < MAX_TABS_COUNT) {
                        Tab testTab = getTabs().get(j++);
                        if (testTab.isActive()) {
                            toSwap = testTab;
                            break;
                        }
                    }

                    // nothing else to do
                    if (toSwap == null)
                        break;

                    tab.activate(toSwap.getName(), toSwap.getTaskflowId().getFullyQualifiedName(),
                                 toSwap.getParameters());
                    toSwap.deactive();
                }
            }

            setNextAvailableTabIndex(getActiveTabsCount());
        }

        return tabs.get(getNextAvailableTabIndex());
    }

    public List<Tab> getTabs() {
        return tabs;
    }

    public int getSelectedTabIndex() {
        return selectedTabIndex;
    }

    public void setSelectedTabIndex(int index) {
        selectedTabIndex = index;
    }

    private void setActiveTabsCount(int activeTabsCount) {
        this.activeTabsCount = activeTabsCount;
    }

    private int getActiveTabsCount() {
        return activeTabsCount;
    }

    private void setNextAvailableTabIndex(int nextAvailableTabIndex) {
        this.nextAvailableTabIndex = nextAvailableTabIndex;
    }

    private int getNextAvailableTabIndex() {
        return nextAvailableTabIndex;
    }
}

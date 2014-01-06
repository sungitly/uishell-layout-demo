package com.sunsuz.adf.uishell.layout.ui.tab.exception;

public class TabDirtyException extends Exception {
    public TabDirtyException(Throwable throwable) {
        super(throwable);
    }

    public TabDirtyException(String string, Throwable throwable) {
        super(string, throwable);
    }

    public TabDirtyException(String string) {
        super(string);
    }

    public TabDirtyException() {
        super();
    }
}

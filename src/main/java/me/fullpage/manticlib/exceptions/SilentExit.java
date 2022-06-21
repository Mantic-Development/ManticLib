package me.fullpage.manticlib.exceptions;

import java.io.PrintStream;
import java.io.PrintWriter;

public class SilentExit extends Exception {

    @Override
    public String toString() {
        return "";
    }

    @Override
    public String getLocalizedMessage() {
        return "";
    }

    @Override
    public String getMessage() {
        return "";
    }

    @Override
    public void printStackTrace() {

    }

    @Override
    public void printStackTrace(PrintStream s) {

    }

    @Override
    public void printStackTrace(PrintWriter s) {

    }

    @Override
    public StackTraceElement[] getStackTrace() {
        return new StackTraceElement[0];
    }

    @Override
    public synchronized Throwable getCause() {
        return null;
    }

    @Override
    public void setStackTrace(StackTraceElement[] stackTrace) {

    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return null;
    }

    @Override
    public synchronized Throwable initCause(Throwable cause) {
        return null;
    }

}

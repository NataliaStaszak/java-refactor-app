package com.sap.refactoring.users.exception;

public class NoRoleException extends RuntimeException {
    public NoRoleException() {
        super("User must have at least one role.");
    }
}

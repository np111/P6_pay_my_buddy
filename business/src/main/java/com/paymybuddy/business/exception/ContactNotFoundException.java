package com.paymybuddy.business.exception;

/**
 * Thrown when a targeted contact does not exist (whether if the contact user not exists or the contact user exists
 * but is not in the contact list).
 * <p>
 * Prefer to return null/empty in a get/search context.
 */
public class ContactNotFoundException extends FastRuntimeException {
}

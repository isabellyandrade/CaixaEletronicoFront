package org.example.patterns.chainOfResponsibility;

import org.example.controller.CaixaEletronicoController.LoginRequest;

public interface ValidationHandler {
    void setNext(ValidationHandler handler);
    ValidationHandler getNext();
    ValidationResult handle(LoginRequest request);
    String getErrorMessage();
}
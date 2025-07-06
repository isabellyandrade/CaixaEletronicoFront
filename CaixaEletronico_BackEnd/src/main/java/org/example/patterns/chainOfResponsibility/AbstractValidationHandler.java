package org.example.patterns.chainOfResponsibility;

import org.example.controller.CaixaEletronicoController.LoginRequest;

public abstract class AbstractValidationHandler implements ValidationHandler {
    private ValidationHandler nextHandler;
    protected String errorMessage;

    @Override
    public void setNext(ValidationHandler handler) {
        this.nextHandler = handler;
    }

    @Override
    public ValidationHandler getNext() {
        return this.nextHandler;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    protected abstract boolean isValid(LoginRequest request);

    @Override
    public ValidationResult handle(LoginRequest request) {
        if (isValid(request)) {
            if (nextHandler != null) {
                // Se o atual passou, passa para o próximo e retorna o resultado dele
                return nextHandler.handle(request);
            }
            // Se não há próximo e o atual passou, significa sucesso na cadeia
            return ValidationResult.success();
        } else {
            // Se o atual falhou, retorna a falha com a mensagem de erro
            return ValidationResult.failure(this.errorMessage);
        }
    }
}

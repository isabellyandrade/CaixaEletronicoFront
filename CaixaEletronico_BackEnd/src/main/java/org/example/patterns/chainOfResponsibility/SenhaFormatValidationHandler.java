package org.example.patterns.chainOfResponsibility;

import org.example.controller.CaixaEletronicoController.LoginRequest;

public class SenhaFormatValidationHandler extends AbstractValidationHandler {

    @Override
    protected boolean isValid(LoginRequest request) {
        if (request.senha == null || request.senha.trim().isEmpty() || request.senha.trim().length() < 4) {
            this.errorMessage = "Formato de senha inválido. A senha deve ter pelo menos 4 caracteres.";
            return false;
        }
        return true;
    }
}
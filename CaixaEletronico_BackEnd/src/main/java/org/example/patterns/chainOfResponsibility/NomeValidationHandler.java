package org.example.patterns.chainOfResponsibility;

import org.example.controller.CaixaEletronicoController.LoginRequest;

public class NomeValidationHandler extends AbstractValidationHandler {

    @Override
    protected boolean isValid(LoginRequest request) {
        if (request.nome == null || request.nome.trim().isEmpty() || request.nome.trim().length() < 3) {
            this.errorMessage = "Nome de usuário inválido. Deve conter pelo menos 3 caracteres.";
            return false;
        }
        return true;
    }
}

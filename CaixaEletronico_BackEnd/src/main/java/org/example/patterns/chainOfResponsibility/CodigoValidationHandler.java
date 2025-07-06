package org.example.patterns.chainOfResponsibility;
import org.example.controller.CaixaEletronicoController.LoginRequest;
public class CodigoValidationHandler extends AbstractValidationHandler{
    private static final String CODIGO_SECRETO_PADRAO = "12345";

    @Override
    protected boolean isValid(LoginRequest request) {
        if (request.codigo == null || request.codigo.trim().isEmpty()) {
            this.errorMessage = "Código de verificação ausente.";
            return false;
        }
        if (!request.codigo.equals(CODIGO_SECRETO_PADRAO)) {
            this.errorMessage = "Código de verificação inválido.";
            return false;
        }
        return true;
    }
}

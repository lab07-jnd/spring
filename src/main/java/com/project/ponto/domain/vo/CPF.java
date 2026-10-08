package com.project.ponto.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record CPF(

        @Column(name = "cpf", nullable = false, length = 11)
        String valor

) {

    private static final int QUANTIDADE_DIGITOS = 11;

    /** Construtor compacto: normaliza e valida antes de atribuir. */
    public CPF {
        if (valor == null) {
            throw new IllegalArgumentException("CPF não pode ser nulo");
        }

        String digits = valor.replaceAll("\\D", "");

        if (digits.length() != QUANTIDADE_DIGITOS) {
            throw new IllegalArgumentException(
                    "CPF deve conter 11 dígitos: %s. Formato: XXX.XXX.XXX-XX".formatted(valor)
            );
        }

        if (!validarDigitosVerificadores(digits)) {
            throw new IllegalArgumentException("CPF com dígitos verificadores inválidos");
        }

        valor = digits;
    }

    private static boolean validarDigitosVerificadores(String cpf) {
        // Rejeita sequências repetidas: 000..., 111..., etc.
        if (cpf.chars().distinct().count() == 1) {
            return false;
        }

        int primeiro = calcularDigito(cpf, 9, 10);
        int segundo  = calcularDigito(cpf, 10, 11);

        return (cpf.charAt(9)  - '0') == primeiro
                && (cpf.charAt(10) - '0') == segundo;
    }

    private static int calcularDigito(String cpf, int limite, int pesoInicial) {
        int soma = 0;
        for (int i = 0; i < limite; i++) {
            soma += (cpf.charAt(i) - '0') * (pesoInicial - i);
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    /** CPF sem formatação (somente dígitos). */
    public String raw() {
        return valor;
    }

    /** CPF formatado: XXX.XXX.XXX-XX */
    public String formatted() {
        return "%s.%s.%s-%s".formatted(
                valor.substring(0, 3),
                valor.substring(3, 6),
                valor.substring(6, 9),
                valor.substring(9)
        );
    }

    /** Valida sem lançar exceção. */
    public static boolean isValid(String cpf) {
        try {
            new CPF(cpf);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return formatted();
    }
}
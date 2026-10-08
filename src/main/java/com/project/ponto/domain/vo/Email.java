package com.project.ponto.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.regex.Pattern;

@Embeddable
public record Email(

        @Column(name = "email", nullable = false, length = 254)
        String valor

) {

    private static final Pattern EMAIL_REGEX = Pattern.compile(
            """
            ^(?<local>[A-Za-z0-9._%+\\-]+)\
            @\
            (?<dominio>(?:[A-Za-z0-9](?:[A-Za-z0-9\\-]*[A-Za-z0-9])?\\.)+[A-Za-z]{2,})$
            """,
            Pattern.COMMENTS
    );

    private static final int TAMANHO_TOTAL_MAX = 254;
    private static final int TAMANHO_LOCAL_MAX = 64;

    public Email {
        if (valor == null) {
            throw new IllegalArgumentException("E-mail não pode ser nulo");
        }

        String normalizado = valor.strip().toLowerCase();

        validarFormato(normalizado);
        validarTamanhoTotal(normalizado);

        String local = extrairLocal(normalizado);
        validarTamanhoLocal(local);
        validarPontosLocal(local);

        valor = normalizado;
    }

    // ----- auxiliares de parsing -----

    private static String extrairLocal(String email) {
        return email.substring(0, email.indexOf('@'));
    }

    private static String extrairDominio(String email) {
        return email.substring(email.indexOf('@') + 1);
    }

    // ----- validações privadas -----

    private static void validarFormato(String email) {
        if (email.isEmpty()) {
            throw new IllegalArgumentException("E-mail não pode ser vazio");
        }
        if (!EMAIL_REGEX.matcher(email).matches()) {
            throw new IllegalArgumentException(
                    "E-mail inválido: %s. Formato esperado: usuario@dominio.com".formatted(email)
            );
        }
    }

    private static void validarTamanhoTotal(String email) {
        if (email.length() > TAMANHO_TOTAL_MAX) {
            throw new IllegalArgumentException(
                    "E-mail excede o limite de 254 caracteres, recebido " + email.length()
            );
        }
    }

    private static void validarTamanhoLocal(String local) {
        if (local.length() > TAMANHO_LOCAL_MAX) {
            throw new IllegalArgumentException(
                    "Parte local do e-mail excede o limite de 64 caracteres, recebido "
                            + local.length()
            );
        }
    }

    private static void validarPontosLocal(String local) {
        if (local.contains("..") || local.startsWith(".") || local.endsWith(".")) {
            throw new IllegalArgumentException(
                    "Parte local do e-mail não pode conter pontos consecutivos, "
                            + "nem iniciar/terminar com ponto: " + local
            );
        }
    }

    /** E-mail normalizado (lower case, sem espaços). */
    public String raw() {
        return valor;
    }

    /** Forma canônica de exibição — mesmo valor normalizado. */
    public String formatted() {
        return valor;
    }

    public String localPart() {
        return extrairLocal(valor);
    }

    public String domain() {
        return extrairDominio(valor);
    }

    /** Valida sem lançar exceção. */
    public static boolean isValid(String enderecoEmail) {
        try {
            new Email(enderecoEmail);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return valor;
    }
}
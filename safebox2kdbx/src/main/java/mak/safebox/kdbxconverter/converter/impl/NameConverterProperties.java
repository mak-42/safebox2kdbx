package mak.safebox.kdbxconverter.converter.impl;

import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

/**
 * Safebox fields name converter properties.
 */
@Data
@Validated
@ConfigurationProperties("converter.field.name")
public class NameConverterProperties {

    /**
     * Password field names.
     */
    private Set<String> password = Set.of("password", "kennwort", "contraseña", "hasło", "parola", "пароль", "код");

    /**
     * User name field names.
     */
    private Set<String> username = Set.of("user name",
            "username",
            "identifier",
            "login",
            "benutzername",
            "id-nummer",
            "nombre de usuario",
            "identificador",
            "iniciar sesión",
            "nome utente",
            "identificativo",
            "nazwa użytkownika",
            "identyfikator",
            "nume utilizator",
            "id",
            "имя пользователя",
            "идентификатор",
            "логин",
            "ім'я користувача",
            "ідентифікатор",
            "логін");

    /**
     * URL field names.
     */
    private Set<String> url = Set.of("url", "www");
}

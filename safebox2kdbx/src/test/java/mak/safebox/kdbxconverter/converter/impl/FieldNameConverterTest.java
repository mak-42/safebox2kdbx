package mak.safebox.kdbxconverter.converter.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.linguafranca.pwdb.Entry.STANDARD_PROPERTY_NAME_PASSWORD;
import static org.linguafranca.pwdb.Entry.STANDARD_PROPERTY_NAME_URL;
import static org.linguafranca.pwdb.Entry.STANDARD_PROPERTY_NAME_USER_NAME;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests of {@link FieldNameConverter}.
 */
@ExtendWith(MockitoExtension.class)
class FieldNameConverterTest {

    /**
     * Safebox fields name converter properties.
     */
    private NameConverterProperties properties = new NameConverterProperties();

    /**
     * Converter to test.
     */
    private FieldNameConverter converter;

    /**
     * Initializes data and instances for every test.
     */
    @BeforeEach
    void setUp() {
        converter = new FieldNameConverter(properties);
    }

    /**
     * Test method for {@link FieldNameConverter#convert(String, List)}.
     */
    @Test
    void testConvert() {
        // given:
        final List<String> names1 = List.of("Somename", "Username", "Login", "PassWord", "url", "www");
        final List<String> names2 = List.of("Somename", "Login", "PassWord", "Username", "www", "url");

        // when:
        // then:
        assertThat(converter.convert("Somename", names1)).isEqualTo("Somename");
        assertThat(converter.convert("Username", names1)).isEqualTo(STANDARD_PROPERTY_NAME_USER_NAME);
        assertThat(converter.convert("PassWord", names1)).isEqualTo(STANDARD_PROPERTY_NAME_PASSWORD);
        assertThat(converter.convert("Login", names1)).isEqualTo("Login");
        assertThat(converter.convert("url", names1)).isEqualTo(STANDARD_PROPERTY_NAME_URL);
        assertThat(converter.convert("www", names1)).isEqualTo("www");
        assertThat(converter.convert("Somename", names2)).isEqualTo("Somename");
        assertThat(converter.convert("Username", names2)).isEqualTo("Username");
        assertThat(converter.convert("PassWord", names2)).isEqualTo(STANDARD_PROPERTY_NAME_PASSWORD);
        assertThat(converter.convert("Login", names2)).isEqualTo(STANDARD_PROPERTY_NAME_USER_NAME);
        assertThat(converter.convert("url", names2)).isEqualTo("url");
        assertThat(converter.convert("www", names2)).isEqualTo(STANDARD_PROPERTY_NAME_URL);
    }
}

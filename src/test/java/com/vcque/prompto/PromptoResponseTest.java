package com.vcque.prompto;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PromptoResponseTest extends BasePlatformTestCase {

    public static final String text = """
            Sure! You can add the following code to your imports:
            ```java
            import com.fasterxml.jackson.databind.ObjectMapper;
            ```
            And then you can create an instance of the `ObjectMapper` class like this:
            ```java
            ObjectMapper objectMapper = new ObjectMapper();
            ```
            Wherever you need to use it.
            """;


    @Test
    public void testMultipleCodeBlock() {
        var blocks = PromptoResponse.extractEditorBlocks(text);

        assertThat(blocks).hasSize(2);
        assertThat(blocks.get(0).lang()).isEqualTo("java");
        assertThat(blocks.get(0).code().trim()).isEqualTo("import com.fasterxml.jackson.databind.ObjectMapper;");

        assertThat(blocks.get(1).lang()).isEqualTo("java");
        assertThat(blocks.get(1).code().trim()).isEqualTo("ObjectMapper objectMapper = new ObjectMapper();");
    }
}
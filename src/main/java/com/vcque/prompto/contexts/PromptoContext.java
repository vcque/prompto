package com.vcque.prompto.contexts;

import com.vcque.prompto.Prompts;
import com.vcque.prompto.Utils;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Represents a context state.
 */
@Getter
@EqualsAndHashCode
@Builder
public class PromptoContext {

    @Builder.Default
    @EqualsAndHashCode.Include
    private String id = "unique";

    @EqualsAndHashCode.Include
    private Type type;

    private String value;

    public int cost() {
        return Utils.countTokens(Prompts.promptoContext(this).getContent());
    }

    @RequiredArgsConstructor
    public enum Type {
        EDITOR("the user editor's content. Can be referred as `this file`"),
        CLASS("a source class of the project"),
        TYPE("a type used in the project"),
        ERROR("an error in the project"),
        SELECTION("the current user's selection. Can be referred as `this`"),
        LANGUAGE("the user editor's language"),
        SETTINGS("the global Prompto settings"),
        FILE_STRUCTURE("the file structure of the project"),
        DATABASE("the database schema used by the editor"),
        METHOD("the currently focused method. Can be referred as `this method`"),
        /** Used for setting up the prompto context. */
        EXAMPLE("$description_of_the_type");

        public final String description;
    }
}

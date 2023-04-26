package com.vcque.prompto.pipelines;

import com.vcque.prompto.contexts.PromptoRetriever;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PromptoRetrieverDefinition {
    PromptoRetriever retriever;
    boolean optional;

    public static PromptoRetrieverDefinition of(PromptoRetriever context) {
        return PromptoRetrieverDefinition.builder()
                .retriever(context)
                .optional(false)
                .build();
    }

    public static PromptoRetrieverDefinition ofOptional(PromptoRetriever retriever) {
        return PromptoRetrieverDefinition.builder()
                .retriever(retriever)
                .optional(true)
                .build();
    }
}

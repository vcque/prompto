package com.vcque.prompto.pipelines;

import com.vcque.prompto.contexts.PromptoContext;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PromptoContextDefinition {
    PromptoContext context;
    boolean optional;

    public static PromptoContextDefinition of(PromptoContext context) {
        return PromptoContextDefinition.builder()
                .context(context)
                .optional(false)
                .build();
    }

    public static PromptoContextDefinition ofOptional(PromptoContext context) {
        return PromptoContextDefinition.builder()
                .context(context)
                .optional(true)
                .build();
    }
}

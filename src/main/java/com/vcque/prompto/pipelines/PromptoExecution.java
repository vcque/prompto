package com.vcque.prompto.pipelines;

import com.vcque.prompto.contexts.PromptoContext;

import java.util.List;

@FunctionalInterface
public interface PromptoExecution<T> {

    /**
     * Executes the given response with the provided scope and contexts.
     *
     * @param response  the response of the LLM
     * @param scope     the action scope of the pipeline
     * @param contexts  the contexts contributed to the pipeline
     */
    void execute(T response, PromptoPipeline.Scope scope, List<PromptoContext> contexts);
}

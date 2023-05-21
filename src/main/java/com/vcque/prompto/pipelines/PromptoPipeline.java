package com.vcque.prompto.pipelines;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.vcque.prompto.outputs.PromptoOutput;
import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents a Prompto pipeline which defines the steps needed to query ChatGPT and produces the desired output.
 * There is:
 * - Which context information are used for the query
 * - The format of the desired output of ChatGPT
 * - The effects to apply to the initial scope once the results have been retrieved
 *
 * @param <T> the output type of the pipeline
 */
@Data
@Builder
public class PromptoPipeline<T> {
    private String name;
    private String defaultInput;
    private List<PromptoRetrieverDefinition> retrievers;
    private PromptoOutput<T> output;
    private PromptoExecution<T> execution;
    @Builder.Default
    private List<String> stopwords = List.of();

    /**
     * Represents the scope of a pipeline execution, containing project, editor, and PSI element information.
     */
    public record Scope(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
    }

    /**
     * Determines if the pipeline is available based on the provided scope.
     * A pipeline is considered available if all non-optional contexts are available given the scope.
     *
     * @param scope the scope containing the IntelliJ project, editor, and PSI element
     * @return true if the pipeline is available, false otherwise
     */
    public boolean isAvailable(Scope scope) {
        if (retrievers == null) {
            return true;
        }
        return retrievers.stream().allMatch(c -> c.optional || c.getRetriever().isAvailable(scope.project(), scope.editor(), scope.element()));
    }

}


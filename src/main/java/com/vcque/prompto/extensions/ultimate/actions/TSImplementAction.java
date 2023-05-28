package com.vcque.prompto.extensions.ultimate.actions;

import com.intellij.lang.Language;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.vcque.prompto.PromptoResponse;
import com.vcque.prompto.Utils;
import com.vcque.prompto.actions.PromptoAction;
import com.vcque.prompto.contexts.EditorContentRetriever;
import com.vcque.prompto.contexts.ErrorRetriever;
import com.vcque.prompto.contexts.LanguageRetriever;
import com.vcque.prompto.contexts.PromptoContext;
import com.vcque.prompto.extensions.ultimate.contexts.TSImplementTargetRetriever;
import com.vcque.prompto.extensions.ultimate.contexts.TSTypesRetriever;
import com.vcque.prompto.outputs.MethodOutput;
import com.vcque.prompto.pipelines.PromptoPipeline;
import com.vcque.prompto.pipelines.PromptoRetrieverDefinition;
import com.vcque.prompto.ui.PromptoAnswerDialog;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Contextual action for implementing or modifying a Typescript method.
 */
public class TSImplementAction extends PromptoAction<PromptoResponse> {

    @Override
    public PromptoPipeline<PromptoResponse> pipeline() {
        return PromptoPipeline.<PromptoResponse>builder()
                .name("implement")
                .retrievers(List.of(
                        PromptoRetrieverDefinition.of(new TSImplementTargetRetriever()),
                        PromptoRetrieverDefinition.of(new LanguageRetriever()),
                        PromptoRetrieverDefinition.ofOptional(new EditorContentRetriever()),
                        PromptoRetrieverDefinition.ofOptional(new ErrorRetriever()),
                        PromptoRetrieverDefinition.ofOptional(new TSTypesRetriever())
                ))
                .stopwords(List.of(PromptoResponse.EDITOR_STOPWORD))
                .defaultInput("Implement this")
                .output(new MethodOutput())
                .execution(this::apply)
                .build();
    }

    private void apply(PromptoResponse result, PromptoPipeline.Scope scope, List<PromptoContext> contexts) {
        var project = scope.project();
        WriteCommandAction.runWriteCommandAction(project, () -> {

            var hasChanged = insertCode(scope, result);
            if (!hasChanged) {
                ApplicationManager.getApplication().invokeLater(() -> new PromptoAnswerDialog(scope.project(), result.getRaw()).show());
            }
        });
    }

    private boolean insertCode(PromptoPipeline.Scope scope, PromptoResponse result) {
        // 1. find function to replace
        var toReplace = Utils.findParentOfType(scope.element(), TypeScriptFunction.class);
        if (toReplace == null) {
            return false;
        }

        // 2. find function that replace
        var newFunction = result.firstBlock()
                .map(block -> asPsiFile(block, scope))
                .map(this::findFunction)
                .orElse(null);
        if (newFunction == null) {
            return false;
        }

        // 3. replace!
        try {
            toReplace.replace(newFunction);
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    private PsiFile asPsiFile(PromptoResponse.EditorBlock blockCode, PromptoPipeline.Scope scope) {
        String code = blockCode.code();
        String lang = blockCode.lang();
        Language language = Language.findLanguageByID(lang);
        if (language == null) {
            return null;
        }
        var factory = PsiFileFactory.getInstance(scope.project());
        return factory.createFileFromText("temp." + lang, language, code);
    }

    private PsiElement findFunction(PsiElement element) {
        var visitor = new PsiRecursiveElementVisitor() {
            TypeScriptFunction function = null;

            @Override
            public void visitElement(@NotNull PsiElement visited) {
                if (function == null && visited instanceof TypeScriptFunction tsf) {
                    function = tsf;
                } else {
                    super.visitElement(visited);
                }
            }
        };

        element.accept(visitor);
        return visitor.function;
    }

}

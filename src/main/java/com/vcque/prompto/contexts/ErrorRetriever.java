package com.vcque.prompto.contexts;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.impl.DocumentMarkupModel;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.vcque.prompto.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class ErrorRetriever implements PromptoRetriever {

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        return Utils.findParentOfType(element, PsiMethod.class) != null;
    }

    @Override
    public List<PromptoContext> retrieveContexts(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        var psiMethod = Utils.findParentOfType(element, PsiMethod.class);
        if (psiMethod == null) {
            return List.of();
        }

        return findErrors(project, editor.getDocument(), psiMethod)
                .stream()
                .map(highlightInfo ->
                        new PromptoContext(highlightInfo.getText(), PromptoContext.Type.ERROR, highlightInfo.getDescription())
                ).toList();
    }

    private List<HighlightInfo> findErrors(Project project, Document document, PsiMethod psiMethod) {

        // Get the document for the PsiFile
        if (document != null) {
            // Get the markup model for the document in the current project
            var markupModel = DocumentMarkupModel.forDocument(document, project, true);

            // Retrieve and iterate over the error highlights
            return Arrays.stream(markupModel.getAllHighlighters())
                    .filter(hl -> isInMethod(hl, psiMethod))
                    .map(RangeHighlighter::getErrorStripeTooltip)
                    .filter(HighlightInfo.class::isInstance)
                    .map(HighlightInfo.class::cast)
                    .filter(info -> info.getSeverity() == HighlightSeverity.ERROR)
                    .toList();
        }
        return List.of();
    }

    private boolean isInMethod(RangeHighlighter hl, PsiMethod psiMethod) {
        var startOffset = hl.getStartOffset();
        var endOffset = hl.getEndOffset();
        var methodStartOffset = psiMethod.getTextRange().getStartOffset();
        var methodEndOffset = psiMethod.getTextRange().getEndOffset();
        return startOffset >= methodStartOffset && endOffset <= methodEndOffset;
    }

    @Override
    public String name() {
        return "Errors";
    }
}

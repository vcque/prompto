package com.vcque.prompto.extensions.ultimate.contexts;

import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.vcque.prompto.Utils;
import com.vcque.prompto.contexts.PromptoContext;
import com.vcque.prompto.contexts.PromptoUniqueRetriever;
import com.vcque.prompto.extensions.ultimate.lang.typescript.PromptoTSUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class TSImplementTargetRetriever implements PromptoUniqueRetriever {
    @Override
    public String name() {
        return "Target";
    }

    @Override
    public String retrieveUniqueContext(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        var method = Utils.findParentOfType(element, TypeScriptFunction.class);
        return Optional.ofNullable(method)
                .map(m -> {
                    var copy = (TypeScriptFunction) method.copy();
                    PromptoTSUtils.removeBody(copy);
                    return copy.getText();
                })
                .orElse(null);
    }

    @Override
    public PromptoContext.Type type() {
        return PromptoContext.Type.METHOD;
    }

}

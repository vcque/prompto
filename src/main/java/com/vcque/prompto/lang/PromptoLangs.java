package com.vcque.prompto.lang;

import com.intellij.lang.Language;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtilBase;
import com.vcque.prompto.lang.generic.PromptoGenericLang;
import com.vcque.prompto.lang.java.PromptoJavaLang;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Optional;

/**
 * Lang register.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PromptoLangs implements PromptoLang {

    public static PromptoLang getInstance() {
        return new PromptoLangs();
    }

    @Override
    public String shrinkFile(PsiFile file) {
        var lang = getLanguage(file);
        return findDelegate(lang).shrinkFile(file);
    }

    @Override
    public String shrinkEditor(Editor editor, int maxCost) {
        var project = editor.getProject();
        if (project == null) {
            return null;
        }
        var psiFile = PsiUtilBase.getPsiFileInEditor(editor, editor.getProject());
        var lang = getLanguage(psiFile);
        return findDelegate(lang).shrinkEditor(editor, maxCost);
    }

    private static Language getLanguage(PsiFile file) {
        return Optional.ofNullable(file)
                .map(PsiElement::getLanguage)
                .orElse(null);
    }

    private PromptoLang findDelegate(Language lang) {
        if (lang == JavaLanguage.INSTANCE) {
            return new PromptoJavaLang();
        } else {
            return new PromptoGenericLang();
        }
    }

}

package com.vcque.prompto.lang.java;

import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.vcque.prompto.Utils;
import com.vcque.prompto.lang.PromptoLang;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class PromptoJavaLang implements PromptoLang {
    @Override
    public String shrinkFile(PsiFile file) {
        // Should remove:
        // - imports
        // - private inner classes
        // - private methods's body

        return Utils.cleanWhitespaces(file.getText());
    }

    /**
     * Reduces the content of the provided editor to meet the specified maximum cost constraint.
     * The method performs the following actions:
     * - Removes import statements, as they are not needed for ChatGPT.
     * - Remove the bodies of public methods in the file, unless:
     * - The file has been shrinked enough.
     * - The method is the parent of the current element.
     * <p>
     * These actions are based on the assumption that methods tend to use other private methods
     * from the same file rather than public methods.
     *
     * @param editor  The editor containing the code to be shrinked.
     * @param maxCost The maximum allowed cost, represented by the number of tokens in the editor.
     * @return The shrinked content of the editor as a string, with unnecessary content removed
     * and whitespace cleaned up.
     */
    @Override
    public String shrinkEditor(Editor editor, int maxCost) {
        var psiFile = PsiDocumentManager.getInstance(editor.getProject()).getPsiFile(editor.getDocument());
        if (psiFile == null) {
            return "";
        }

        var clone = (PsiJavaFile) psiFile.copy();
        var currentElement = getElement(editor, psiFile);

        // Imports are (almost) useless for chatGPT
        Optional.ofNullable(clone.getImportList()).ifPresent(PsiElement::delete);

        if (clone.getClasses().length > 0) {
            var psiClass = clone.getClasses()[0];

            for (PsiMethod method : psiClass.getMethods()) {
                var shrinkMethod = Utils.countTokens(clone.getText()) > maxCost
                                   && method.hasModifierProperty(PsiModifier.PUBLIC)
                                   && method.getText() != null
                                   && !isMethodChild(currentElement, method);
                if (shrinkMethod) {
                    PromptoJavaUtils.elideBody(method);
                }
            }
        }

        return Utils.cleanWhitespaces(clone.getText());
    }

    /**
     * Can't use `isAncestor` in this context. Best guess is to use method signature
     */
    private static boolean isMethodChild(PsiElement child, PsiMethod method) {
        if (child == null) {
            return false;
        }
        var childMethod = Utils.findParentOfType(child, PsiMethod.class);
        if (childMethod == null) {
            return false;
        } else {
            return Utils.hasSameSignature(method, childMethod);
        }
    }

    @Nullable
    private static PsiElement getElement(@NotNull Editor editor, @NotNull PsiFile file) {
        CaretModel caretModel = editor.getCaretModel();
        int position = caretModel.getOffset();
        return file.findElementAt(position);
    }
}

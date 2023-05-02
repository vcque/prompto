package com.vcque.prompto.lang.generic;

import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import com.vcque.prompto.Utils;
import com.vcque.prompto.lang.PromptoLang;


/**
 * Generic lang handling.
 */
public class PromptoGenericLang implements PromptoLang {
    @Override
    public String shrinkFile(PsiFile file) {
        return Utils.cleanWhitespaces(file.getText());
    }

    @Override
    public String shrinkEditor(Editor editor, int maxCost) {
        // TODO: should try to keep direct parents of the selection/caret non-elided
        // Then, should try to remove the farthest lines from the selection

        return Utils.cleanWhitespaces(editor.getDocument().getText());
    }

}

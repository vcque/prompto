package com.vcque.prompto.lang;

import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;

public interface PromptoLang {

    /** Shrinks a file's content. Will be used for import-based context. */
    String shrinkFile(PsiFile file);

    /** Shrinks the editor's content. */
    String shrinkEditor(Editor editor, int maxCost);
}

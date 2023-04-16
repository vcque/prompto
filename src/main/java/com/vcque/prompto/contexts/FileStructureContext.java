package com.vcque.prompto.contexts;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.vcque.prompto.Prompts;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FileStructureContext implements PromptoContext {

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        return true;
    }

    @Override
    public String retrieveContext(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        return buildFileStructure(project);
    }

    @Override
    public ChatMessage toMessage(String contextValue) {
        return Prompts.fileStructureContext(contextValue);
    }

    public String buildFileStructure(Project project) {
        var psiManager = PsiManager.getInstance(project);

        // Get the project base directory
        var baseDir = ProjectUtil.guessProjectDir(project);
        if (baseDir == null) {
            return null;
        }
        var psiBaseDir = psiManager.findDirectory(baseDir);
        if (psiBaseDir == null) {
            return null;
        }

        List<String> fileStructure = retrieveFile(psiBaseDir, "");
        return String.join("\n", fileStructure);
    }


    private List<String> retrieveFile(PsiDirectory directory, String indent) {
        var changeListManager = ChangeListManager.getInstance(directory.getProject());

        var nextIndent = indent + "->";

        var fileStructure = Arrays.stream(directory.getSubdirectories())
                .flatMap(sub -> retrieveFile(sub, nextIndent).stream())
                .collect(Collectors.toList());

        Arrays.stream(directory.getFiles())
                .filter(f -> !changeListManager.isIgnoredFile(f.getVirtualFile()))
                .forEach(f -> fileStructure.add(nextIndent + f.getName()));

        if (!fileStructure.isEmpty()) {
            fileStructure.add(0, indent + directory.getName());
        }
        return fileStructure;
    }
}
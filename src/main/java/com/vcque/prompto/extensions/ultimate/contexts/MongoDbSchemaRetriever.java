package com.vcque.prompto.extensions.ultimate.contexts;

import com.intellij.database.model.DasDataSource;
import com.intellij.database.model.DasObject;
import com.intellij.database.model.DasTable;
import com.intellij.database.model.DasTypedObject;
import com.intellij.database.psi.DataSourceManager;
import com.intellij.database.util.DasUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.vcque.prompto.contexts.PromptoContext;
import com.vcque.prompto.contexts.PromptoUniqueRetriever;
import org.jetbrains.annotations.NotNull;

public class MongoDbSchemaRetriever implements PromptoUniqueRetriever {

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        var psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
        return psiFile != null &&
               psiFile.getLanguage().getID().equals("MongoJSExt")
               && retrieveDataSource(project, editor) != null;
    }

    /**
     * There might be a better way...
     */
    public DasDataSource retrieveDataSource(Project project, Editor editor) {
        var virtualFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
        if (virtualFile == null) {
            return null;
        }

        var editorPath = virtualFile.getPath();
        for (var datasourceManager : DataSourceManager.getManagers(project)) {
            for (var dataSource : datasourceManager.getDataSources()) {
                var datasourceId = dataSource.getUniqueId();
                if (editorPath.contains(datasourceId)) {
                    return dataSource;
                }
            }
        }
        return null;
    }

    @Override
    public String name() {
        return "MongoDB schema";
    }

    @Override
    public String retrieveUniqueContext(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        var database = retrieveDataSource(project, editor);
        return printDatabaseSchema(database);
    }


    public String printDatabaseSchema(DasDataSource datasource) {
        StringBuilder stringBuilder = new StringBuilder();
        for (DasTable table : DasUtil.getTables(datasource)) {
            stringBuilder.append("\n");
            stringBuilder.append("collection ");
            stringBuilder.append(table.getName());
            // Iterate through the columns and print their definitions
            for (DasObject column : DasUtil.getColumns(table)) {
                stringBuilder.append("\n\t");
                stringBuilder.append(column.getName());
                if (column instanceof DasTypedObject dto) {
                    stringBuilder.append(" ");
                    stringBuilder.append(dto.getDataType().getSpecification());
                }
            }
        }

        return stringBuilder.toString();
    }

    @Override
    public PromptoContext.Type type() {
        return PromptoContext.Type.DATABASE;
    }
}
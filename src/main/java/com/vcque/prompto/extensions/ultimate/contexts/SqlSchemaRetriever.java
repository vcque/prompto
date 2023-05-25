package com.vcque.prompto.extensions.ultimate.contexts;

import com.intellij.database.model.DasDataSource;
import com.intellij.database.model.DasForeignKey;
import com.intellij.database.model.DasObject;
import com.intellij.database.model.DasTable;
import com.intellij.database.model.DasTypedObject;
import com.intellij.database.psi.DataSourceManager;
import com.intellij.database.util.DasUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.vcque.prompto.contexts.PromptoContext;
import com.vcque.prompto.contexts.PromptoUniqueRetriever;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class SqlSchemaRetriever implements PromptoUniqueRetriever {

    /**
     * System schemas for postgresql.
     */
    private static final Set<String> FILTERED_SCHEMAS = Set.of(
            "information_schema",
            "pg_catalog"
    );

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        var psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
        return psiFile != null &&
               psiFile.getLanguage().isKindOf("SQL")
               && retrieveDataSource(project, editor) != null;
    }

    @Override
    public String name() {
        return "SQL schema";
    }

    @Override
    public String retrieveUniqueContext(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        var datasource = retrieveDataSource(project, editor);
        return printDatabaseSchema(datasource);
    }

    /**
     * There might be a better way...
     */
    public DasDataSource retrieveDataSource(Project project, Editor editor) {
        var psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
        var editorPath = psiFile.getVirtualFile().getPath();

        for (var datasourceManager : DataSourceManager.getManagers(project)) {
            for (DasDataSource dataSource : datasourceManager.getDataSources()) {
                var datasourceId = dataSource.getUniqueId();
                if (editorPath.contains(datasourceId)) {
                    return dataSource;
                }
            }
        }
        return null;
    }

    public String printDatabaseSchema(DasDataSource dataSource) {
        var stringBuilder = new StringBuilder();
        // Iterate through the tables and print their names
        for (DasTable table : DasUtil.getTables(dataSource)) {
            if (FILTERED_SCHEMAS.contains(DasUtil.getNamespace(table).getName())) {
                continue;
            }
            stringBuilder.append("\n");
            stringBuilder.append("table ");
            stringBuilder.append(table.getName());
            // Iterate through the columns and print their definition
            for (DasObject column : DasUtil.getColumns(table)) {
                stringBuilder.append("\n  ");
                stringBuilder.append(column.getName());
                if (column instanceof DasTypedObject dto) {
                    stringBuilder.append(" ");
                    stringBuilder.append(dto.getDataType().getSpecification());
                }
            }

            for (var foreignKey : DasUtil.getForeignKeys(table)) {
                stringBuilder.append("\n  ");
                stringBuilder.append(foreignKeyAsText(foreignKey));
                if (foreignKey instanceof DasTypedObject dto) {
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

    public String foreignKeyAsText(DasForeignKey fk) {
        var result = "";
        result += "foreign key ";
        result += fk.getName();
        result += " (";
        result += StreamSupport.stream(fk.getColumnsRef().names().spliterator(), false).collect(Collectors.joining(", "));
        result += ") references ";
        result += fk.getRefTableName();
        result += " (";
        result += StreamSupport.stream(fk.getRefColumns().names().spliterator(), false).collect(Collectors.joining(", "));
        result += ")";
        return result;
    }
}

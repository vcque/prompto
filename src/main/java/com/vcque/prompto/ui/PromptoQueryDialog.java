package com.vcque.prompto.ui;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.CheckboxTree;
import com.intellij.ui.CheckboxTreeListener;
import com.intellij.ui.CheckedTreeNode;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.panels.NonOpaquePanel;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.vcque.prompto.contexts.PromptoContext;
import com.vcque.prompto.pipelines.PromptoPipeline;
import com.vcque.prompto.pipelines.PromptoRetrieverDefinition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PromptoQueryDialog extends DialogWrapper {
    private final CheckboxTree contextTree;

    private final JBTextField userInput = new JBTextField();
    private final JBLabel tokenCount = new JBLabel();

    public static final int CLIPBOARD_EXIT_CODE = 2;

    private final int maxTokens;

    public PromptoQueryDialog(PromptoPipeline<?> pipeline, LinkedHashMap<PromptoRetrieverDefinition, Set<PromptoContext>> contextsByRetrievers, int maxTokens) {
        super(true);
        this.maxTokens = maxTokens;
        this.contextTree = buildCheckedTree(maxTokens, contextsByRetrievers);

        userInput.setText(pipeline.getDefaultInput());
        userInput.setMinimumSize(new Dimension(500, 0));
        tokenCount.setToolTipText("Most LLMs handle a 4k token context. Go higher at your own risk.");

        contextTree.addCheckboxTreeListener(new CheckboxTreeListener() {
            @Override
            public void nodeStateChanged(@NotNull CheckedTreeNode node) {
                updateTokens();
            }
        });
        updateTokens();

        init();
        setTitle("Prompto " + pipeline.getName());
    }

    public List<PromptoContext> getSelectedContexts() {
        return Arrays.stream(contextTree.getCheckedNodes(PromptoContext.class, ctn -> true)).toList();
    }

    public String getUserInput() {
        return userInput.getText();
    }

    /**
     * Update the token count based on the checked contexts.
     */
    private void updateTokens() {
        var cost = Arrays.stream(contextTree.getCheckedNodes(PromptoContext.class, ctn -> true))
                .mapToInt(PromptoContext::cost)
                .sum();

        var overMax = cost > maxTokens;
        tokenCount.setForeground(overMax ? JBColor.RED : JBColor.foreground());
        tokenCount.setFont(tokenCount.getFont().deriveFont(Font.ITALIC + (overMax ? Font.BOLD : Font.PLAIN)));
        tokenCount.setText("Approximate token usage: %d/%d".formatted(cost, maxTokens));
    }

    /**
     *
     * @param maxTokens Used to define the initial checked contexts
     * @param contextsByRetrievers data model
     * @return The JTree
     */
    private CheckboxTree buildCheckedTree(int maxTokens, Map<PromptoRetrieverDefinition, Set<PromptoContext>> contextsByRetrievers) {
        var tokenUsage = 0;
        var rootNode = new CheckedTreeNode("retrievers");
        for (var entry : contextsByRetrievers.entrySet()) {
            var retriever = entry.getKey();
            var contexts = entry.getValue();
            var retrieverNode = new CheckedTreeNode(retriever);
            for (var context : contexts) {
                var contextNode = new CheckedTreeNode(context);
                contextNode.setEnabled(retriever.isOptional());
                retrieverNode.add(contextNode);
                tokenUsage += context.cost();
                contextNode.setChecked(tokenUsage <= maxTokens);
            }
            if (retrieverNode.getChildCount() > 0) {
                retrieverNode.setEnabled(retriever.isOptional());
                rootNode.add(retrieverNode);
            }
        }
        var result = new CheckboxTree(new CheckboxTree.CheckboxTreeCellRenderer(true, true) {
            @Override
            public void customizeRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                if (!(value instanceof DefaultMutableTreeNode)) {
                    return;
                }
                value = ((DefaultMutableTreeNode) value).getUserObject();
                if (value instanceof PromptoRetrieverDefinition prd) {
                    getTextRenderer().append(prd.getRetriever().name());
                    if (!prd.isOptional()) {
                        getTextRenderer().append(
                                "  -  required",
                                new SimpleTextAttributes(SimpleTextAttributes.STYLE_ITALIC, JBColor.gray)
                        );
                    }
                } else if (value instanceof PromptoContext pc) {
                    getTextRenderer().append(pc.getId());
                    getTextRenderer().append(
                            "  -  %d tokens".formatted(pc.cost()),
                            new SimpleTextAttributes(SimpleTextAttributes.STYLE_ITALIC, JBColor.gray)
                    );
                }
            }
        }, rootNode);
        expandTree(result, new TreePath(rootNode));
        return result;
    }

    private void expandTree(CheckboxTree tree, TreePath parent) {
        TreeNode node = (TreeNode) parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (var e = node.children(); e.hasMoreElements(); ) {
                TreeNode n = e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandTree(tree, path);
            }
        }
        tree.expandPath(parent);
    }


    @Override
    protected @Nullable JComponent createCenterPanel() {
        var contentPane = new NonOpaquePanel(new GridLayoutManager(2, 1));
        var contextLabel = new JBLabel("Context information to add to your prompt:");
        var scrollPane = new JBScrollPane(contextTree);
        var gc = new GridConstraints();
        gc.setAnchor(GridConstraints.ANCHOR_WEST);
        contentPane.add(contextLabel, gc);
        gc.setRow(1);
        gc.setFill(GridConstraints.FILL_BOTH);
        contentPane.add(scrollPane, gc);
        return contentPane;
    }

    @Override
    protected JComponent createSouthPanel() {
        var okButton = createJButtonForAction(getOKAction());
        var clipboardButton = createJButtonForAction(clipboardAction);
        clipboardButton.setText("");
        clipboardButton.setIcon(AllIcons.Actions.Copy);
        clipboardButton.setToolTipText("Save prompt to clipboard");
        var buttonsPanel = super.createButtonsPanel(List.of(okButton, clipboardButton));
        var panel = new NonOpaquePanel();
        panel.setLayout(new GridLayoutManager(2, 2));
        var gc = new GridConstraints();
        gc.setAnchor(GridConstraints.ANCHOR_SOUTHWEST);
        gc.setFill(GridConstraints.FILL_HORIZONTAL);
        gc.setHSizePolicy(GridConstraints.SIZEPOLICY_WANT_GROW);
        gc.setColSpan(2);
        panel.add(tokenCount, gc);
        gc.setColSpan(1);
        gc.setRow(1);
        panel.add(userInput, gc);
        gc.setColumn(1);
        gc.setHSizePolicy(GridConstraints.SIZEPOLICY_FIXED);
        panel.add(buttonsPanel, gc);
        return panel;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return userInput;
    }

    @Override
    protected Action @NotNull [] createActions() {
        return new Action[]{
                getOKAction(),
                clipboardAction,
        };
    }

    protected final ClipboardAction clipboardAction = new ClipboardAction();
    protected class ClipboardAction extends DialogWrapperAction {

        protected ClipboardAction() {
            super("Clipboard");
        }

        @Override
        protected void doAction(ActionEvent e) {
            close(CLIPBOARD_EXIT_CODE);
        }

    }
}

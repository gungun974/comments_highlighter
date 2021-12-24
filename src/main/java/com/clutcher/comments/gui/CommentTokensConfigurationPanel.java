package com.clutcher.comments.gui;

import com.clutcher.comments.configuration.HighlightTokenConfiguration;
import com.clutcher.comments.highlighter.HighlightTokenType;
import com.intellij.CommonBundle;
import com.intellij.openapi.application.ApplicationBundle;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import com.intellij.ui.JBColor;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.UIBundle;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import com.intellij.util.ui.components.BorderLayoutPanel;
import com.intellij.xml.util.XmlStringUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;


public class CommentTokensConfigurationPanel extends BorderLayoutPanel implements SearchableConfigurable, Configurable.NoScroll {

    private static final HighlightTokenConfiguration tokenConfiguration = ApplicationManager.getApplication().getService(HighlightTokenConfiguration.class);

    private final JBTable tokenTable;
    private ListTableModel<Pair<HighlightTokenType, String>> tableModel;

    private static final ColumnInfo<Pair<HighlightTokenType, String>, String> TYPE_COLUMN = new ColumnInfo<Pair<HighlightTokenType, String>, String>("Type") {
        @Override
        public @Nullable String valueOf(Pair<HighlightTokenType, String> tokenTypeStringPair) {
            return tokenTypeStringPair.first.toString();
        }
    };
    private static final ColumnInfo<Pair<HighlightTokenType, String>, String> NAME_COLUMN = new ColumnInfo<Pair<HighlightTokenType, String>, String>("Token") {
        @Override
        public @Nullable String valueOf(Pair<HighlightTokenType, String> tokenTypeStringPair) {
            return tokenTypeStringPair.second;
        }
    };

    private JLabel reopenLabel;
    private final JCheckBox plainFilesHighlighting;

    public CommentTokensConfigurationPanel() {
        super(0, 20);

        reopenLabel = new JLabel(XmlStringUtil.wrapInHtml("Reopen setting window to see new tokens on Color settings page (https://youtrack.jetbrains.com/issue/IDEA-226087)"));
        reopenLabel.setForeground(JBColor.RED);

        tokenTable = new JBTable();
        tokenTable.getEmptyText().setText("No tokens defined");
        plainFilesHighlighting = new JCheckBox("Enable comment highlighting in Plain text files.");

        reset();

        BorderLayoutPanel tokensTablePanel = createTokensTablePanel();

        this.addToTop(plainFilesHighlighting);
        this.addToCenter(tokensTablePanel);
    }

    @NotNull
    private BorderLayoutPanel createTokensTablePanel() {
        var tableLabel = new JLabel(XmlStringUtil.wrapInHtml("Custom comment/keyword tokens would be used to highlight comments/keywords based on color settings"));

        var tokensTablePanel = new BorderLayoutPanel(0, 5);
        tokensTablePanel
                .addToTop(tableLabel)
                .addToCenter(
                        ToolbarDecorator.createDecorator(tokenTable)
                                        .setAddAction(button -> {
                                            AddUpdateCommentTokenDialog dlg = new AddUpdateCommentTokenDialog();
                                            dlg.setTitle("Add comment token");
                                            if (dlg.showAndGet()) {
                                                HighlightTokenType highlightTokenType = dlg.getCustomTokenType();
                                                String tokenValue = dlg.getToken();

                                                tableModel.addRow(Pair.create(highlightTokenType, tokenValue));
                                            }
                                        })
                                        .setRemoveAction(button -> {
                                            int returnValue = Messages.showOkCancelDialog("Delete selected token?",
                                                    UIBundle.message("delete.dialog.title"),
                                                    ApplicationBundle.message("button.delete"),
                                                    CommonBundle.getCancelButtonText(),
                                                    Messages.getQuestionIcon());
                                            if (returnValue == Messages.OK) {
                                                tableModel.removeRow(tokenTable.getSelectedRow());
                                            }
                                        })
                                        .setEditAction(button -> {
                                            int selectedRow = tokenTable.getSelectedRow();
                                            Pair<HighlightTokenType, String> value = tableModel.getItem(selectedRow);

                                            AddUpdateCommentTokenDialog dlg = new AddUpdateCommentTokenDialog();
                                            dlg.setTitle("Edit comment token");
                                            dlg.setCustomTokenType(value.first);
                                            dlg.setToken(value.second);

                                            if (dlg.showAndGet()) {
                                                final HighlightTokenType editedHighlightTokenType = dlg.getCustomTokenType();
                                                final String editedToken = dlg.getToken();

                                                tableModel.removeRow(selectedRow);
                                                tableModel.insertRow(selectedRow, Pair.create(editedHighlightTokenType, editedToken));
                                            }
                                        })
                                        .setButtonComparator("Add", "Edit", "Remove")
                                        .disableUpDownActions()
                                        .createPanel()
                );
        return tokensTablePanel;
    }

    @Override
    public void apply() {
        add(reopenLabel, BorderLayout.SOUTH);

        Map<HighlightTokenType, List<String>> updatedTokens = getTokenMapFromModel(tableModel);

        tokenConfiguration.setCustomTokens(updatedTokens);
        tokenConfiguration.setPlainTextFileHighlightEnabled(plainFilesHighlighting.isSelected());
    }

    @Override
    public boolean isModified() {
        boolean savedValue = tokenConfiguration.isPlainTextFileHighlightEnabled();
        boolean currentValue = plainFilesHighlighting.isSelected();

        if (currentValue != savedValue) {
            return true;
        }

        Map<HighlightTokenType, List<String>> updatedTokens = getTokenMapFromModel(tableModel);
        Map<HighlightTokenType, Collection<String>> allTokens = tokenConfiguration.getAllTokens();

        for (HighlightTokenType value : HighlightTokenType.values()) {
            if (!updatedTokens.get(value).equals(allTokens.get(value))) {
                return true;
            }
        }

        return false;
    }


    @Override
    public void reset() {
        tableModel = new ListTableModel<>(TYPE_COLUMN, NAME_COLUMN);

        Map<HighlightTokenType, Collection<String>> allTokens = tokenConfiguration.getAllTokens();

        for (Map.Entry<HighlightTokenType, Collection<String>> entry : allTokens.entrySet()) {
            HighlightTokenType tokenType = entry.getKey();
            for (String token : entry.getValue()) {
                tableModel.addRow(Pair.create(tokenType, token));
            }
        }

        tokenTable.setModel(tableModel);

        plainFilesHighlighting.setSelected(tokenConfiguration.isPlainTextFileHighlightEnabled());
    }

    private Map<HighlightTokenType, List<String>> getTokenMapFromModel(ListTableModel<Pair<HighlightTokenType, String>> tableModel) {
        Map<HighlightTokenType, List<String>> tokenMap = new EnumMap<>(HighlightTokenType.class);
        for (HighlightTokenType value : HighlightTokenType.values()) {
            tokenMap.put(value, new ArrayList<>());
        }

        for (Pair<HighlightTokenType, String> item : tableModel.getItems()) {
            List<String> mapValueList = tokenMap.get(item.first);
            mapValueList.add(item.second);
        }

        return tokenMap;
    }

    @Override
    @Nls
    public String getDisplayName() {
        return "Custom comment/keyword tokens";
    }

    @Override
    @NotNull
    public String getId() {
        return "comment.highlighter.token.configuration";
    }

    @Override
    public JComponent createComponent() {
        return this;
    }
}

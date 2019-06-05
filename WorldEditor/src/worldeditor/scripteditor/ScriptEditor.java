package worldeditor.scripteditor;

import worldeditor.Theme;
import worldeditor.UIManagerDefaults;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ScriptEditor extends JDialog {

    private final JPanel contentPanel = new JPanel();
    static Color defColor = new Color(249,38,114);
    static Color varColor = new Color(166,226,46);
    public Map<String, Color> keywords = Map.ofEntries(
            Map.entry("abstract",defColor),
            Map.entry ("boolean",varColor),
    Map.entry ("break",defColor),
    Map.entry ("byte",varColor),
    Map.entry ("case",defColor),
    Map.entry ("catch",defColor),
    Map.entry ("char",varColor),
    Map.entry ("class",defColor),
    Map.entry ("continue",defColor),
    Map.entry ("default",defColor),
    Map.entry ("do",defColor),
    Map.entry ("double",varColor),
    Map.entry ("enum",defColor),
    Map.entry ("extends",defColor),
    Map.entry ("else",defColor),
    Map.entry ("false",defColor),
    Map.entry ("final",defColor),
    Map.entry ("finally",defColor),
    Map.entry ("float",varColor),
    Map.entry ("for",defColor),
    Map.entry ("if",defColor),
    Map.entry ("implements",defColor),
            Map.entry ("import",defColor),
    Map.entry ("instanceof",defColor),
    Map.entry ("int",varColor),
    Map.entry ("interface",defColor),
    Map.entry ("long",varColor),
    Map.entry ("native",defColor),
    Map.entry ("new",defColor),
    Map.entry ("null",defColor),
    Map.entry ("package",defColor),
    Map.entry ("private",defColor),
    Map.entry ("protected",defColor),
    Map.entry ("public",defColor),
    Map.entry ("return",defColor),
    Map.entry ("short",varColor),
    Map.entry ("static",defColor),
    Map.entry ("super",defColor),
    Map.entry ("switch",defColor),
    Map.entry ("synchronized",defColor),
    Map.entry ("throw",defColor),
    Map.entry ("throws",defColor),
    Map.entry ("transient",defColor),
    Map.entry ("true",defColor),
    Map.entry ("try",defColor),
    Map.entry ("void",defColor),
    Map.entry ("volatile",defColor),
    Map.entry ("while",defColor)
    );

    /**
     * Create the dialog.
     */
    public ScriptEditor(String fileName,String contents) {
        JDialog window = this;
        setBounds(100, 100, 850, 600);
        setTitle(fileName);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        BorderLayout borderLayout = new BorderLayout();
        borderLayout.setVgap(1);
        borderLayout.setHgap(1);
        getContentPane().setLayout(borderLayout);
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new BorderLayout(0, 0));
        {
            JTextPane editorPane = new JTextPane();
            editorPane.setEditable(true);
            ScriptDoc doc = new ScriptDoc();
            Color comment = new Color(253,151,31);
            Color javadoc = new Color(63,95,191);
            Color annotation = new Color(100,100,100);
            doc.setCommentColor(comment);
            doc.setJavadocColor(javadoc);
            doc.setAnnotationColor(annotation);
            doc.setKeywords(keywords);
            editorPane.setDocument(doc);
            editorPane.setBorder(BorderFactory.createEmptyBorder());
            editorPane.setText(contents);
            contentPanel.add(new JScrollPane(editorPane), BorderLayout.CENTER);
        }
        {
            Box verticalBox = Box.createVerticalBox();
            verticalBox.setBorder(BorderFactory.createEtchedBorder());
            contentPanel.add(verticalBox, BorderLayout.EAST);
            {
                JLabel label = new JLabel("Imports");
                verticalBox.add(label);
            }
            {
                JTextArea textArea = new JTextArea();
                textArea.setColumns(10);
                verticalBox.add(new JScrollPane(textArea));
            }
            {
                JButton button = new JButton("View Scripts");
                button.addActionListener((e)->{
                    try {
                        Runtime.getRuntime().exec("explorer.exe /select," + System.getProperty("user.dir"));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });
                verticalBox.add(button);
            }
        }
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                JLabel status = new JLabel("Compile Failed");
                buttonPane.add(status);
            }
            {
                JButton save = new JButton("Save and Compile");
                save.setActionCommand("OK");
                buttonPane.add(save);
                getRootPane().setDefaultButton(save);
            }
            {
                JButton cancelButton = new JButton("Cancel");
                cancelButton.addActionListener(e -> {
                    window.dispose();
                });
                buttonPane.add(cancelButton);
            }
        }
    }

}


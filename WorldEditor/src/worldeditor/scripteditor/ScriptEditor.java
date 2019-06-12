package worldeditor.scripteditor;

import com.opengg.core.engine.Resource;
import com.opengg.core.io.FileStringLoader;
import com.opengg.core.math.Tuple;
import com.opengg.core.script.ScriptCompiler;
import com.opengg.core.util.FileUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.tools.Diagnostic;
import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ScriptEditor extends JFrame {

    static Color comment = new Color(253, 151, 31);
    static Color javadoc = new Color(63, 95, 191);
    static Color annotation = new Color(100, 100, 100);
    static Color defColor = new Color(249, 38, 114);
    static Color varColor = new Color(166, 226, 46);
    private final JLabel result;
    private final JTabbedPane tabbedPane;
    private final List<Tuple<String, Tuple<Supplier<String>, Supplier<String>>>> currentlyOpen = new ArrayList<>(); //name, content, imports
    public Map<String, Color> keywords = Map.ofEntries(
            Map.entry("abstract", defColor),
            Map.entry("boolean", varColor),
            Map.entry("break", defColor),
            Map.entry("byte", varColor),
            Map.entry("case", defColor),
            Map.entry("catch", defColor),
            Map.entry("char", varColor),
            Map.entry("class", defColor),
            Map.entry("continue", defColor),
            Map.entry("default", defColor),
            Map.entry("do", defColor),
            Map.entry("double", varColor),
            Map.entry("enum", defColor),
            Map.entry("extends", defColor),
            Map.entry("else", defColor),
            Map.entry("false", defColor),
            Map.entry("final", defColor),
            Map.entry("finally", defColor),
            Map.entry("float", varColor),
            Map.entry("for", defColor),
            Map.entry("if", defColor),
            Map.entry("implements", defColor),
            Map.entry("import", defColor),
            Map.entry("instanceof", defColor),
            Map.entry("int", varColor),
            Map.entry("interface", defColor),
            Map.entry("long", varColor),
            Map.entry("native", defColor),
            Map.entry("new", defColor),
            Map.entry("null", defColor),
            Map.entry("package", defColor),
            Map.entry("private", defColor),
            Map.entry("protected", defColor),
            Map.entry("public", defColor),
            Map.entry("return", defColor),
            Map.entry("short", varColor),
            Map.entry("static", defColor),
            Map.entry("super", defColor),
            Map.entry("switch", defColor),
            Map.entry("synchronized", defColor),
            Map.entry("throw", defColor),
            Map.entry("throws", defColor),
            Map.entry("transient", defColor),
            Map.entry("true", defColor),
            Map.entry("try", defColor),
            Map.entry("void", defColor),
            Map.entry("volatile", defColor),
            Map.entry("while", defColor)
    );

    /**
     * Create the dialog.
     */
    public ScriptEditor() {
        super();
        JFrame window = this;
        setBounds(100, 100, 850, 600);
        setTitle("Script Editor");
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        tabbedPane = new JTabbedPane();
        BorderLayout borderLayout = new BorderLayout();
        borderLayout.setVgap(1);
        borderLayout.setHgap(1);
        getContentPane().setLayout(borderLayout);
        getContentPane().add(tabbedPane, BorderLayout.CENTER);


        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
        getContentPane().add(buttonPane, BorderLayout.SOUTH);

        result = new JLabel("");
        buttonPane.add(result);

        JButton button = new JButton("View Scripts");
        button.addActionListener((e) -> {
            try {
                Runtime.getRuntime().exec("explorer.exe /select," + Resource.getAbsoluteFromLocal("resources/scripts"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        buttonPane.add(button);

        JButton compile = new JButton("Compile");
        compile.setActionCommand("OK");

        compile.addActionListener(c -> processCurrentScript());
        buttonPane.add(compile);
        getRootPane().setDefaultButton(compile);

        JButton save = new JButton("Save");
        save.addActionListener(c -> saveCurrentScript());
        buttonPane.add(save);

        JButton load = new JButton("Load file");
        load.addActionListener(a -> {
            var dialog = new JFileChooser(Resource.getAbsoluteFromLocal("resources/scripts/"));
            dialog.setFileFilter(new FileNameExtensionFilter("Script source file","ssf"));
            dialog.showOpenDialog(null);

            var resultfile = dialog.getSelectedFile();
            if (resultfile == null) return;
            var result = resultfile.getAbsolutePath();

            try {
                var data = FileStringLoader.loadStringSequence(result);
                var newName = FileUtil.getFileName(result);

                createNewEditor(newName, data.substring(0, data.indexOf("~")), data.substring(data.indexOf("~")+1));
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
        buttonPane.add(load);

        createNewEditor("","","");

        window.setVisible(true);
    }

    private void createNewEditor(String name, String imports, String contents){
        if(name.isEmpty()){
            var scriptName = JOptionPane.showInputDialog("New script name:");
            name = scriptName;
        }

        var contentPanel = new JPanel();

        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPanel.setLayout(new BorderLayout(0, 0));
        contentPanel.setPreferredSize(new Dimension(600, 600));

        Box writingBox = Box.createVerticalBox();
        writingBox.setBorder(BorderFactory.createEtchedBorder());
        writingBox.setPreferredSize(new Dimension(400, 800));
        contentPanel.add(writingBox, BorderLayout.CENTER);

        JLabel declarationLabel = new JLabel("(ScriptComponent c, float delta) -> ");
        writingBox.add(declarationLabel);

        JTextPane editorPane = new JTextPane();
        editorPane.setEditable(true);
        ScriptDoc doc = new ScriptDoc();
        doc.setCommentColor(comment);
        doc.setJavadocColor(javadoc);
        doc.setKeywords(keywords);

        editorPane.setDocument(doc);
        editorPane.setBorder(BorderFactory.createEmptyBorder());
        editorPane.setText(contents);
        writingBox.add(new JScrollPane(editorPane));

        Box importsBox = Box.createVerticalBox();
        importsBox.setBorder(BorderFactory.createEtchedBorder());
        importsBox.setPreferredSize(new Dimension(200, 800));
        contentPanel.add(importsBox, BorderLayout.EAST);

        JLabel scriptLabel = new JLabel("Imports");
        importsBox.add(scriptLabel);

        JTextPane importPane = new JTextPane();
        ScriptDoc importDoc = new ScriptDoc();
        importDoc.setCommentColor(comment);
        importDoc.setJavadocColor(javadoc);
        importDoc.setKeywords(keywords);

        importPane.setDocument(importDoc);
        importPane.setBorder(BorderFactory.createEtchedBorder());
        importPane.setBounds(0,20,300,780);
        importPane.setText(imports);
        importsBox.add(new JScrollPane(importPane));

        currentlyOpen.add(Tuple.of(name, Tuple.of(editorPane::getText, importPane::getText)));

        tabbedPane.addTab(name, contentPanel);
        tabbedPane.setTabComponentAt(tabbedPane.indexOfComponent(contentPanel), new ButtonTabComponent(tabbedPane, (i) -> {
            currentlyOpen.remove(tabbedPane.indexOfComponent(contentPanel));
            tabbedPane.remove(tabbedPane.indexOfComponent(contentPanel));
        }));

    }

    private void saveCurrentScript(){
        var editorData = currentlyOpen.get(tabbedPane.getSelectedIndex());
        saveScript(editorData.x, editorData.y.x.get(), editorData.y.y.get());
    }

    private void saveScript(String name, String contents, String imports) {
        try {
            result.setText("Saving file...");
            Files.write(Paths.get(Resource.getAbsoluteFromLocal("resources/scripts/" + name + ".ssf")),
                    Collections.singleton(imports + "~" + contents), StandardCharsets.UTF_8);
            result.setText("Saved file to " + "resources/scripts/" + name + ".ssf");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processCurrentScript(){
        var editorData = currentlyOpen.get(tabbedPane.getSelectedIndex());
        processScript(editorData.x, editorData.y.x.get(), editorData.y.y.get());
    }

    private void processScript(String name, String contents, String imports){
        result.setText("Compiling script...");
        var compilationResults = ScriptCompiler.compileScript(name, imports, contents);
        if(!compilationResults.isSuccess()){
            var error = compilationResults.getDiagnostics().stream()
                    .filter(d -> d.getKind() == Diagnostic.Kind.ERROR)
                    .map(d -> d.getKind() + "\nAt line " + d.getLineNumber() + ": " + d.getMessage(null))
                    .collect(Collectors.joining("\nERROR: \n"));
            JOptionPane.showMessageDialog(this, error);
            result.setText("Compilation failed");
        }else{
            saveScript(name, contents, imports);
            result.setText("Compiled and saved script to " + compilationResults.getResultLocation().replace(Resource.getAbsoluteFromLocal(""), ""));
        }
    }
}


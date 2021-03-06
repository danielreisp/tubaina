package br.com.caelum.tubaina.parser.html.desktop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.com.caelum.tubaina.parser.pygments.CodeCache;
import br.com.caelum.tubaina.parser.pygments.CodeOutputType;
import br.com.caelum.tubaina.util.CommandExecutor;

import com.google.inject.Inject;

public class SyntaxHighlighter {

    private final CommandExecutor commandExecutor;
    private CodeOutputType output;
    
    public static final String HTML_OUTPUT = "html";
    public static final String LATEX_OUTPUT = "latex";
    
    private CodeCache codeCache;

    @Inject
    public SyntaxHighlighter(CommandExecutor commandExecutor, CodeOutputType outputType, CodeCache codeCache) {
        this.commandExecutor = commandExecutor;
        this.output = outputType;
        this.codeCache = codeCache;
    }

    public String highlight(String code, String language, boolean numbered, List<Integer> lines, String pygmentsOptions) {
        String codeAndOptions = code + numbered;
		if (codeCache.exists(codeAndOptions)) {
            return codeCache.find(codeAndOptions);
        }
        ArrayList<String> commandWithArgs = buildCommand(language, numbered, lines, pygmentsOptions);
        String codeHighlighted = commandExecutor.execute(commandWithArgs, code);
        
        codeCache.write(codeAndOptions, codeHighlighted);
        return codeHighlighted;
    }

    public String highlight(String code, String language, boolean numbered, String pygmentsOptions) {
    	List<Integer> list = Collections.emptyList();
    	return highlight(code, language, numbered, list, pygmentsOptions);
    }
    
    private ArrayList<String> buildCommand(String language, boolean numbered, List<Integer> lines, String pygmentsOptions) {
        StringBuilder options = new StringBuilder();
        pygmentsOptions = pygmentsOptions.isEmpty() ? "" : "," + pygmentsOptions;
        options.append(pygmentsOptions);
        if (numbered || output.equals(CodeOutputType.KINDLE_HTML)) { // for kindle output all lines are numbered
            appendLineNumberingOption(options);
        }
        
        addLineHighlightOption(lines, options);
        
        String encoding = System.getProperty("file.encoding");
        
        ArrayList<String> commands = new ArrayList<String>();
        commands.add("pygmentize");
        commands.add("-O");
        commands.add("encoding=" + encoding + ",outencoding=UTF-8" + options);
        if (output.equals(CodeOutputType.LATEX)) {
            commands.add("-P");
            commands.add("verboptions=numbersep=5pt");
        }
        commands.add("-f");
        commands.add(output.pygmentsFormatterName());
        commands.add("-l");
        commands.add(language);
        return commands;
    }

    private void appendLineNumberingOption(StringBuilder options) {
        if (output.equals(CodeOutputType.HTML) || output.equals(CodeOutputType.KINDLE_HTML)) {
            options.append(",linenos=inline");
        } else {
            options.append(",linenos=yes");
        }
        
    }

    private void addLineHighlightOption(List<Integer> lines, StringBuilder options) {
        if (!lines.isEmpty()) {
            options.append(",hl_lines=");
            for (Integer line : lines) {
                options.append(line.toString());
                options.append(" ");
            }
            options.deleteCharAt(options.length() - 1);
            options.append("");
        }
    }

}

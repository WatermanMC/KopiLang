import javax.tools.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class KopiLang {
    private final String RESET = "\u001B[0m";
    private final String RED = "\u001B[31m";

    static void main(String[] args) {
        try {
            new KopiLang().run(args);
        } catch (IOException e) {
            System.err.println("Fatal I/O Error: " + e.getMessage());
            System.exit(1);
        }
    }

    public void run(String[] args) throws IOException {
        if (args.length == 0) {
            printUsage();
            System.exit(1);
        }

        String GREEN = "\u001B[32m";
        switch (args[0]) {
            case "-java" -> {
                if (args.length != 2 || !args[1].endsWith(".kpi")) {
                    System.err.printf("%s'-java' flag requires exactly one '.kpi' file.%s%n", RED, RESET);
                    printUsage();
                    System.exit(1);
                }
                Path kpiPath = Paths.get(args[1]);
                Path javaFile = javaPath(kpiPath);
                Files.writeString(javaFile, transpile(Files.readString(kpiPath)));
                System.out.printf("%sGenerated: " + javaFile + "%s%n", GREEN, RESET);
            }

            case "-mass" -> {
                if (args.length < 3) {
                    System.err.printf("%sError: -mass requires at least 2 '.kpi' file.%s%n", RED, RESET);
                    printUsage();
                    System.exit(1);
                }
                for (int i = 2; i < args.length; i++) {
                    String file = args[i].replaceAll(",+$", "").trim();
                    if (file.isEmpty()) continue;
                    if (!file.endsWith(".kpi")) {
                        continue;
                    }
                    Path kpiPath = Paths.get(file);
                    Path javaFile = javaPath(kpiPath);
                    Files.writeString(javaFile, transpile(Files.readString(kpiPath)));
                    System.out.printf("%sGenerated: " + javaFile + "%s%n", GREEN, RESET);
                }
            }

            default -> {
                if (!args[0].endsWith(".kpi")) {
                    System.err.printf("%sError: Unknown flag or non .kpi file: " + args[0] + "%s%n" + RED, RESET);
                    printUsage();
                    System.exit(1);
                }
                Path kpiPath = Paths.get(args[0]);
                String baseName = kpiPath.getFileName().toString().replace(".kpi", "");
                Path javaFile = javaPath(kpiPath);
                Files.writeString(javaFile, transpile(Files.readString(kpiPath)));
                boolean success = compile(javaFile);
                if (success) {
                    System.out.printf("%sCompiled: %s.class%s%n", GREEN, baseName, RESET);
                    Files.deleteIfExists(javaFile);
                } else {
                    System.err.printf("%n%sCompilation failed. Please check your syntax.%s%n", RED, RESET);
                    Files.deleteIfExists(javaFile);
                    System.exit(1);
                }
            }
        }
    }

    /** Resolves the sibling .java path for a given .kpi path. */
    private Path javaPath(Path kpiPath) {
        return kpiPath.resolveSibling(kpiPath.getFileName().toString().replace(".kpi", ".java"));
    }

    private void printUsage() {
        System.out.println("""
            Usage:
                  Compile to .class:    java -jar KopiLang.jar <file.kpi>
                  Generate .java only:  java -jar KopiLang.jar -java <file.kpi>
                  Generate many .java:  java -jar KopiLang.jar -mass file1.kpi, file2.kpi, path/to/file3.kpi
            """);
    }

    String transpile(String input) {
        StringBuilder out = new StringBuilder();
        String[] lines = input.split("\\r?\\n");

        for (String rawLine : lines) {
            String indent = rawLine.replaceFirst("^(\\s*).*", "$1");
            String line = rawLine.stripLeading();

            if (line.isEmpty()) {
                out.append('\n');
                continue;
            }

            line = stripComments(line);
            line = replaceKeywords(line);
            line = handleControlFlow(line);
            line = handleArrows(line);
            line = handleVoidMethod(line);
            line = addSemicolons(line);

            out.append(indent).append(line).append('\n');
        }
        String javaCode = out.toString();
        javaCode = autoImport(javaCode);
        return javaCode;
    }

    private String stripComments(String line) {
        boolean inString = false;
        int sharpPos = -1;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"' && (i == 0 || line.charAt(i-1) != '\\')) {
                inString = !inString;
            }
            if (c == '#' && !inString) {
                sharpPos = i;
                break;
            }
        }
        if (sharpPos >= 0) {
            String before = line.substring(0, sharpPos);
            if (before.trim().isEmpty()) return "";
            return before;
        }
        return line;
    }

    private String replaceKeywords(String line) {
        var keywords = keywordMapper();
        StringBuilder result = new StringBuilder();
        boolean inString = false;
        int i = 0;
        while (i < line.length()) {
            char c = line.charAt(i);
            if (c == '"' && (i == 0 || line.charAt(i-1) != '\\')) {
                inString = !inString;
                result.append(c);
                i++;
                continue;
            }
            if (!inString && Character.isLetter(c)) {
                boolean matched = false;
                for (Map.Entry<String, String> entry : keywords.entrySet()) {
                    Pattern p = Pattern.compile(entry.getKey());
                    Matcher m = p.matcher(line.substring(i));
                    if (m.lookingAt()) {
                        result.append(entry.getValue());
                        i += m.group().length();
                        matched = true;
                        break;
                    }
                }
                if (matched) continue;
            }
            result.append(c);
            i++;
        }
        return result.toString();
    }

    private static Map<String, String> keywordMapper() {
        Map<String, String> keywords = new LinkedHashMap<>();
        // Add your own mappings like:
        // keywords.put("\\bYOUR KEYWORD HERE\\b", "EXISTING JAVA KEYWORD");
        keywords.put("\\bpkg\\b", "package");
        keywords.put("\\bpub\\b", "public");
        keywords.put("\\bpriv\\b", "private");
        keywords.put("\\bext\\b", "extends");
        keywords.put("\\bimpl\\b", "implements");
        keywords.put("\\bstc\\b", "static");
        keywords.put("\\bprintLn\\b", "System.out.println");
        keywords.put("\\bprint\\b", "System.out.print");
        keywords.put("\\bprintForm\\b", "System.out.printf");
        return keywords;
    }

    private String handleControlFlow(String line) {
        Matcher elifM = Pattern.compile("^(}\\s*)elif\\s+").matcher(line);
        if (elifM.find()) {
            String prefix = elifM.group(1);
            String rest = line.substring(elifM.end());
            if (!rest.trim().endsWith("{")) return line;
            String condition = rest.trim();
            condition = condition.substring(0, condition.length() - 1).trim();
            if (condition.startsWith("(") && condition.endsWith(")")) {
                return prefix + "else if " + condition + " {";
            }
            return prefix + "else if (" + condition + ") {";
        }

        Matcher m = Pattern.compile("^(if|while|switch|for)\\s+").matcher(line);
        if (!m.find()) return line;

        String keyword = m.group(1);
        String rest = line.substring(m.end());

        if (!rest.trim().endsWith("{")) return line;

        String condition = rest.trim();
        condition = condition.substring(0, condition.length() - 1).trim();

        if (condition.startsWith("(") && condition.endsWith(")")) return line;

        return keyword + " (" + condition + ") {";
    }

    private String handleArrows(String line) {
        int arrowIdx = line.indexOf("->");
        if (arrowIdx == -1) return line;

        int braceIdx = line.indexOf('{', arrowIdx);
        if (braceIdx == -1) return line;

        String beforeArrow = line.substring(0, arrowIdx).trim();
        String afterArrow = line.substring(arrowIdx + 2, braceIdx).trim();
        String rest = line.substring(braceIdx);

        int openParen = beforeArrow.indexOf('(');
        if (openParen == -1) return line;
        String methodNamePart = beforeArrow.substring(0, openParen);
        String paramsPart = beforeArrow.substring(openParen);

        String[] tokens = methodNamePart.split("\\s+");
        String methodName = tokens[tokens.length - 1];
        String modifiers = methodNamePart.substring(0, methodNamePart.lastIndexOf(methodName)).trim();

        return (modifiers.isEmpty() ? "" : modifiers + " ")
                + afterArrow + " " + methodName + paramsPart + " " + rest;
    }

    private String autoImport(String javaCode) {
        StringBuilder result = new StringBuilder();
        String[] lines = javaCode.split("\\r?\\n", -1);
        boolean inserted = false;

        for (String line : lines) {
            result.append(line).append('\n');
            if (!inserted && line.trim().startsWith("package ")) {
                result.append("import java.util.*;\n");
                result.append("import java.io.*;\n");
                result.append("import java.nio.file.*;\n");
                result.append("import java.time.*;\n");
                result.append("import java.util.stream.*;\n");
                result.append("import java.math.*;\n");
                inserted = true;
            }
        }

        if (!inserted) {
            return "import java.util.*;\n" +
                    "import java.io.*;\n" +
                    "import java.nio.file.*;\n" +
                    "import java.time.*;\n" +
                    "import java.net.*;\n" +
                    "import java.math.*;\n" +
                    "import java.util.stream.*;\n\n" + javaCode;
        }
        return result.toString();
    }

    private String handleVoidMethod(String line) {
        if (line.contains("->")) return line;
        Matcher m = Pattern.compile(
                "^(public|private|protected)?(\\s+static)?(\\s+final)?\\s+(\\w+)\\s*\\([^)]*\\)\\s*\\{"
        ).matcher(line);
        if (m.find()) {
            String methodName = m.group(4);
            if (Character.isUpperCase(methodName.charAt(0))) return line;

            var modifiers = new StringBuilder();
            if (m.group(1) != null) modifiers.append(m.group(1));
            if (m.group(2) != null) modifiers.append(m.group(2));
            if (m.group(3) != null) modifiers.append(m.group(3));
            String paramsAndBrace = line.substring(m.start(4) + methodName.length());
            String newLine = modifiers.toString().trim();
            if (!newLine.isEmpty()) newLine += " ";
            newLine += "void " + methodName + paramsAndBrace;
            return newLine;
        }
        return line;
    }

    private String addSemicolons(String line) {
        String trimmed = line.trim();
        boolean noSemicolon = trimmed.endsWith("{") || trimmed.endsWith("}") ||
                trimmed.endsWith(":") || trimmed.startsWith("@") ||
                trimmed.matches("^case\\s+.*:") ||
                trimmed.matches("^default\\s*:");

        if (trimmed.matches("^(if|for|while|switch)\\s*\\(.*\\)\\s*\\{$")) {
            noSemicolon = true;
        }
        if (trimmed.matches(".*\\)\\s*\\{") && !trimmed.contains(";")) {
            noSemicolon = true;
        }

        if (!noSemicolon && !trimmed.endsWith(";")) {
            return line + ";";
        }
        return line;
    }

    boolean compile(Path javaFile) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            System.err.printf("%sJDK compiler not found. Run with a JDK, not a JRE%s%n", RED, RESET);
            return false;
        }
        try (StandardJavaFileManager fm = compiler.getStandardFileManager(null, null, null)) {
            Iterable<? extends JavaFileObject> units = fm.getJavaFileObjects(javaFile.toFile());
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            boolean success = compiler.getTask(null, fm, diagnostics, null, null, units).call();
            if (!success) {
                for (Diagnostic<?> d : diagnostics.getDiagnostics()) {
                    System.err.printf("%s%s%s%n", RED, d, RESET);
                }
            }
            return success;
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }
}
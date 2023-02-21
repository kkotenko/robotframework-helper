package com.github.nghiatm.robotframeworkplugin.ide;

import com.github.nghiatm.robotframeworkplugin.ide.config.RobotOptionsProvider;
import com.github.nghiatm.robotframeworkplugin.psi.RecommendationWord;
import com.github.nghiatm.robotframeworkplugin.psi.RobotElementType;
import com.github.nghiatm.robotframeworkplugin.psi.RobotKeywordProvider;
import com.github.nghiatm.robotframeworkplugin.psi.RobotTokenTypes;
import com.github.nghiatm.robotframeworkplugin.psi.dto.ImportType;
import com.github.nghiatm.robotframeworkplugin.psi.element.DefinedKeyword;
import com.github.nghiatm.robotframeworkplugin.psi.element.DefinedVariable;
import com.github.nghiatm.robotframeworkplugin.psi.element.Heading;
import com.github.nghiatm.robotframeworkplugin.psi.element.KeywordFile;
import com.github.nghiatm.robotframeworkplugin.psi.element.RobotFile;
import com.intellij.codeInsight.TailType;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.lookup.TailTypeDecorator;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ProcessingContext;
import org.apache.commons.lang.WordUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Arrays;
import java.util.Collection;

import static com.intellij.patterns.PlatformPatterns.psiElement;

/**
 * @author Stephen Abrams
 */
public class RobotCompletionContributor extends CompletionContributor {

    private static final TailType NEW_LINE = TailType.createSimpleTailType('\n');
    private static final TailType SUPER_SPACE = new TailType() {
        @Override
        public int processTail(Editor editor, int tailOffset) {
            Document document = editor.getDocument();
            int textLength = document.getTextLength();
            CharSequence chars = document.getCharsSequence();
            if (tailOffset < textLength - 1 && chars.charAt(tailOffset) == ' ' && chars.charAt(tailOffset + 1) == ' ') {
                // if we already have the two spaces then move the caret to after them
                return moveCaret(editor, tailOffset, 2);
            } else if (tailOffset < textLength && chars.charAt(tailOffset) == ' ') {
                // if we only have one space then add the second and move the caret after both
                document.insertString(tailOffset, " ");
                return moveCaret(editor, tailOffset, 2);
            } else {
                // if there are not spaces then add two and move the caret after them
                document.insertString(tailOffset, "  ");
                return moveCaret(editor, tailOffset, 2);
            }
        }
    };

    public RobotCompletionContributor() {
        // This is the rule for adding Headings (*** Settings ***, *** Test Cases ***)
        extend(CompletionType.BASIC,
                psiElement().inFile(PlatformPatterns.psiElement(RobotFile.class)),
                new CompletionProvider<CompletionParameters>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters,
                                                  @NotNull ProcessingContext context,
                                                  @NotNull CompletionResultSet results) {
                        addSyntaxLookup(RobotTokenTypes.HEADING, results, NEW_LINE);
                    }
                });
        // This is the rule for adding Bracket Settings ([Tags], [Setup])
        extend(CompletionType.BASIC,
                psiElement().inFile(psiElement(RobotFile.class)),
                new CompletionProvider<CompletionParameters>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters,
                                                  @NotNull ProcessingContext context,
                                                  @NotNull CompletionResultSet results) {
                        // TODO: some brackets are only for Test Cases, some only Keywords, some both
                        PsiElement heading = getHeading(parameters.getOriginalPosition());
                        if (isInTestCases(heading) || isInKeywords(heading)) {
                            addSyntaxLookup(RobotTokenTypes.BRACKET_SETTING, results, SUPER_SPACE);
                        }
                    }
                });
        // This is the rule for adding settings and imports (Library, Test Setup)
        extend(CompletionType.BASIC,
                psiElement().inFile(psiElement(RobotFile.class)),
                new CompletionProvider<CompletionParameters>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters,
                                                  @NotNull ProcessingContext context,
                                                  @NotNull CompletionResultSet results) {
                        PsiElement heading = getHeading(parameters.getOriginalPosition());
                        if (isInSettings(heading)) {
                            addSyntaxLookup(RobotTokenTypes.SETTING, results, SUPER_SPACE);
                            addSyntaxLookup(RobotTokenTypes.IMPORT, results, SUPER_SPACE);
                        }
                    }
                });
        // This is the rule for adding Gherkin (When, Then)
        extend(CompletionType.BASIC,
                psiElement().inFile(psiElement(RobotFile.class)),
                new CompletionProvider<CompletionParameters>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters,
                                                  @NotNull ProcessingContext context,
                                                  @NotNull CompletionResultSet results) {
                        PsiElement heading = getHeading(parameters.getOriginalPosition());
                        if (isInTestCases(heading)) {
                            addSyntaxLookup(RobotTokenTypes.GHERKIN, results, TailType.SPACE);
                        }
                    }
                });

        // This is the rule for adding syntax marker (IF, END)
        extend(CompletionType.BASIC,
                psiElement().inFile(psiElement(RobotFile.class)),
                new CompletionProvider<CompletionParameters>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters,
                                                  @NotNull ProcessingContext context,
                                                  @NotNull CompletionResultSet results) {
                        PsiElement heading = getHeading(parameters.getOriginalPosition());
                        if (isInTestCases(heading) || isInKeywords(heading)) {
                            addSyntaxLookup(RobotTokenTypes.SYNTAX_MARKER, results, SUPER_SPACE);
                        }
                    }
                });

        // This is the rule for adding imported keywords and library methods
        extend(CompletionType.BASIC,
                psiElement().inFile(psiElement(RobotFile.class)),
                new CompletionProvider<CompletionParameters>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters,
                                                  @NotNull ProcessingContext context,
                                                  @NotNull CompletionResultSet result) {
                        PsiElement heading = getHeading(parameters.getOriginalPosition());
                        if (isInTestCases(heading) || isInKeywords(heading)) {
                            addRobotKeywords(result, parameters.getOriginalFile());
                        }
                    }
                });
        // This is the rule for adding included variable definitions
        // TODO: include variables defined in the current statement
        extend(CompletionType.BASIC,
                psiElement().inFile(psiElement(RobotFile.class)),
                new CompletionProvider<CompletionParameters>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters,
                                                  @NotNull ProcessingContext context,
                                                  @NotNull CompletionResultSet result) {
                        PsiElement heading = getHeading(parameters.getOriginalPosition());
                        if (isInTestCases(heading) || isInKeywords(heading) || isInSettings(heading)) {
                            addRobotVariables(result, parameters.getOriginalFile(), parameters.getOriginalPosition());
                        }
                    }
                });
    }

    private static PsiElement getHeading(PsiElement current) {
        if (current == null) {
            return null;
        }
        if (current instanceof Heading) {
            return current;
        } else {
            return getHeading(current.getParent());
        }
    }

    private static boolean isInSettings(@Nullable PsiElement element) {
        boolean result = false;
        if (element instanceof Heading) {
            result = ((Heading) element).isSettings();
        }
        return result;
    }

    private static boolean isInTestCases(@Nullable PsiElement element) {
        boolean result = false;
        if (element instanceof Heading) {
            result = ((Heading) element).containsTestCases();
        }
        return result;
    }

    private static boolean isInKeywords(@Nullable PsiElement element) {
        boolean result = false;
        if (element instanceof Heading) {
            result = ((Heading) element).containsKeywordDefinitions();
        }
        return result;
    }

    private static void addRobotKeywords(CompletionResultSet result, PsiFile file) {
        if (!(file instanceof RobotFile)) {
            return;
        }
        RobotFile robotFile = (RobotFile) file;

        boolean capitalize = RobotOptionsProvider.getInstance(robotFile.getProject()).capitalizeKeywords();
        addKeywordsToResult(robotFile.getDefinedKeywords(), result, capitalize);

        boolean includeTransitive = RobotOptionsProvider.getInstance(file.getProject()).allowTransitiveImports();
        Collection<KeywordFile> importedFiles = robotFile.getImportedFiles(includeTransitive);
        for (KeywordFile f : importedFiles) {
            if (f.getImportType() != ImportType.VARIABLES) {
                addKeywordsToResult(f.getDefinedKeywords(), result, capitalize);
            }
        }
    }

    private static void addRobotVariables(@NotNull CompletionResultSet result, @NotNull PsiFile file, @Nullable PsiElement position) {
        if (!(file instanceof RobotFile)) {
            return;
        }
        RobotFile robotFile = (RobotFile) file;
        addVariablesToResult(robotFile.getDefinedVariables(), result, position);

        boolean includeTransitive = RobotOptionsProvider.getInstance(file.getProject()).allowTransitiveImports();
        Collection<KeywordFile> importedFiles = robotFile.getImportedFiles(includeTransitive);
        for (KeywordFile f : importedFiles) {
            if (f.getImportType() == ImportType.VARIABLES) {
                addVariablesToResult(f.getDefinedVariables(), result, position);
            }
        }
    }

    private static void addVariablesToResult(@NotNull final Collection<DefinedVariable> variables,
                                             @NotNull final CompletionResultSet result,
                                             @Nullable PsiElement position) {
        for (DefinedVariable variable : variables) {
            if (!variable.isInScope(position)) {
                continue;
            }
            String text = variable.getLookup();
            if (text != null) {
                // we only want the first word of the variable
                String[] words = text.split("\\s+");
                String lookupString = words.length > 0 ? words[0] : text;
                String[] lookupStrings = {text, WordUtils.capitalize(text),
                        text.toLowerCase(),
                        lookupString, lookupString.toLowerCase()};
                LookupElement element = TailTypeDecorator.withTail(
                        LookupElementBuilder.create(lookupString)
                                .withLookupStrings(Arrays.asList(lookupStrings))
                                .withPresentableText(lookupString)
                                .withCaseSensitivity(true),
                        TailType.NONE);
                result.addElement(element);
            }
        }
    }

    private static void addKeywordsToResult(final Collection<DefinedKeyword> keywords,
                                            final CompletionResultSet result,
                                            boolean capitalize) {
        for (DefinedKeyword keyword : keywords) {
            String text = keyword.getKeywordName();
            String lookupString = capitalize ? WordUtils.capitalize(text) : text;
            String[] lookupStrings = {text, WordUtils.capitalize(text), text.toLowerCase()};
            LookupElement element = TailTypeDecorator.withTail(
                    LookupElementBuilder.create(lookupString)
                            .withLookupStrings(Arrays.asList(lookupStrings))
                            .withPresentableText(lookupString)
                            .withCaseSensitivity(true),
                    keyword.hasArguments() ? SUPER_SPACE : TailType.NONE);
            result.addElement(element);
        }
    }

    private static void addSyntaxLookup(@NotNull RobotElementType type, @NotNull CompletionResultSet results, @NotNull TailType tail) {
        Collection<RecommendationWord> words = RobotKeywordProvider.getInstance().getRecommendationsForType(type);
        for (RecommendationWord word : words) {
            String text = word.getLookup();
            String lookupString = word.getPresentation();
            String[] lookupStrings = {text, WordUtils.capitalize(text),
                    lookupString, WordUtils.capitalize(lookupString),
                    lookupString.toLowerCase()};
            LookupElement element = TailTypeDecorator.withTail(
                    LookupElementBuilder.create(lookupString)
                            .withLookupStrings(Arrays.asList(lookupStrings))
                            .withPresentableText(lookupString)
                            .withCaseSensitivity(true),
                    tail);
            results.addElement(element);
        }
    }

    @Override
    public void fillCompletionVariants(@NotNull final CompletionParameters parameters, @NotNull CompletionResultSet result) {
        // debugging point
        super.fillCompletionVariants(parameters, result);
    }
}
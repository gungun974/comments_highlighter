# Comments Highlighter

This plugin allows creating custom highlighting for commented lines and language keyword. Commented lines highlighting
is supported for any language, highlighting of language keywords supports Java, Kotlin, PHP, C/C++/ObjectiveC and C# (
for example, highlight "public" keyword). Plugin has possibility to define custom user tokens for commented line
highlighting.

## How to add comment highlighting token or language keyword

+ Open settings window
+ Editor->Comment Highlighter
+ Click on "+" sign, select type of highlight token/keyword and enter token/keyword itself in popup. (Third screenshot
  of plugin)
+ Click "Apply"
+ **Reopen settings window.** This is mandatory step due
  to [bug in Intellij](https://youtrack.jetbrains.com/issue/IDEA-226087)

## How to enable comment highlighting in plain text files

+ Open settings window
+ Editor->Comment Highlighter
+ Enable checkbox "Enable comment highlighting in Plain text files."
+ Click "Apply"

In plain text files could be used "#" or "//" as a comment line.

## Implementation notes

General architecture is simple and has 2 main parts:

+ Annotators:
  + **AbstractCommentHighlighterAnnotator**
    abstract [Annotator](https://www.jetbrains.org/intellij/sdk/docs/reference_guide/custom_language_support/syntax_highlighting_and_error_highlighting.html#annotator)
    to highlight comments defined in Intellij AST.
    + **GenericCommentHighlighterAnnotator** highlights PsiComment type. In such way plugin is able to handle any
      language, supported by Intellij IDEA.
  + **AbstractKeywordHighlighterAnnotator**
    abstract [Annotator](https://www.jetbrains.org/intellij/sdk/docs/reference_guide/custom_language_support/syntax_highlighting_and_error_highlighting.html#annotator)
    to highlight keywords defined in Intellij AST.
    + Each language requires specific implementation of _isKeywordElement_ and _isMethodAccessModifierKeyword_.
    + _findClassByName_ is used instead of _instanceof_ comparing to be able to compile plugin on any type of IDE(
      PyCharm,Rider etc).
    + Language specific implementations:
      + **JavaKeywordHighlighterAnnotator**
      + **KotlinKeywordHighlighterAnnotator**
      + **CKeywordHighlighterAnnotator**
      + **CSharpKeywordHighlighterAnnotator**
      + **PHPKeywordHighlighterAnnotator**
  + **AbstractLexerPostProcessingHighlighterAnnotator**
    abstract [Annotator](https://www.jetbrains.org/intellij/sdk/docs/reference_guide/custom_language_support/syntax_highlighting_and_error_highlighting.html#annotator)
    to highlight both comments and keywords defined in Lexer segments. Used for files, which have only lexer highlighting, for example C++ files in Rider.
    + **CPPLexerPostProcessingHighlighterAnnotator** highlights CppElementType comment and keyword tokens in CPPFile type.
+ Highlighters:
  + **CommentHighlighter** main class to define, what part of comment must be highlighted.
  + **KeywordHighlighter** main class to define, what keywords must be highlighted.

## Version history

### 2.4

+ **(Feature)** Add support of Lexer based highlighting, which is used in Rider to highlight C++ files.
+ **(Feature)** Add support for java tokens highlighting (null, true, false).

### 2.3.2

+ **(Bug Fix)** Fix comment highlighting for tokens, which contains other tokens.

### 2.3.1

+ **(Bug Fix)** Fix comment highlighting for Twig and Blade templates.

### 2.3

+ **(Bug Fix)** Add tab sign as valid start line char.
+ **(Feature)** Add possibility to highlight PHP keywords.

### 2.2

+ **(Feature)** Add possibility to highlight comments in plain txt files.
+ **(Feature)** Add keyword highlighting for C/C++/ObjectiveC.
+ **(Feature)** Add possibility to highlight Kotlin keywords.
+ **(Improvement)** Migrate on newest gradle.
+ **(Improvement)** Move settings into panel into "Editor" section.

### 2.1

+ **(Feature)** Add keyword highlighting for C#.
+ **(Feature)** Add possibility to remove/modify default tokens.
+ **(Feature)** Add possibility to highlight any keyword type, not only method keywords.

### 2.0

+ **(Feature)** Add possibility to create custom highlighting for java keyword.
+ **(Feature)** Add default highlight for public keyword.
+ **(Improvement)** Migrate on newest gradle.

### 1.3.6

+ **(Improvement)** Migrate on java 11 toolchain.

### 1.3.5

+ **(Improvement)** Migrate on non-deprecated api usage.
+ **(Improvement)** Disable until version plugin definition.

### 1.3.4

+ **(Bug Fix)** Fix StringIndexOutOfBoundsException on Rider IDE.

### 1.3.3

+ **(Bug Fix)** Made compatible with EAP builds.

### 1.3.2
+ **(Documentation)** Add how-to define custom user token.

### 1.3.1
+ **(Feature)** Add possibility to create custom tokens.
+ **(Bug Fix)** Define plugin compatibility starting from version 2019.1.

### 1.2
+ **(Feature)** Improve performance.
+ **(Feature)** Change default color scheme.

### 1.1
+ **(Bug Fix)** Fix fail positive highlight of xml comments.

### 1.0
+ **(Feature)** Add support for one line, multi line and doc comments highlighting.

package com.memory.app.util;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

public class MarkdownUtils {
    private static final Parser PARSER = Parser.builder().build();
    private static final HtmlRenderer RENDERER = HtmlRenderer.builder().build();
    private static final PolicyFactory SANITIZER_POLICY = new HtmlPolicyBuilder()
            .allowCommonBlockElements()
            .allowCommonInlineFormattingElements()
            .allowElements("a", "img")
            .allowAttributes("href", "title").onElements("a")
            .allowAttributes("src", "alt", "title").onElements("img")
            .allowStandardUrlProtocols()
            .toFactory();

    public static String convertToHtml(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return "";
        }
        Node document = PARSER.parse(markdown);
        String html = RENDERER.render(document);
        return SANITIZER_POLICY.sanitize(html);
    }
}
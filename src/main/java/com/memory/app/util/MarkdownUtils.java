package com.memory.app.util;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.renderer.html.AttributeProvider;
import org.commonmark.renderer.html.AttributeProviderContext;
import org.commonmark.renderer.html.AttributeProviderFactory;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.IndentedCodeBlock;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.task.list.items.TaskListItemsExtension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Pattern;

public class MarkdownUtils {
    // 启用Markdown扩展：表格、任务列表、删除线、自动链接
    private static final Parser PARSER = Parser.builder()
            .extensions(Arrays.asList(
                    TablesExtension.create(),
                    TaskListItemsExtension.create(),
                    StrikethroughExtension.create(),
                    AutolinkExtension.create()
            ))
            .build();
    
    // 自定义属性提供器，为代码块添加语言类名
    private static final AttributeProviderFactory ATTRIBUTE_PROVIDER_FACTORY = new AttributeProviderFactory() {
        @Override
        public AttributeProvider create(AttributeProviderContext context) {
            return new AttributeProvider() {
                @Override
                public void setAttributes(Node node, String tagName, Map<String, String> attributes) {
                    if (node instanceof FencedCodeBlock) {
                        FencedCodeBlock codeBlock = (FencedCodeBlock) node;
                        String info = codeBlock.getInfo();
                        if (info != null && !info.trim().isEmpty()) {
                            // 添加语言类名，用于代码高亮
                            String language = info.trim().split("\\s+")[0];
                            attributes.put("class", "language-" + language);
                        } else {
                            attributes.put("class", "language-text");
                        }
                    } else if (node instanceof IndentedCodeBlock) {
                        attributes.put("class", "language-text");
                    }
                }
            };
        }
    };
    
    private static final HtmlRenderer RENDERER = HtmlRenderer.builder()
            .extensions(Arrays.asList(
                    TablesExtension.create(),
                    TaskListItemsExtension.create(),
                    StrikethroughExtension.create(),
                    AutolinkExtension.create()
            ))
            .attributeProviderFactory(ATTRIBUTE_PROVIDER_FACTORY)
            .build();
    
    // 更宽松的HTML清理策略，支持表格、任务列表等扩展Markdown语法
    private static final PolicyFactory SANITIZER_POLICY = new HtmlPolicyBuilder()
            .allowCommonBlockElements()
            .allowCommonInlineFormattingElements()
            // 允许更多的HTML元素
            .allowElements("a", "img", "pre", "code", "div", "span", "section", "article", "aside", "nav", "header", "footer", "main")
            // 允许表格相关元素
            .allowElements("table", "thead", "tbody", "tfoot", "tr", "th", "td", "caption", "colgroup", "col")
            // 允许任务列表相关元素
            .allowElements("input")
            // 允许更多的属性
            .allowAttributes("href", "title", "target", "rel").onElements("a")
            .allowAttributes("src", "alt", "title", "width", "height").onElements("img")
            .allowAttributes("type", "checked", "disabled").onElements("input")
            .allowAttributes("class", "id", "style").matching(Pattern.compile(".*")).globally()
            .allowAttributes("data-*").matching(Pattern.compile(".*")).globally()
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
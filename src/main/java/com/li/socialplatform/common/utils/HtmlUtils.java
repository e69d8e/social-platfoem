package com.li.socialplatform.common.utils;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.apache.commons.lang3.StringUtils;

public class HtmlUtils {
    /**
     * 将 HTML 安全地转换为纯文本，并尽可能保留段落和换行。
     * @param html 原始 HTML 字符串
     * @return 清洗并转换后的纯文本
     */
    public static String htmlToPlainText(String html) {
        if (StringUtils.isBlank(html)) {
            return "";
        }

        // step 1: 使用扩展的白名单进行清洗，保留安全标签，这是XSS防御的核心
        Safelist safeList = Safelist.basic()
                .addTags("p", "br", "div", "h2", "h3", "pre") // 添加你需要的其他块级标签
                .addAttributes("a", "href") // 如果你需要保留链接文本，而非链接地址
                .addEnforcedAttribute("a", "rel", "nofollow"); // 为所有链接强制添加 rel=nofollow 属性，提升安全性

        String safeHtml = Jsoup.clean(html, safeList);

        // step 2: 将安全HTML中的块级标签和换行符替换为真正的换行符
        // 将常见的块级标签或换行标签替换为 \n
        String withNewlines = safeHtml
                .replaceAll("(?i)<\\s*p\\s*/?>", "\n")      // <p> 前后换行
                .replaceAll("(?i)<\\s*br\\s*/?>", "\n")    // <br> 换行
                .replaceAll("(?i)<\\s*div\\s*/?>", "\n")   // <div> 换行
                .replaceAll("(?i)<\\s*/(p|div|h\\d|pre)\\s*>", "\n"); // 闭合标签后换行

        // step 3: 使用 Jsoup 清洗所有剩余的 HTML 标签，提取纯文本
        // 此时，只将换行符作为文本节点保留，其他标签都被移除
        String plainText = Jsoup.clean(withNewlines, "", Safelist.none(), new org.jsoup.nodes.Document.OutputSettings().prettyPrint(false));

        // step 4: 去除多余的重复换行和空格，得到干净的结果
        return plainText.replaceAll("\\n\\s*\\n", "\n").trim();
    }
}
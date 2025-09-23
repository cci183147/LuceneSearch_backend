package com.cci.lucenesearch;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static String extractAbstract(Document doc) {
        NodeList abstractNodes = doc.getElementsByTagName("abstract");

        if (abstractNodes.getLength() == 0) {
            System.out.println("Warning: No abstract found in the document");
            return "";
        }

        // 获取 <abstract> 中的所有 <p> 标签
        Node abstractNode = abstractNodes.item(0); // 获取第一个 <abstract> 节点
        NodeList pNodes = ((Element) abstractNode).getElementsByTagName("p");

        StringBuilder abstractText = new StringBuilder();

        // 遍历所有 <p> 标签并拼接文本内容
        for (int i = 0; i < pNodes.getLength(); i++) {
            Node pNode = pNodes.item(i);
            abstractText.append(pNode.getTextContent().trim()).append("\n");
        }

        return abstractText.toString();

    }
    public static String extractPublicationDate(Document doc) {
        NodeList dateNodes = doc.getElementsByTagName("date");

        for (int i = 0; i < dateNodes.getLength(); i++) {
            Node dateNode = dateNodes.item(i);

            // 检查date标签是否有type属性为published，并提取日期
            if (dateNode.getAttributes() != null) {
                Node typeAttr = dateNode.getAttributes().getNamedItem("type");

                // 判断type属性是否存在
                if (typeAttr != null && "published".equals(typeAttr.getTextContent())) {
                    // 提取 "when" 属性（日期的标准格式）
                    Node whenAttr = dateNode.getAttributes().getNamedItem("when");


                    return (whenAttr != null) ? normalizeDate(whenAttr.getTextContent()): "1949-01-01";
                }
            }
        }

        return "1949-01-01";  // 如果没有找到日期返回默认值
    }
    public static String normalizeDate(String date) {
        // 定义标准化日期的格式
        String resultDate = "";

        // 1. 检查是否是完整的日期格式 yyyy-MM-dd
        if (date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            resultDate = date; // 已经是标准格式，直接返回
        }
        // 2. 检查是否只有年份 yyyy
        else if (date.matches("\\d{4}")) {
            resultDate = date + "-01-01"; // 只包含年份，默认为01-01
        }
        // 3. 检查是否为 yyyy-MM 格式
        else if (date.matches("\\d{4}-\\d{2}")) {
            resultDate = date + "-01"; // 只包含年份和月份，默认为01号
        }

        // 如果没有匹配任何已知格式，返回原始值（或者报错/返回默认日期）
        if (resultDate.isEmpty()) {
            resultDate = "Invalid Date Format";
        }

        return resultDate;
    }

    // 解析标题
    public static String extractTitle(Document doc) {
        NodeList titleNodes = doc.getElementsByTagName("title");
        for (int i = 0; i < titleNodes.getLength(); i++) {
            Node titleNode = titleNodes.item(i);
            if (titleNode.getAttributes().getNamedItem("type").getTextContent().equals("main")) {
                return titleNode.getTextContent();
            }
        }
        return "";
    }

    // 解析作者
// 解析作者 - 限定在sourceDesc标签下
    public static String extractAuthors(Document doc) {
        List<String> authors = new ArrayList<>();

        // 首先获取sourceDesc节点
        NodeList sourceDescNodes = doc.getElementsByTagName("sourceDesc");

        // 检查是否找到sourceDesc节点
        if (sourceDescNodes.getLength() == 0) {
            System.out.println("Warning: No sourceDesc tag found in the document");
            return ""; // 返回空列表
        }

        // 获取第一个sourceDesc节点下的所有author标签
        Element sourceDesc = (Element) sourceDescNodes.item(0);
        NodeList authorNodes = sourceDesc.getElementsByTagName("author");

        for (int i = 0; i < authorNodes.getLength(); i++) {
            Node authorNode = authorNodes.item(i);
            NodeList persNameNodes = ((Element) authorNode).getElementsByTagName("persName");

            StringBuilder authorName = new StringBuilder();
            for (int j = 0; j < persNameNodes.getLength(); j++) {
                Element persName = (Element) persNameNodes.item(j);

                // 增加空值检查，避免NullPointerException
                Node forenameNode = persName.getElementsByTagName("forename").item(0);
                Node surnameNode = persName.getElementsByTagName("surname").item(0);

                if (forenameNode != null && surnameNode != null) {
                    String firstName = forenameNode.getTextContent();
                    String lastName = surnameNode.getTextContent();
                    authorName.append(firstName).append(" ").append(lastName);
                }
            }

            if (!authorName.isEmpty()) {
                authors.add(authorName.toString());
            }
        }

        return String.join(", ", authors);
    }
}

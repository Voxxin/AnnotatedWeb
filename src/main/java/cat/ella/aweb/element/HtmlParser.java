package cat.ella.aweb.element;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class HtmlParser {
    /**
     * Parses the given HTML string and returns a list of HtmlElement objects representing the parsed content.
     *
     * @param  htmlString  the HTML string to be parsed
     * @return             a list of HtmlElement objects representing the parsed content
     */
    public static List<HtmlElement> parseHtmlString(String htmlString) {
        return parseContent(htmlString);
    }

    /**
     * Parses the given HTML string and returns a list of HtmlElement objects representing the parsed content.
     *
     * @param  html  the HTML string to be parsed
     * @return       a list of HtmlElement objects representing the parsed content
     */
    private static List<HtmlElement> parseContent(String html) {
        List<Tag> tags = new ArrayList<>();
        List<HtmlElement> elements = new ArrayList<>();

        int end = html.length() - 1;
        int layer = 0;

        while (end >= 0) {
            int closingIndex = html.lastIndexOf('>', end);
            int openingIndex = html.lastIndexOf('<', closingIndex);

            if (openingIndex != -1 && closingIndex != -1) {
                String tagString = html.substring(openingIndex + 1, closingIndex);
                boolean isOpeningTag = !tagString.startsWith("/");
                String tagName = isOpeningTag ? tagString.split("\\s+")[0] : tagString.substring(1);
                String attributes = isOpeningTag ? tagString.substring(tagName.length()).trim() : "";
                int rareLayer = html.contains("</" + tagName + ">") ? layer : layer+1;

                tags.add(new Tag(tagName, attributes, isOpeningTag, openingIndex, closingIndex + 1, rareLayer));

                if (isOpeningTag && html.contains("</" + tagName + ">")) {
                    layer--;
                } else if (!isOpeningTag) {
                    layer++;
                }
                end = openingIndex - 1;
            } else {
                break;
            }
        }

        Collections.reverse(tags);

        for (int i = 0; i < tags.size(); i++) {
            Tag tag = tags.get(i);

            if (tag.layer != 1 || !tag.isOpeningTag) continue;

            boolean hasClosingTag = false;
            int sameTagRepeats = 0;
            for (int j = i + 1; j < tags.size(); j++) {
                Tag closingTag = tags.get(j);
                if (closingTag.isOpeningTag && closingTag.tagName.equals(tag.tagName)) {
                    sameTagRepeats++;
                    continue;
                }

                if (!closingTag.isOpeningTag && closingTag.tagName.equals(tag.tagName)) {
                    if (sameTagRepeats > 0) {
                        sameTagRepeats--;
                        continue;
                    }

                    String content = html.substring(tag.closingIndex, closingTag.openingIndex);

                    List<HtmlElement> subElements = parseContent(content);
                    if (subElements.isEmpty()) elements.add(new HtmlElement(tag.tagName).addAttributes(parseAttributes(tag.attributes)).setStringSubElement(content));
                    else elements.add(new HtmlElement(tag.tagName).addAttributes(parseAttributes(tag.attributes)).addSubElements(subElements));

                    hasClosingTag = true;
                    break;
                }
            }

            if (!hasClosingTag) {
                elements.add(new HtmlElement(tag.tagName).addAttributes(parseAttributes(tag.attributes)));
            }
        }


        return elements;
    }

    /**
     * Parses the given attributes string and returns a list of attributes.
     *
     * @param  attributesString  the string containing the attributes
     * @return                   a list of attributes
     */
    private static HashMap<String, List<String>> parseAttributes(String attributesString) {
        HashMap<String, List<String>> attributes = new HashMap<>();

        if (attributesString == null || attributesString.isEmpty()) return attributes;

        Pattern attributePattern = Pattern.compile("(\\w+)\\s*=\\s*\"([^\"]*)\"");
        Matcher matcher = attributePattern.matcher(attributesString);

        while (matcher.find()) {
            String attributeName = matcher.group(1);
            String attributeValues = matcher.group(2);

            List<String> values = Arrays.stream(attributeValues.split("\\s+"))
                    .filter(val -> !val.isEmpty())
                    .collect(Collectors.toList());

            attributes.put(attributeName, values);
        }

        return attributes;
    }

    static class Tag {
        String tagName;
        String attributes;
        boolean isOpeningTag;
        int openingIndex;
        int closingIndex;
        int layer;

        public Tag(String tagName, String attributes, boolean isOpeningTag, int openingIndex, int closingIndex, int layer) {
            this.tagName = tagName;
            this.attributes = attributes;
            this.isOpeningTag = isOpeningTag;
            this.openingIndex = openingIndex;
            this.closingIndex = closingIndex;
            this.layer = layer;
        }
    }
}

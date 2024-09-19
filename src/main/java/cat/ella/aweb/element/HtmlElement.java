package cat.ella.aweb.element;

import java.util.*;

public class HtmlElement {
    private String tagName;
    private Map<String, List<String>> attributes = new HashMap<>();
    private List<HtmlElement> subElements = new ArrayList<>();
    private String subElement = null;

    /**
     * Constructor for HtmlElement with sub-elements.
     *
     * @param tagName     The tag name of the HTML element.
     */
    public HtmlElement(String tagName) {
        this.tagName = tagName;
    }

    public String getTagName() {
        return tagName;
    }

    public HtmlElement addAttributes(String attribute, List<String> values) {
        if (this.attributes.containsKey(attribute)) this.attributes.get(attribute).addAll(values);
        else this.attributes.put(attribute, values);
        return this;
    }

    public HtmlElement addAttributes(HashMap<String, List<String>> values) {
        for(String key : values.keySet()) {
            if (this.attributes.containsKey(key)) this.attributes.get(key).addAll(values.get(key));
            else this.attributes.put(key, values.get(key));
        }
        return this;
    }

    public HtmlElement addAttribute(String attribute,  String value) {
        if (this.attributes.containsKey(attribute)) this.attributes.get(attribute).add(value);
        else this.attributes.put(attribute, List.of(value));
        return this;
    }

    public HtmlElement setAttributes(HashMap<String, List<String>> values) {
        this.attributes = values;
        return this;
    }

    public HtmlElement setAttributes(String attribute,  List<String> values) {
        this.attributes = new HashMap<>();
        this.attributes.put(attribute, values);
        return this;
    }

    public HtmlElement setAttribute(String attribute,  String value) {
        this.attributes = new HashMap<>();
        this.attributes.put(attribute, Collections.singletonList(value));
        return this;
    }


    public List<String> getAttributes(String attribute) {
        return this.attributes.getOrDefault(attribute, Collections.emptyList());
    }

    public String getAttributesAsString(String attribute) {
        return this.attributes.containsKey(attribute) ? String.join(" ", this.attributes.get(attribute)) : "";
    }

    public void removeAttribute(String attribute) {
        this.attributes.remove(attribute);
    }

    public HtmlElement addSubElement(HtmlElement element) {
        this.subElements.add(element);
        this.subElement = null;
        return this;
    }

    public HtmlElement addSubElements(List<HtmlElement> elements) {
        this.subElements.addAll(elements);
        this.subElement = null;
        return this;
    }

    public HtmlElement addSubElements(HtmlElement... elements) {
        this.subElements.addAll(List.of(elements));
        this.subElement = null;
        return this;
    }

    public HtmlElement addSubElementsFromString(String elements) {
        List<HtmlElement> builtElements = HtmlParser.parseHtmlString(elements);
        this.subElements.addAll(builtElements);
        this.subElement = null;
        return this;
    }

    public List<HtmlElement> getSubElements() {
        return Collections.unmodifiableList(this.subElements);
    }

    public String getStringSubElement() {
        return this.subElement;
    }

    public HtmlElement setSubElements(List<HtmlElement> elements) {
        this.subElements = elements != null ? elements : new ArrayList<>();
        this.subElement = null;
        return this;
    }

    public HtmlElement setStringSubElement(String subElement) {
        this.subElement = subElement;
        this.subElements = new ArrayList<>();
        return this;
    }

    public HtmlElement removeSubElement(HtmlElement element) {
        this.subElements.remove(element);
        return this;
    }


    /**
     * Generate the HTML string representation of this HtmlElement.
     *
     * @return The HTML string.
     */
    protected String htmlString() {
        StringBuilder builder = new StringBuilder();

        builder.append("<").append(this.tagName);
        if (this.attributes != null) {
            for (String attributeKey : this.attributes.keySet()) {
                builder.append(" ").append(attributeKey).append("=\"").append(this.getAttributesAsString(attributeKey)).append("\" ");
            }
        }
        builder.append(">");

        if (!this.subElements.isEmpty()) {
            for (HtmlElement element : this.subElements) {
                builder.append("\n  ").append(element.htmlString());
            }
            builder.append("\n");
        } else if (this.subElement != null) {
            builder.append(" ").append(this.subElement);
        }

        builder.append("</").append(this.tagName).append(">");

        return builder.toString();
    }

}
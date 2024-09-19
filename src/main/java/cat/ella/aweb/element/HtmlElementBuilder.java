package cat.ella.aweb.element;

import java.util.ArrayList;
import java.util.List;

public class HtmlElementBuilder {
    private final List<Object> elements;

    /**
     * Constructor for HtmlElementBuilder.
     */
    public HtmlElementBuilder() {
        this.elements = new ArrayList<>();
    }

    /**
     * Add an HtmlElement to the builder.
     *
     * @param element The HtmlElement to add.
     * @return The HtmlElementBuilder instance.
     */
    public HtmlElementBuilder addElement(HtmlElement element) {
        this.elements.add(element);
        return this;
    }

    /**
     * Add a string element to the builder.
     *
     * @param element The string element to add.
     * @return The HtmlElementBuilder instance.
     */
    public HtmlElementBuilder addElement(String element) {
        this.elements.add(element);
        return this;
    }

    /**
     * Add a list of HtmlElement objects to the builder.
     *
     * @param htmlElements The list of HtmlElement objects to add.
     * @return The HtmlElementBuilder instance.
     */
    public HtmlElementBuilder addElements(List<HtmlElement> htmlElements) {
        this.elements.addAll(htmlElements);
        return this;
    }

    /**
     * Build the HTML string from the elements added to the builder.
     *
     * @return The constructed HTML string.
     */
    public String build() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Object obj : elements) {
            stringBuilder.append(obj instanceof HtmlElement ? ((HtmlElement) obj).htmlString() : obj).append("\n");
        }
        return stringBuilder.toString();
    }

}
package thut.core.xml.bind;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.google.common.collect.Maps;

import thut.core.common.ThutCore;
import thut.core.xml.bind.annotation.XmlAnyAttribute;
import thut.core.xml.bind.annotation.XmlAttribute;
import thut.core.xml.bind.annotation.XmlElement;
import thut.core.xml.bind.annotation.XmlRootElement;

public class Factory<T>
{

    private static Map<Class<?>, Map<String, Field>> knownElemMappins = Maps.newHashMap();
    private static Map<Class<?>, Map<String, Field>> knownAttrMappins = Maps.newHashMap();

    private static Map<Class<?>, Field> knownAnyAtrMappins = Maps.newHashMap();

    public static <T> T make(final InputStream stream, final Class<T> clazz) throws Exception
    {
        final SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        final XMLReader xmlReader = spf.newSAXParser().getXMLReader();
        xmlReader.setEntityResolver((publicId, systemId) -> new InputSource(new StringReader("")));
        final InputSource inputSource = new InputSource(new InputStreamReader(stream));
        final SAXSource source = new SAXSource(xmlReader, inputSource);
        return Factory.makeForClass(clazz).make(source);
    }

    public static <T> Factory<T> makeForClass(final Class<T> clazz) throws Exception
    {
        return new Factory<>(clazz);
    }

    final T toFill;

    private Factory(final Class<T> clazz) throws Exception
    {
        this.toFill = clazz.getConstructor().newInstance();
    }

    @SuppressWarnings("unchecked")
    private void apply(final Node n, final Object obj, final int depth, final Field field, final String tabbing)
    {
        final String name = n.getNodeName();
        final String value = n.getNodeValue();
        if (field == null) return;
        final Class<?> fclaz = field.getType();
        // System.out.print(tabbing + "attr=" + name + " val=" + value + "
        // field=" + fclaz + "\n");
        Object obj2;
        try
        {
            obj2 = field.get(obj);
            if (fclaz == String.class) field.set(obj, value);
            else if (fclaz == float.class || fclaz == Float.class || fclaz == double.class || fclaz == Double.class)
                field.set(obj, Float.parseFloat(value));
            else if (fclaz == int.class || fclaz == Integer.class) field.set(obj, Integer.parseInt(value));
            else if (fclaz == byte.class || fclaz == Byte.class) field.set(obj, Byte.parseByte(value));
            else if (fclaz == boolean.class || fclaz == Boolean.class) field.set(obj, Boolean.parseBoolean(value));
            else if (fclaz == List.class)
            {
                final ParameterizedType pType = (ParameterizedType) field.getGenericType();
                final Class<?> clazz = (Class<?>) pType.getActualTypeArguments()[0];
                // System.out.print(tabbing + pType.getActualTypeArguments()[0]
                // + "\n");
                @SuppressWarnings("rawtypes")
                final List list = (List<?>) obj2;
                if (clazz == String.class) obj2 = value;
                else if (clazz == float.class || clazz == Float.class || clazz == double.class || clazz == Double.class)
                    obj2 = Float.parseFloat(value);
                else if (clazz == int.class || clazz == Integer.class) obj2 = Integer.parseInt(value);
                else if (clazz == byte.class || clazz == Byte.class) obj2 = Byte.parseByte(value);
                else if (clazz == boolean.class || clazz == Boolean.class) obj2 = Boolean.parseBoolean(value);
                else if (clazz.getAnnotation(XmlRootElement.class) != null)
                {
                    obj2 = clazz.getConstructor().newInstance();
                    this.processNode(n, obj2, depth + 1);
                }
                list.add(obj2);
            }
            else if (fclaz == Map.class)
            {
                final Map<QName, String> m = (Map<QName, String>) obj2;
                final QName qn = new QName(name);
                m.put(qn, value);
            }
            else if (fclaz.getAnnotation(XmlRootElement.class) != null)
            {
                if (obj2 == null)
                {
                    obj2 = fclaz.getConstructor().newInstance();
                    field.set(obj, obj2);
                }
                this.processNode(n, obj2, depth + 1);
            }
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }

    private void processNode(final Node node, final Object obj, final int depth)
    {
        // if (depth == 0) System.out.print("\n\n\n\n");
        String tabbing = "";
        for (int i = 0; i < depth; i++)
            tabbing = tabbing + "   ";

        String name = node.getNodeName();
        final NodeList children = node.getChildNodes();
        final NamedNodeMap attributes = node.getAttributes();
        if (name.equals("#text")) return;
        // System.out.print(tabbing + name + "\n");

        final Class<?> clazz = obj.getClass();
        Map<String, Field> attrs = Factory.knownAttrMappins.get(clazz);
        Map<String, Field> elems = Factory.knownElemMappins.get(clazz);
        Field otherAttrs = Factory.knownAnyAtrMappins.get(clazz);
        if (attrs == null || elems == null)
        {
            Factory.knownAttrMappins.put(clazz, attrs = Maps.newHashMap());
            Factory.knownAttrMappins.put(clazz, elems = Maps.newHashMap());
            for (final Field field : obj.getClass().getDeclaredFields())
            {
                if (Modifier.isFinal(field.getModifiers())) continue;
                if (Modifier.isStatic(field.getModifiers())) continue;
                if (Modifier.isTransient(field.getModifiers())) continue;
                final XmlAttribute atr = field.getAnnotation(XmlAttribute.class);
                final XmlElement elem = field.getAnnotation(XmlElement.class);
                final XmlAnyAttribute anyatr = field.getAnnotation(XmlAnyAttribute.class);
                if (atr != null) attrs.put(atr.name(), field);
                else if (elem != null) elems.put(elem.name(), field);
                else if (anyatr != null) otherAttrs = field;
                else continue;
                field.setAccessible(true);
            }
            Factory.knownAnyAtrMappins.put(clazz, otherAttrs);
        }
        // System.out.print(tabbing + "elems=" + elems.keySet() + "\n");
        // System.out.print(tabbing + "attrs=" + attrs.keySet() + "\n");
        // System.out.print(tabbing + "anyatr=" + otherAttrs + "\n");

        if (children != null && children.getLength() > 0) for (int i = 0; i < children.getLength(); i++)
        {
            final Node n = children.item(i);
            name = n.getNodeName();
            final String value = n.getNodeValue();
            // This is a text value node for this object, we treat them
            // seperately
            if (name.equals("#text"))
            {
                if (value == null) continue;
                if (ThutCore.trim(value).isEmpty()) continue;
                // Else we try to set the corresponding object as this.
                ThutCore.LOGGER.error("We do not handle this properly yet!");
                continue;
            }
            if (elems.containsKey(name)) this.apply(n, obj, depth, elems.get(name), tabbing);
        }
        if (attributes != null && attributes.getLength() > 0) for (int i = 0; i < attributes.getLength(); i++)
        {
            final Node n = attributes.item(i);
            name = n.getNodeName();
            if (attrs.containsKey(name)) this.apply(n, obj, depth, attrs.get(name), tabbing);
            else if (otherAttrs != null) this.apply(n, obj, depth, otherAttrs, tabbing);
        }

    }

    public T make(final SAXSource source) throws SAXException, IOException, ParserConfigurationException
    {
        final DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        f.setFeature("http://xml.org/sax/features/namespaces", false);
        f.setFeature("http://xml.org/sax/features/validation", false);
        f.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
        f.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        final Document d = f.newDocumentBuilder().parse(source.getInputSource());
        final Node root = d.getDocumentElement();
        this.processNode(root, this.toFill, 0);
        return this.toFill;
    }
}

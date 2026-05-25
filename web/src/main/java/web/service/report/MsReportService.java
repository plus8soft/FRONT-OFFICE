/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.report;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.annotation.PreDestroy;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
// import com4j.Variant; // Commented out - ms-com-bridge module removed
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.TypedValue;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import web.dictionary.AbstractDictionary;
import web.dictionary.Unit;
import web.entity.core.DictionaryName;
// Imports commented out - ms-com-bridge module removed for Linux compatibility
// To enable on Windows: restore ms-com-bridge module and uncomment these imports
// import word.ClassFactory;
// import word.WdOpenFormat;
// import word.WdSaveFormat;
// import word._Application;
// import word._Document;

/**
 * Service for generating PDF reports from Word (DOCX) templates.
 * 
 * Used for:
 * - Expired document reports (ExpiredDocumentReportService)
 * - Money transfer applications (StepSixContactView, StepSixTransferView)
 * - Other reports requiring PDF generation
 * 
 * DISABLED: Service is disabled because it requires Windows DLL (com4j-amd64.dll), which is not compatible with Linux.
 * The service uses COM4J library to interact with MS Word via COM API.
 * 
 * ENABLE if you want to use: Uncomment the @Component annotation below.
 * Note: Service works only on Windows with Microsoft Word installed.
 */
// @Component
public class MsReportService {

    public static final String SCHEMA = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";

    private static final String NAMESPACE_PREFIX = "w";

    // Commented out - ms-com-bridge module removed for Linux compatibility
    // private static _Application APPLICATION;
    // private static Variant MISSING;
    
    private static final Object APPLICATION = null;
    private static final Object MISSING = null;
    
    // static {
    //     // COM4J initialization - may fail on Linux, so wrap in try-catch
    //     try {
    //         APPLICATION = ClassFactory.createApplication();
    //         MISSING = Variant.getMissing();
    //     } catch (UnsatisfiedLinkError | Exception | NoClassDefFoundError e) {
    //         // COM4J is not available on Linux or classes are not found - class will load, but service won't work
    //         // This is normal, since @Component is commented out and bean is not created
    //         APPLICATION = null;
    //         MISSING = null;
    //     }
    // }

    private static XPathExpression typeExpression;

    private static XPathExpression pathExpression;

    private static XPath xpath;

    private static XPathExpression contentExpression;

    private static XPathExpression textExpression;

    private static ApplicationContext applicationContext;

    private static List<AbstractDictionary> dictionaries;

    static {
        xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(new NamespaceContext() {
            @Override
            public String getNamespaceURI(String prefix) {
                return SCHEMA;
            }

            @Override
            public String getPrefix(String namespaceUri) {
                return NAMESPACE_PREFIX;
            }

            @Override
            public Iterator getPrefixes(String namespaceUri) {
                return Collections.singleton(SCHEMA).iterator();
            }
        });
        try {
            typeExpression = xpath.compile("w:sdtPr/w:alias/@w:val");
            pathExpression = xpath.compile("w:sdtPr/w:tag/@w:val");
            contentExpression = xpath.compile("w:sdtContent/*");
            textExpression = xpath.compile("current()//w:t");
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Autowired
    private ReportService reportService;

    private static Unit findDictionaryUnit(String dictionaryType, String code) {
        DictionaryName dictionaryName;
        try {
            dictionaryName = DictionaryName.valueOf(dictionaryType);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Dictionary '" + dictionaryType + "' not found (may have been removed)", e);
        }
        AbstractDictionary dictionary = dictionaries.stream().filter(abstractDictionary -> abstractDictionary.getDictionaryName()
                                                                                                             .equals(dictionaryName)).findFirst()
                                                    .orElseThrow(() -> new RuntimeException("Dictionary '" + dictionaryType + "' not found"));
        return dictionary.findOne(dictionary.getKeyFunction().apply(code));
    }

    private static byte[] convertToPdf(byte[] template) throws IOException {
        // Method disabled - ms-com-bridge module removed for Linux compatibility
        throw new UnsupportedOperationException("MS Word/COM4J is not available on this platform. PDF conversion requires Windows with MS Word installed and ms-com-bridge module.");
        // Original implementation commented out:
        /*
        if (APPLICATION == null || MISSING == null) {
            throw new UnsupportedOperationException("MS Word/COM4J is not available on this platform. PDF conversion requires Windows with MS Word installed.");
        }
        Path input = Files.createTempFile(null, ".docx");
        Path output = Files.createTempFile(null, ".pdf");
        try {
            Files.write(input, template);
            _Document document;
            synchronized (MsReportService.class) {
                document = APPLICATION.documents()
                                      .open(input.toFile().getAbsolutePath(), false, false, false, MISSING, MISSING, true, MISSING, MISSING,
                                            WdOpenFormat.wdOpenFormatXMLDocument, MISSING, MISSING, MISSING, MISSING, MISSING, MISSING);
            }
            document.saveAs(output.toFile().getAbsolutePath(), WdSaveFormat.wdFormatPDF, MISSING, MISSING, MISSING, MISSING, MISSING, MISSING,
                            MISSING, MISSING, MISSING, MISSING, MISSING, MISSING, MISSING, MISSING);
            document.close(MISSING, MISSING, MISSING);
            return Files.readAllBytes(output);
        } finally {
            Files.deleteIfExists(input);
            Files.deleteIfExists(output);
        }
        */
    }

    private static List<Node> toList(NodeList nodeList) {
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            nodes.add(nodeList.item(i));
        }
        return nodes;
    }

    @PreDestroy
    private void destroy() {
        // Method disabled - ms-com-bridge module removed
        // if (APPLICATION != null && MISSING != null) {
        //     try {
        //         APPLICATION.quit(MISSING, MISSING, MISSING);
        //     } catch (Exception e) {
        //         // Ignore errors during shutdown
        //     }
        // }
    }

    @Autowired
    private void setDictionaries(List<AbstractDictionary> dictionaries) {
        MsReportService.dictionaries = dictionaries;
    }

    @Autowired
    private void setApplicationContext(ApplicationContext applicationContext) {
        MsReportService.applicationContext = applicationContext;
    }

    public String build(byte[] template, Locale locale, ZoneId zoneId, Object... contexts) throws Exception {
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(template));
             ZipOutputStream zipOutputStream = new ZipOutputStream(byteOutputStream)) {
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                zipOutputStream.putNextEntry(new ZipEntry(zipEntry.getName()));
                if ("word/document.xml".equals(zipEntry.getName())) {
                    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                    documentBuilderFactory.setNamespaceAware(true);
                    documentBuilderFactory.setIgnoringElementContentWhitespace(true);
                    byte[] bytes = new byte[1];
                    int length;
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    while ((length = zipInputStream.read(bytes, 0, bytes.length)) > 0) {
                        byteArrayOutputStream.write(bytes, 0, length);
                    }
                    StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
                    evaluationContext.setVariable("DateTimeFormatter", DateTimeFormatter.class);
                    evaluationContext.setVariable("String", String.class);
                    evaluationContext.setVariable("locale", locale);
                    evaluationContext.setVariable("zoneId", zoneId);
                    evaluationContext.registerFunction("dictionary", getClass().getDeclaredMethod("findDictionaryUnit", String.class, String.class));
                    evaluationContext.setBeanResolver((context, beanName) -> applicationContext.getBean(beanName));
                    Stream.of(contexts)
                          .forEach(context -> evaluationContext.setVariable(StringUtils.uncapitalize(context.getClass().getSimpleName()), context));
                    Document document =
                            documentBuilderFactory.newDocumentBuilder().parse(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
                    process(document.getDocumentElement(), evaluationContext, new SpelExpressionParser());
                    document.getDocumentElement().removeAttributeNS("http://schemas.openxmlformats.org/markup-compatibility/2006", "Ignorable");
                    Transformer transformer = TransformerFactory.newInstance().newTransformer();
                    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
                    byteArrayOutputStream = new ByteArrayOutputStream();
                    transformer.transform(new DOMSource(document), new StreamResult(byteArrayOutputStream));
                    zipOutputStream.write(byteArrayOutputStream.toByteArray(), 0, byteArrayOutputStream.size());
                } else {
                    byte[] bytes = new byte[1];
                    int length;
                    while ((length = zipInputStream.read(bytes, 0, bytes.length)) > 0) {
                        zipOutputStream.write(bytes, 0, length);
                    }
                }
            }
        }
        return reportService.buildReport(convertToPdf(byteOutputStream.toByteArray()));
    }

    private void process(Node node, StandardEvaluationContext evaluationContext, ExpressionParser expressionParser) {
        try {
            if (SCHEMA.equals(node.getNamespaceURI()) && "sdt".equalsIgnoreCase(node.getLocalName())) {
                String type = (String) typeExpression.evaluate(node, XPathConstants.STRING);
                String path = (String) pathExpression.evaluate(node, XPathConstants.STRING);
                switch (type) {
                    case "rendered":
                        if (expressionParser.parseExpression(path).getValue(evaluationContext, Boolean.class)) {
                            List<Node> contents = toList((NodeList) contentExpression.evaluate(node, XPathConstants.NODESET));
                            contents.forEach(contentNode -> {
                                contentNode = contentNode.cloneNode(true);
                                node.getParentNode().insertBefore(contentNode, node);
                                process(contentNode, evaluationContext, expressionParser);
                            });
                        }
                        node.getParentNode().removeChild(node);
                        break;
                    case "field":
                        for (Node contentNode1 : toList((NodeList) contentExpression.evaluate(node, XPathConstants.NODESET))) {
                            try {
                                Element fieldElement = (Element) textExpression.evaluate(contentNode1, XPathConstants.NODE);
                                if (fieldElement != null) {
                                    fieldElement.setTextContent(expressionParser.parseExpression(path).getValue(evaluationContext, String.class));
                                    node.getParentNode().insertBefore(contentNode1, node);
                                    break;
                                }
                            } catch (XPathExpressionException e) {
                                throw new RuntimeException(e.getMessage(), e);
                            }
                        }
                        node.getParentNode().removeChild(node);
                        break;
                    case "list":
                        TypedValue rootObject = evaluationContext.getRootObject();
                        List<Node> contents = toList((NodeList) contentExpression.evaluate(node, XPathConstants.NODESET));
                        expressionParser.parseExpression(path).getValue(evaluationContext, Iterable.class).forEach(item -> {
                            evaluationContext.setRootObject(item);
                            contents.forEach(contentNode -> {
                                contentNode = contentNode.cloneNode(true);
                                node.getParentNode().insertBefore(contentNode, node);
                                process(contentNode, evaluationContext, expressionParser);
                            });
                        });
                        node.getParentNode().removeChild(node);
                        evaluationContext.setRootObject(rootObject.getValue());
                        break;
                }
            } else {
                toList(node.getChildNodes()).forEach(childNode -> process(childNode, evaluationContext, expressionParser));
            }
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}

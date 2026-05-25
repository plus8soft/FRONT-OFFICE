/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.custom.reporttemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.TreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import web.entity.dict.ContextType;
import web.entity.dict.DictionaryParameter;
import web.entity.dict.ReportTemplate;
import web.entity.dict.ReportTemplateContext;
import web.repository.dict.ReportTemplateContextRepository;
import web.repository.dict.ReportTemplateRepository;
import web.service.report.MsReportService;
import web.view.Message;

@Getter
@Setter
@Log4j2
public class ReportTemplateEditView implements Serializable, Message {

    private static final String TEMPLATE_IMAGE_PATH = "/image/report-template.png";

    private static final String CONTEXT_IMAGE_PATH = "/image/data-context.png";

    @Autowired
    private ReportTemplateRepository reportTemplateRepository;

    @Autowired
    private ReportTemplateContextRepository reportTemplateContextRepository;

    @Autowired(required = false)
    private MsReportService msReportService;

    private DictionaryParameter dictionary;

    private ReportTemplate template;

    private StreamedContent downloadedTemplate;

    private StreamedContent downloadedSchema;

    private String templateImage;

    private String dataContextImage;

    private List<ContextType> contextTypes;

    private List<ContextType> selectedContextTypes;

    private List<ReportTemplateContext> selectedContexts;

    private TreeNode contextTree;

    public void init(ReportTemplate template, DictionaryParameter dictionary) {
        this.template = template;
        this.dictionary = dictionary;
        contextTypes = Arrays.asList(ContextType.values());
        selectedContexts = template.getReportTemplateContexts();
        selectedContextTypes = selectedContexts.stream().map(ReportTemplateContext::getType).collect(Collectors.toList());
        rebuildContextTree();
        try {
            templateImage = String.format("data:image/jpg;base64,%s", Base64.getEncoder().encodeToString(
                    Files.readAllBytes(Paths.get(getClass().getResource(TEMPLATE_IMAGE_PATH).toURI()))));
            dataContextImage = String.format("data:image/jpg;base64,%s", Base64.getEncoder().encodeToString(
                    Files.readAllBytes(Paths.get(getClass().getResource(CONTEXT_IMAGE_PATH).toURI()))));
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void rebuildContextTree() {
        contextTree = buildContextTree(template.getSystemName() != null ? Collections.singletonList(template.getSystemName().getContextClass()) :
                                       selectedContextTypes.stream().map(ContextType::getContextClass).collect(Collectors.toList()));
    }

    private DefaultTreeNode buildContextTree(List<Class<?>> contextClasses) {
        DefaultTreeNode node = new DefaultTreeNode(new ContextItem("root", null, null), null);
        contextClasses.forEach(contextClass -> {
            DefaultTreeNode child =
                    new DefaultTreeNode(new ContextItem(StringUtils.uncapitalize(contextClass.getSimpleName()), contextClass, null), node);
            node.getChildren().add(child);
            processClass(contextClass, child, new HashSet<>());
        });
        return node;
    }

    private void processClass(Class<?> contextClass, TreeNode currentNode, Set<Class> classCache) {
        if (contextClass.getCanonicalName().startsWith("web.") && !classCache.contains(contextClass)) {
            classCache.add(contextClass);
            ReflectionUtils.doWithFields(contextClass, field -> {
                Class<?> type;
                Class<?> iterableType = null;
                if (Iterable.class.isAssignableFrom(field.getType())) {
                    type = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                    iterableType = field.getType();
                } else {
                    type = field.getType();
                }
                TreeNode treeNode = new DefaultTreeNode(new ContextItem(field.getName(), type, iterableType), currentNode);
                currentNode.getChildren().add(treeNode);
                processClass(type, treeNode, new HashSet<>(classCache));
            });
        }
    }

    public void uploadTemplate(FileUploadEvent event) {
        template.setFile(event.getFile().getContents());
        template.setDate(Instant.now());
        template.setSize(event.getFile().getSize());
    }

    public String save() {
        try {
            reportTemplateRepository.save(template);
            selectedContextTypes.forEach(selectedContextType -> {
                if (selectedContexts.stream().noneMatch(selectedContext -> selectedContext.getType().equals(selectedContextType))) {
                    ReportTemplateContext reportTemplateContext = new ReportTemplateContext();
                    reportTemplateContext.setType(selectedContextType);
                    reportTemplateContext.setReportTemplate(template);
                    reportTemplateContextRepository.save(reportTemplateContext);
                }
            });
            selectedContexts.forEach(selectedContext -> {
                if (selectedContextTypes.stream().noneMatch(selectedContextType -> selectedContext.getType().equals(selectedContextType))) {
                    reportTemplateContextRepository.delete(selectedContext);
                }
            });
            addInfoMessage("Data saved successfully.");
            return "save";
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            addErrorMessage("Internal error while saving data.");
            return null;
        }
    }

    public void downloadTemplate() {
        try (ByteArrayInputStream stream = new ByteArrayInputStream(template.getFile())) {
            downloadedTemplate = new DefaultStreamedContent(stream, "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                                            String.format("%s%s", template.getName(), ".docx"));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}

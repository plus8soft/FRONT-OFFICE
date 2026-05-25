/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.ce;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.color.DeviceRgb;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Style;
import com.itextpdf.layout.border.Border;
import com.itextpdf.layout.border.SolidBorder;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.List;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Tab;
import com.itextpdf.layout.element.TabStop;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.Leading;
import com.itextpdf.layout.property.ListNumberingType;
import com.itextpdf.layout.property.Property;
import com.itextpdf.layout.property.TabAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import com.itextpdf.layout.property.VerticalAlignment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import web.dictionary.DocumentTypeDictionary;
import web.entity.ce.CurrencyOperation;
import web.entity.core.Department;
import web.entity.crm.BaseDocument;
import web.entity.dict.Currency;
import web.entity.log.OperationCode;
import web.entity.log.PersonHistory;
import web.repository.back.ce.OutputOperationData;
import web.repository.dict.CurrencyRepository;
import web.service.back.CurrencyExchangeBackService;
import web.service.report.ReportService;
import web.session.UserSession;
import web.utils.DateTimes;
import web.utils.Utils;

@Component
public class OperationReportService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    private static final String NATIONAL_CURRENCY_CODE = "840"; // USD

    @Autowired
    private DocumentTypeDictionary documentTypeDictionary;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private CurrencyExchangeBackService currencyExchangeBackService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private Utils utils;

    public String print(CurrencyOperation operation, UserSession userSession, Locale locale) {
        return reportService.buildReport(document -> {
            document.setFontSize(10).setProperty(Property.LEADING, new Leading(Leading.FIXED, 10));
            Table table = new Table(UnitValue.createPercentArray(new float[]{70, 30}));
            table.addCell(createFixedLeadingLeftCell("Full (abbreviated) legal name of authorized bank (branch name)"))
                 .addCell(createFixedLeadingRightCell(
                         Optional.ofNullable(operation.getDepartment().getParent()).map(Department::getFullName).orElse("")));
            table.addCell(createFixedLeadingLeftCell("Name of internal structural unit"))
                 .addCell(createFixedLeadingRightCell(operation.getDepartment().getFullName()));
            table.addCell(createFixedLeadingLeftCell("Registration number of authorized bank (branch sequential number)"))
                 .addCell(createRightCell("2490"));
            table.addCell(
                    createFixedLeadingLeftCell("Location (address) of authorized bank (branch) or internal structural unit"))
                 .addCell(createFixedLeadingRightCell(utils.getAddresses().formatAddress(operation.getDepartment())));
            document.add(table);
            table = new Table(UnitValue.createPercentArray(new float[]{50, 50})).setMarginTop(5);
            table.addCell(createLeftCell("Operation sequential number:")).addCell(createRightCell(String.valueOf(operation.getRegistryNumber())));
            table.addCell(createLeftCell("Date and time of transaction:"))
                 .addCell(createRightCell(operation.getDate().format(DateTimes.DATE_TIME_FORMATTER.withLocale(locale))));
            table.addCell(createLeftCell("Operation type code:"))
                 .addCell(createRightCell(OperationCode.SELL.equals(operation.getCode()) ? "02" : "01"));
            table.addCell(createLeftCell("Full Name:")).addCell(createRightCell(operation.getPersonHistory() != null ?
                                                                             getFullName(operation.getPersonHistory().getPerson().getLastname(),
                                                                                         operation.getPersonHistory().getPerson().getFirstname(),
                                                                                         operation.getPersonHistory().getPerson().getPatronymic()) :
                                                                             ""));
            BaseDocument personDocument = Optional.ofNullable(operation.getPersonHistory()).map(PersonHistory::getDocument).orElse(null);
            table.addCell(createLeftCell("Document:")).addCell(new Cell().add(personDocument != null ? new Paragraph(
                    String.format("%s: %s No. %s Issued by %s %s%s", documentTypeDictionary.findOne(personDocument.getType()).getValue(),
                                  personDocument.getSeries() == null ? "" : String.format("series %s ", personDocument.getSeries()),
                                  personDocument.getNumber(), personDocument.getIssuanceUnit(), personDocument.getIssuanceDate().format(FORMATTER),
                                  personDocument.getIssuanceUnitCode() == null ? "" :
                                  String.format(", code %s", personDocument.getIssuanceUnitCode()))).setFixedLeading(10) : new Paragraph())
                                                                         .setPaddingTop(0).setPaddingBottom(0).setBorder(Border.NO_BORDER));
            table.addCell(createLeftCell("Rate:").setPaddingTop(5).setPaddingBottom(3))
                 .addCell(createRightCell(String.format(locale, "%,.4f", operation.getRate())).setPaddingTop(5).setPaddingBottom(3));
            table.addCell(new Cell(1, 2).add("RECEIVED:").setPaddingTop(0).setPaddingBottom(0).setBorder(Border.NO_BORDER));
            table.addCell(new Cell(1, 2).add("Cash currency:").setPaddingLeft(40).setBorder(Border.NO_BORDER));
            Currency nationalCurrency = currencyRepository.findOne(NATIONAL_CURRENCY_CODE);
            if (OperationCode.BUY.equals(operation.getCode())) {
                table.addCell(createLeftCell("Currency code").setTextAlignment(TextAlignment.RIGHT)).addCell(createRightCell(
                        nationalCurrency.equals(operation.getCurrency()) ? nationalCurrency.getAlternativeCode() : operation.getCurrency().getId()));
                table.addCell(createLeftCell("Currency name").setTextAlignment(TextAlignment.RIGHT))
                     .addCell(createRightCell(operation.getCurrency().getName()));
                table.addCell(createLeftCell("Amount").setTextAlignment(TextAlignment.RIGHT))
                     .addCell(createRightCell(String.format(locale, "%,.2f", operation.getSum())));
            } else {
                table.addCell(createLeftCell("Currency code").setTextAlignment(TextAlignment.RIGHT))
                     .addCell(createRightCell(nationalCurrency.getAlternativeCode()));
                table.addCell(createLeftCell("Currency name").setTextAlignment(TextAlignment.RIGHT))
                     .addCell(createRightCell(nationalCurrency.getName()));
                table.addCell(createLeftCell("Amount").setTextAlignment(TextAlignment.RIGHT));
                table.addCell(createRightCell(String.format(locale, "%,.2f",
                                                            operation.isCommissionEnabled() ? operation.getBaseAmount().add(operation.getCommission()) :
                                                            operation.getBaseAmount())));
            }
            if (operation.isCommissionEnabled()) {
                table.addCell(new Cell(1, 2).add("BANK COMMISSION DEDUCTED:").setPaddingTop(0).setPaddingBottom(0).setBorder(Border.NO_BORDER));
                table.addCell(createLeftCell("Currency code").setTextAlignment(TextAlignment.RIGHT))
                     .addCell(createRightCell(nationalCurrency.getAlternativeCode()));
                table.addCell(createLeftCell("Currency name").setTextAlignment(TextAlignment.RIGHT))
                     .addCell(createRightCell(nationalCurrency.getName()));
                table.addCell(createLeftCell("Amount").setTextAlignment(TextAlignment.RIGHT))
                     .addCell(createRightCell(String.format(locale, "%,.2f", operation.getCommission())));
            }
            table.addCell(new Cell(1, 2).add("ISSUED:").setPaddingTop(0).setPaddingBottom(0).setBorder(Border.NO_BORDER));
            table.addCell(new Cell(1, 2).add("Cash currency:").setPaddingLeft(40).setBorder(Border.NO_BORDER));
            if (OperationCode.BUY.equals(operation.getCode())) {
                table.addCell(createLeftCell("Currency code").setTextAlignment(TextAlignment.RIGHT))
                     .addCell(createRightCell(nationalCurrency.getAlternativeCode()));
                table.addCell(createLeftCell("Currency name").setTextAlignment(TextAlignment.RIGHT))
                     .addCell(createRightCell(nationalCurrency.getName()));
                table.addCell(createLeftCell("Amount").setTextAlignment(TextAlignment.RIGHT));
                table.addCell(createRightCell(String.format(locale, "%,.2f", operation.isCommissionEnabled() ?
                                                                             operation.getBaseAmount().add(operation.getCommission().negate()) :
                                                                             operation.getBaseAmount())));
            } else {
                table.addCell(createLeftCell("Currency code").setTextAlignment(TextAlignment.RIGHT)).addCell(createRightCell(
                        nationalCurrency.equals(operation.getCurrency()) ? nationalCurrency.getAlternativeCode() : operation.getCurrency().getId()));
                table.addCell(createLeftCell("Currency name").setTextAlignment(TextAlignment.RIGHT))
                     .addCell(createRightCell(operation.getCurrency().getName()));
                table.addCell(createLeftCell("Amount").setTextAlignment(TextAlignment.RIGHT))
                     .addCell(createRightCell(String.format(locale, "%,.2f", operation.getSum())));
            }
            document.add(table);
            Text text = new Text("/" + utils.getStrings().capitalizeFio(userSession.getUser().getLastname(), userSession.getUser().getFirstname(),
                                                                        userSession.getUser().getPatronymic()) + "/")
                    .addStyle(new Style().setMarginLeft(10));
            document.add(new Paragraph(new Text("Cashier signature").addStyle(new Style().setMarginRight(10)))
                                 .addTabStops(new TabStop(270, TabAlignment.LEFT, new SolidLine(1))).add(new Tab()).add(text).setMarginTop(8));
        });
    }

    public String printRequestConfirmation(CurrencyOperation operation, Locale locale) {
        return reportService.buildReport(document -> {
            document.add(new Image(ImageDataFactory.createPng(getClass().getResource("/image/logo.png")))
                                 .setHorizontalAlignment(HorizontalAlignment.CENTER));
            Div div = new Div();
            div.setTextAlignment(TextAlignment.CENTER);
            div.setBold();
            String fio = getFullName(operation.getPersonHistory().getPerson().getLastname(), operation.getPersonHistory().getPerson().getFirstname(),
                                     operation.getPersonHistory().getPerson().getPatronymic());
            div.add(new Paragraph("To: ").add(fio).setTextAlignment(TextAlignment.RIGHT));
            div.add(new Paragraph("Address: ").add(utils.getAddresses().formatAddress(operation.getPersonHistory().getAddress())));
            div.add(new Paragraph("Request for confirmation of source of funds."));
            div.add(new Paragraph(operation.getDate().format(new DateTimeFormatterBuilder().appendLiteral("dated ").appendPattern("MM/dd")
                                                                                           .appendText(ChronoField.MONTH_OF_YEAR,
                                                                                                       DateTimes.MONTH_OF_YEAR_TEXT)
                                                                                           .appendPattern("/yyyy").toFormatter(locale))));
            div.add(new Paragraph("Dear ").add(Stream.of(operation.getPersonHistory().getPerson().getFirstname(),
                                                                    operation.getPersonHistory().getPerson().getPatronymic()).filter(Objects::nonNull)
                                                                .collect(Collectors.joining(" ")) + "!").setMarginTop(20));
            document.add(div);
            document.add(new Paragraph("In accordance with applicable anti-money laundering and counter-terrorism financing laws and regulations, " +
                                                                                                    "we request that you provide information (including documentary evidence) regarding the source of funds:")
                                                                                               .setFirstLineIndent(30).setFontSize(10)
                                                                                               .setTextAlignment(TextAlignment.JUSTIFIED));
            String squareChar = "\u25A1";
            List list = new List().setListSymbol(new Text(squareChar).setFontSize(14)).setSymbolIndent(5).setFontSize(10)
                                  .add("deposited to your bank account (deposit);");
            document.add(list);
            document.add(new LineSeparator(new SolidLine(1.3f)).setMarginTop(12));
            document.add(new Paragraph("(account number)").setTextAlignment(TextAlignment.CENTER).setFontSize(8).setMarginTop(0)
                                                       .setRelativePosition(0, 0, 0, 2));
            list = new List().setListSymbol(new Text(squareChar).setFontSize(14)).setSymbolIndent(5).setFontSize(10)
                             .add("for the purpose of making a transfer;").add("for the purpose of currency exchange operation;").add("other");
            document.add(list);
            document.add(new LineSeparator(new SolidLine(1)).setWidthPercent(50).setMarginLeft(35).setRelativePosition(0, 0, 0, 5));
            document.add(new LineSeparator(new SolidLine(1)).setWidthPercent(80).setMarginTop(20));
            document.add(new Paragraph("(date of receipt/deposit of funds)").setFontSize(8).setRelativePosition(0, 0, 0, 4)
                                                                                      .add(new Text("amount").setFontSize(10)
                                                                                                               .setRelativePosition(10, 0, 0, 10)
                                                                                                               .setBackgroundColor(Color.WHITE))
                                                                                      .setMarginTop(0));
            document.add(new Paragraph("by completing Appendix No. 1 to this request.").setMarginTop(20).setFontSize(10));
            document.add(new Paragraph("Certified copies of documents signed by you are submitted only in paper form to " +
                                       "the operations department with a note for the Financial Monitoring Department.")
                                 .setFirstLineIndent(30).setMarginTop(20).setTextAlignment(TextAlignment.JUSTIFIED).setFontSize(10));
            document.add(new Paragraph("The Bank guarantees the confidentiality of the information you provide in accordance with applicable law.")
                                 .setMarginTop(30).setTextAlignment(TextAlignment.JUSTIFIED).setFontSize(10));
            document.add(new LineSeparator(new SolidLine(1)).setWidthPercent(43).setMarginTop(80));
            document.add(new LineSeparator(new SolidLine(1)).setWidthPercent(20).setHorizontalAlignment(HorizontalAlignment.CENTER).setMarginLeft(50)
                                                            .setRelativePosition(0, 0, 0, 1));
            document.add(new LineSeparator(new SolidLine(1)).setWidthPercent(30).setHorizontalAlignment(HorizontalAlignment.RIGHT).setMarginRight(10)
                                                            .setRelativePosition(0, 0, 0, 2));
            document.add(new Paragraph("(position of employee sending/transmitting request)").setRelativePosition(0, 0, 0, 4)
                                                                                                 .add(new Text("(signature)")
                                                                                                              .setRelativePosition(50, 0, 0, 0))
                                                                                                 .add(new Text("(Full Name)")
                                                                                                              .setRelativePosition(170, 0, 0, 0))
                                                                                                 .setFontSize(8).setMarginTop(0));
            document.add(new AreaBreak());
            document.add(new Paragraph("Appendix No. 1").setTextAlignment(TextAlignment.RIGHT).setFontSize(8).setMarginBottom(0));
            document.add(
                    new Paragraph("to the request for confirmation of source of funds.").setTextAlignment(TextAlignment.RIGHT).setFontSize(8)
                                                                                              .setMarginTop(0));
            Table table = new Table(UnitValue.createPercentArray(new float[]{30, 70})).setMarginTop(20);
            table.addCell(new Cell().add(new Paragraph("Last Name, First Name, Middle Name (full).").setFixedLeading(14)).setBold().setItalic()
                                    .setTextAlignment(TextAlignment.CENTER))
                 .addCell(new Cell().add(fio).setBold().setFontSize(10).setVerticalAlignment(VerticalAlignment.MIDDLE));
            document.add(table);
            table = new Table(UnitValue.createPercentArray(new float[]{70, 30})).setFontSize(10);
            table.addCell(
                    new Cell(1, 2).add("I confirm the source of funds:").setBold().setItalic().setBorderTop(Border.NO_BORDER));
            Paragraph paragraph = new Paragraph(squareChar).setFontSize(20).setFixedLeading(10);
            table.addCell("Salary").addCell(new Cell().add(paragraph).setTextAlignment(TextAlignment.CENTER));
            table.addCell("Personal savings").addCell(new Cell().add(paragraph).setTextAlignment(TextAlignment.CENTER));
            table.addCell("Business income").addCell(new Cell().add(paragraph).setTextAlignment(TextAlignment.CENTER));
            table.addCell("Inheritance").addCell(new Cell().add(paragraph).setTextAlignment(TextAlignment.CENTER));
            table.addCell("Credit, borrowed funds").addCell(new Cell().add(paragraph).setTextAlignment(TextAlignment.CENTER));
            table.addCell("Interest income on deposits (securities)").addCell(new Cell().add(paragraph).setTextAlignment(TextAlignment.CENTER));
            table.addCell("Proceeds from sale of real estate, stocks").addCell(new Cell().add(paragraph).setTextAlignment(TextAlignment.CENTER));
            table.addCell(new Cell(1, 2).add("Other:").setPaddingBottom(120));
            table.addCell(new Cell(1, 2).add("Documents provided:").setPaddingBottom(80));
            table.addCell(new Paragraph("I refuse to disclose the source of funds or other property").setFixedLeading(10))
                 .addCell(new Cell().add(paragraph).setTextAlignment(TextAlignment.CENTER).setVerticalAlignment(VerticalAlignment.MIDDLE));
            document.add(table);
            document.add(new Paragraph("Client /").add(new Text(fio).setBold()).add("/").setPaddingLeft(40).setPaddingTop(60).setMarginBottom(0)
                                                  .setFontSize(10));
            document.add(new Paragraph("(Full Name)").add(new Text("(signature)").setRelativePosition(200, 0, 0, 0)).setPaddingLeft(110).setMarginTop(0)
                                               .setFontSize(10).setRelativePosition(0, 0, 0, 6));
            document.add(new Paragraph("Date ").add(new Text(String.format("%8s", "")).setBorderBottom(new SolidBorder(1))).add("/")
                                               .add(new Text(String.format("%24s", "")).setBorderBottom(new SolidBorder(1))).add("/")
                                               .add(new Text(String.format("%4s", "")).setBorderBottom(new SolidBorder(1)))
                                               .setPaddingLeft(40).setFontSize(10));
        });
    }

    public String printConsentPersonalData(CurrencyOperation operation, UserSession userSession, Locale locale) {
        return reportService.buildReport(document -> {
            document.setFontSize(8).setTextAlignment(TextAlignment.JUSTIFIED).setProperty(Property.LEADING, new Leading(Leading.FIXED, 9));
            document.add(new Paragraph("Consent to processing of personal data.").setTextAlignment(TextAlignment.CENTER).setBold());
            String date = operation.getDate().format(new DateTimeFormatterBuilder().appendPattern("''MM/dd'' ")
                                                                                   .appendPattern("yyyy").toFormatter(locale));
            document.add(new Paragraph(date).setTextAlignment(TextAlignment.RIGHT).setBold());
            Text text = new Text(
                    getFullName(operation.getPersonHistory().getPerson().getLastname(), operation.getPersonHistory().getPerson().getFirstname(),
                                operation.getPersonHistory().getPerson().getPatronymic())).addStyle(new Style().setMarginLeft(10));
            document.add(new Paragraph("I, ").add(text).setMarginBottom(0));
            document.add(new LineSeparator(new SolidLine(0.5f)).setWidthPercent(98).setHorizontalAlignment(HorizontalAlignment.RIGHT));
            document.add(new Paragraph("(Last Name, First Name, Middle Name)").setMargin(0).setItalic().setFontSize(6).setTextAlignment(TextAlignment.CENTER));
            BaseDocument personDocument = operation.getPersonHistory().getDocument();
            text.setText(documentTypeDictionary.findOne(personDocument.getType()).getValue());
            document.add(new Paragraph("identity document:").add(text).setMarginBottom(0));
            document.add(new LineSeparator(new SolidLine(0.5f)).setWidthPercent(28).setMarginLeft(136));
            text.setText(personDocument.getSeries() == null ? "" : personDocument.getSeries());
            document.add(new Paragraph("series:").add(text).setMargin(0).setRelativePosition(296, 0, 0, 10));
            document.add(new LineSeparator(new SolidLine(0.5f)).setWidthPercent(10).setMarginLeft(322).setRelativePosition(0, 0, 0, 9));
            text.setText(personDocument.getNumber());
            document.add(new Paragraph("No.:").add(text).setMargin(0).setRelativePosition(396, 0, 0, 19));
            document.add(new LineSeparator(new SolidLine(0.5f)).setWidthPercent(21).setHorizontalAlignment(HorizontalAlignment.RIGHT)
                                                               .setRelativePosition(0, 0, 0, 19));
            text.setText(personDocument.getIssuanceDate().format(FORMATTER));
            document.add(new Paragraph("issued by: ").add(text).setMargin(0).setRelativePosition(0, 0, 0, 10));
            document.add(new LineSeparator(new SolidLine(0.5f)).setWidthPercent(94).setHorizontalAlignment(HorizontalAlignment.RIGHT)
                                                               .setRelativePosition(0, 0, 0, 10));
            Paragraph issuanceParagraph = new Paragraph(personDocument.getIssuanceUnit());
            if (personDocument.getIssuanceUnitCode() != null) {
                issuanceParagraph.add(", code ").add(personDocument.getIssuanceUnitCode());
            }
            document.add(issuanceParagraph.setMarginBottom(0).setPaddingLeft(5));
            document.add(new LineSeparator(new SolidLine(0.5f)));
            document.add(new Paragraph("(issued by and date)").setMargin(0).setItalic().setFontSize(6).setTextAlignment(TextAlignment.CENTER));
            text.setText(utils.getAddresses().formatAddress(operation.getPersonHistory().getAddress()));
            document.add(new Paragraph("registered at address:").add(text).setMarginBottom(0));
            document.add(new LineSeparator(new SolidLine(0.5f)).setWidthPercent(80).setHorizontalAlignment(HorizontalAlignment.RIGHT));
            Paragraph paragraph = new Paragraph("acting of my own free will and in my own interest, I provide ").add(new Text("Bank").setBold());
            paragraph.add(" (hereinafter referred to as the «Operator»), consent to the processing of " +
                          "my personal data (hereinafter referred to as «Consent»). The Operator has the right to process the personal data provided by me, namely:");
            document.add(paragraph.setMargin(0)).add(new LineSeparator(new SolidLine(1)).setMarginTop(5));
            Table table = new Table(UnitValue.createPercentArray(new float[]{55, 45}));
            List list = new List().setListSymbol("-").setSymbolIndent(10).add("Last Name, First Name, Middle Name;").add("citizenship;")
                                  .add("identity document data;").add("taxpayer identification number (EIN);")
                                  .add("information about documents containing my personal data;")
                                  .add("contact phone numbers and email addresses;").add("marital status;");
            table.addCell(new Cell().add(list).setBorder(Border.NO_BORDER).setPadding(0));
            list = new List().setListSymbol("-").setSymbolIndent(10).add("date and place of birth;").add("address;").add("migration card data;")
                             .add("employment information;").add("education;").add("profession;");
            table.addCell(new Cell().add(list).setBorder(Border.NO_BORDER));
            list = new List().setListSymbol("-").setSymbolIndent(10).add("other personal data and any other information available or " +
                                                                         "known to the Operator at any given time.");
            document.add(table.addCell(new Cell(1, 2).add(list).setBorder(Border.NO_BORDER).setPadding(0)));
            document.add(new Paragraph("The Operator has the right to perform any actions with the personal data provided by me, " +
                                       "as provided by applicable law on personal data protection.").setFirstLineIndent(15).setMargin(0));
            document.add(new Paragraph("This consent is provided for the proper implementation of the following purposes: collection, " +
                                       "systematization, accumulation, storage, clarification, (update, change), use, depersonalization, " +
                                       "blocking, destruction, distribution, (including transfer to regulatory authorities), fulfillment " +
                                       "by the Operator of its obligations arising from applicable laws, other legal acts, including acts " +
                                       "of executive authorities, Central Bank, as well as from agreements with counterparties, as well as " +
                                       "performance of any other actions with my personal data in accordance with applicable law.")
                                 .setFirstLineIndent(15).setMargin(0));
            document.add(new Paragraph("I hereby acknowledge that if it is necessary to provide personal data to achieve the above purposes to a " +
                                       "third party, the Operator has the right to disclose in the necessary scope for performing the above actions " +
                                       "information about me personally (including my Personal Data) to such third parties, as well as provide such " +
                                       "persons with relevant documents containing such information. ")
                                 .setFirstLineIndent(15).setMargin(0));
            document.add(new Paragraph("This consent is valid for an indefinite period and may be withdrawn by sending a written withdrawal statement " +
                                       "to the Operator, whereupon the Operator stops processing personal data and destroys it, except for personal data " +
                                       "included in documents, the storage of which is directly provided for by law and the Operator's internal documents. " +
                                       "Storage of such personal data is carried out by the Operator for the period established by law and the Operator's " +
                                       "internal documents.").setFirstLineIndent(15).setMargin(0));
            document.add(new Paragraph("The statement may be made in free form. ").setFirstLineIndent(15).setMargin(0));
            document.add(new Paragraph("In case of withdrawal of this consent, personal data included in documents generated in the Operator's activities, " +
                                       "including in the Operator's internal documents during the validity period of the Consent, may be transferred to third " +
                                       "parties in the scope and cases specified in this Consent.").setFirstLineIndent(15)
                                                                                                                        .setMargin(0));
            document.add(new Paragraph("I also confirm that my personal data may be obtained by the Operator from any third parties.")
                                 .setFirstLineIndent(15).setMargin(0));
            document.add(new Paragraph("Notification about receiving personal data not from the personal data subject.").setBold().setTextAlignment(
                    TextAlignment.CENTER).setMarginTop(5).setMarginBottom(0));
            list = new List(ListNumberingType.DECIMAL).setSymbolIndent(5);
            list.add("Processing of personal data is carried out by the Operator in order to comply with the requirements of applicable law, " +
                     "as well as contracts and agreements with legal entities on behalf of or in the interests of which the personal data subject acts.")
                .add("The expected circle of users of personal data provided by the subject includes employees of the Operator, " +
                     "employees of regulatory, controlling and supervisory government bodies, counterparties of the Operator and other persons when " +
                     "exercising their powers in accordance with the requirements of applicable law and concluded agreements.")
                .add("In accordance with applicable law, personal data subjects have the following rights:");
            document.add(list);
            list = new List().setListSymbol(ListNumberingType.ENGLISH_LOWER).setSymbolIndent(5);
            list.add("to access their personal data;")
                .add("to preliminary consent and immediate termination of processing upon request when processing personal data for the purpose of " +
                     "promoting goods, works, services on the market;")
                .add("arising when making decisions based solely on automated processing of their personal data;")
                .add("to appeal against actions or inaction of the Operator;").add("other rights established by applicable law.");
            document.add(list)
                    .add(new Paragraph("Personal data subject signature").add(new Text("1").setFontSize(6).setRelativePosition(0, 0, 0, 3))
                                                                              .add(":").setMarginBottom(0));
            document.add(new LineSeparator(new SolidLine(0.5f)).setWidthPercent(12).setMarginLeft(144));
            document.add(new Paragraph(
                    "For transactions up to $15,000 this section is not filled in, except if the recipient is an individual!")
                                 .setWidthPercent(50).setFontSize(8).setMargin(0).setHorizontalAlignment(HorizontalAlignment.RIGHT)
                                 .setRelativePosition(0, 0, 0, 10));
            document.add(new LineSeparator(new SolidLine(1)));
            table = new Table(UnitValue.createPercentArray(new float[]{50, 50})).setFontSize(7);
            Text yes = new Text("Yes").addStyle(new Style().setMarginLeft(30));
            Text no = new Text("No").addStyle(new Style().setMarginLeft(30));
            table.addCell(new Cell().add(new Paragraph("1) Do you have a Beneficial Owner").add(yes).add(no)).add(new Paragraph(
                    "Beneficial owner - an individual who has the right (possibility), including on the basis of an agreement with " +
                    "the client, to exert direct or indirect (through third parties) significant influence on decisions made by " +
                    "the client, to use their powers to influence the amount of the client's income, an individual " +
                    "has the ability to influence decisions made by the client regarding transactions (including those carrying " +
                    "credit risk (on granting loans, guarantees, etc.), as well as financial operations.").setPaddingTop(5)));
            table.addCell(new Cell().add(new Paragraph(
                    "2) Are you a Foreign public official, an official of a public international " +
                    "organization or a US public official").add(yes).add(no).setPaddingTop(5)).add(new Paragraph(
                    "2.1) Are you a relative of or acting on behalf of persons specified in item 2").add(yes).add(no)
                                                                                                                         .setPaddingTop(10)));
            document.add(table);
            document.add(new Paragraph("I confirm the authenticity of the information provided, Client signature:").setFontSize(8).setFirstLineIndent(5)
                                                                                                            .setMarginBottom(0));
            document.add(new LineSeparator(new SolidLine(0.5f)).setWidthPercent(12).setMarginLeft(260));
            document.add(new Paragraph("Passport validity verification completed")
                                 .setFontSize(8).setFirstLineIndent(5).setMarginTop(0));
            document.add(new Paragraph(userSession.getUser().getPositionText()).setMargin(0).setPaddingLeft(5).setFontSize(8).setWidthPercent(55));
            document.add(new LineSeparator(new SolidLine(0.5f)).setWidthPercent(55));
            document.add(new Paragraph(utils.getStrings().capitalizeFio(userSession.getUser().getLastname(), userSession.getUser().getFirstname(),
                                                                        userSession.getUser().getPatronymic())).setMargin(0).setFontSize(8)
                                                                                                               .setRelativePosition(305, 0, 0, 10));
            document.add(
                    new LineSeparator(new SolidLine(0.5f)).setWidthPercent(22).setHorizontalAlignment(HorizontalAlignment.CENTER).setMarginLeft(190)
                                                          .setRelativePosition(0, 0, 0, 10));
            document.add(
                    new LineSeparator(new SolidLine(0.5f)).setWidthPercent(15).setHorizontalAlignment(HorizontalAlignment.RIGHT).setMarginRight(10)
                                                          .setRelativePosition(0, 0, 0, 10));
            document.add(
                    new Paragraph("Employee position").setRelativePosition(100, 0, 0, 12).add(new Text("Full Name").setRelativePosition(190, 0, 0, 0))
                                                         .add(new Text("Signature").setRelativePosition(290, 0, 0, 0)).setFontSize(6).setMargin(0));
            document.add(new LineSeparator(new SolidLine(0.5f)).setWidthPercent(25).setMarginTop(5));
            document.add(new Paragraph(new Text("1").setRelativePosition(0, 0, 0, 3))
                                 .add(" The signature of the personal data subject means providing written consent for processing of personal data " +
                                      "and confirms the fact of notification about the possibility of obtaining personal data by operators not from " +
                                      "the personal data subject.").setFontSize(6));
        });
    }

    public String printMessageFinancialMonitoring(CurrencyOperation operation, UserSession userSession, Locale locale) {
        return reportService.buildReport(document -> {
            document.setFontSize(8);
            document.add(new Paragraph(new Text("Appendix No. 3").setBold())
                                 .add(" to the Program for identifying operations subject to mandatory control in client activities, and " +
                                      "operations in respect of which suspicions arise that they are carried out for ML/TF purposes.").setWidthPercent(35)
                                 .setHorizontalAlignment(HorizontalAlignment.RIGHT).setTextAlignment(TextAlignment.JUSTIFIED).setItalic()
                                 .setFixedLeading(10).setMarginTop(0).setMarginBottom(0));
            document.add(
                    new Paragraph("MESSAGE").setTextAlignment(TextAlignment.CENTER).setFontSize(12).setBold().setMarginTop(2).setMarginBottom(0));
            Table table = new Table(UnitValue.createPercentArray(new float[]{20, 20, 30, 30})).setMarginTop(0);
            DeviceRgb deviceRgb = new DeviceRgb(224, 224, 224);
            OutputOperationData outputOperationData = currencyExchangeBackService
                    .receiveOperationData(userSession.getUser().getLogin(), userSession.getUser().getDepartment().getExternalId(),
                                          operation.getExternalId(), operation.getCode(), operation.getDate());
            table.addCell(new Cell(1, 4).add("1. Operation type:").setBold().setBackgroundColor(deviceRgb));
            table.addCell(new Cell(1, 3).add("a) operation subject to mandatory control;"))
                 .addCell(new Cell().add(createCenterCell(outputOperationData.getRequireTransactionCode())));
            table.addCell(new Cell(1, 3).add(new Paragraph("b) operation in respect of which suspicions arise that it is carried out for " +
                                                           "money laundering (of proceeds obtained by criminal means) or terrorism financing " +
                                                           "purposes;").setFixedLeading(8))).addCell(new Cell());
            table.addCell(new Cell(1, 4).add(new Paragraph("2. Operation (transaction) content:").setBold())
                                        .add(new Paragraph(outputOperationData.getPaymentPurpose())).setBackgroundColor(deviceRgb));
            table.addCell(new Cell().add("3. Operation date").setBold().setBackgroundColor(deviceRgb))
                 .addCell(new Cell().add("Operation currency").setBold().setBackgroundColor(deviceRgb))
                 .addCell(new Cell().add("Operation amount in base currency equivalent").setBold().setBackgroundColor(deviceRgb))
                 .addCell(new Cell().add("Operation amount in transaction currency").setBold().setBackgroundColor(deviceRgb));
            BigDecimal sum = operation.isCommissionEnabled() ? operation.getBaseAmount()
                                                                        .add(operation.getCode() == OperationCode.SELL ? operation.getCommission() :
                                                                             operation.getCommission().negate()) : operation.getBaseAmount();
            table.addCell(createCenterCell(operation.getDate().format(FORMATTER))).addCell(createCenterCell(operation.getCurrency().getId()))
                 .addCell(createCenterCell(String.format(locale, "%,.2f", sum)))
                 .addCell(createCenterCell(String.format(locale, "%,.2f", operation.getSum())));
            document.add(table);
            UnitValue[] unitValues = UnitValue.createPercentArray(new float[]{33.3f, 33.3f, 33.3f});
            table = new Table(unitValues).setMarginTop(5);
            String clientName =
                    getFullName(operation.getPersonHistory().getPerson().getLastname(), operation.getPersonHistory().getPerson().getFirstname(),
                                operation.getPersonHistory().getPerson().getPatronymic());
            table.addCell(new Cell(1, 2).add(new Paragraph(
                    "4. Information about the payer performing the operation with funds or other property (name/EIN)")
                                                     .setFixedLeading(8)).setBold().setBackgroundColor(deviceRgb))
                 .addCell(createCenterCell(operation.getCode().equals(OperationCode.SELL) ? clientName : outputOperationData.getPayerName()));
            table.addCell(createCenterCell(
                    "For legal entity: location address; for individual: identity document information (name, series, " +
                    "number, issued by, date, code)", 8))
                 .addCell(createCenterCell("Name of credit organization servicing the payer", 8))
                 .addCell(createCenterCell("Payer account information"));
            BaseDocument personDocument = operation.getPersonHistory().getDocument();
            if (operation.getCode().equals(OperationCode.SELL)) {
                table.addCell(createCenterCell(
                        String.format("%s: %s No. %s Issued by %s %s%s", documentTypeDictionary.findOne(personDocument.getType()).getValue(),
                                      personDocument.getSeries() == null ? "" : String.format("series %s ", personDocument.getSeries()),
                                      personDocument.getNumber(), personDocument.getIssuanceUnit(),
                                      personDocument.getIssuanceDate().format(FORMATTER), personDocument.getIssuanceUnitCode() == null ? "" :
                                                                                          String.format(", code %s",
                                                                                                        personDocument.getIssuanceUnitCode())), 8))
                     .addCell(createCenterCell(outputOperationData.getPayerCreditOrganization(), 8)).addCell(createCenterCell("0"));
            } else {
                table.addCell(createCenterCell(outputOperationData.getPayerAddressOrDocument(), 8)).addCell(createCenterCell(
                        outputOperationData.getReceiverCreditOrganization(),
                        8)).addCell(createCenterCell("0"));
            }
            document.add(table);
            table = new Table(unitValues).setMarginTop(5);
            table.addCell(new Cell(1, 2).add(new Paragraph(
                    "Information about the receiver performing the operation with funds or other property (name/EIN)").setFixedLeading(8))
                                        .setBold().setBackgroundColor(deviceRgb))
                 .addCell(createCenterCell(operation.getCode().equals(OperationCode.BUY) ? clientName : outputOperationData.getReceiverName()));
            table.addCell(createCenterCell(
                    "For legal entity: location address; for individual: identity document information (name, series, " +
                    "number, issued by, date, code)", 8)).addCell(createCenterCell("Name of credit organization servicing the receiver", 8))
                 .addCell(createCenterCell("Receiver account information"));
            if (operation.getCode().equals(OperationCode.BUY)) {
                table.addCell(createCenterCell(
                        String.format("%s: %s No. %s Issued by %s %s%s", documentTypeDictionary.findOne(personDocument.getType()).getValue(),
                                      personDocument.getSeries() == null ? "" : String.format("series %s ", personDocument.getSeries()),
                                      personDocument.getNumber(), personDocument.getIssuanceUnit(),
                                      personDocument.getIssuanceDate().format(FORMATTER), personDocument.getIssuanceUnitCode() == null ? "" :
                                                                                          String.format(", code %s",
                                                                                                        personDocument.getIssuanceUnitCode())), 8))
                     .addCell(createCenterCell(outputOperationData.getPayerCreditOrganization(), 8)).addCell(createCenterCell("0"));
            } else {
                table.addCell(createCenterCell(outputOperationData.getReceiverAddressOrDocument(), 8)).addCell(createCenterCell(
                        outputOperationData.getReceiverCreditOrganization(),
                        8)).addCell(createCenterCell("0"));
            }
            document.add(table);
            unitValues = UnitValue.createPercentArray(new float[]{50, 50});
            table = new Table(unitValues).setMarginTop(5);
            table.addCell(new Cell(1, 2).add(new Paragraph(
                    "5. Description of difficulties encountered in qualifying as an operation subject to mandatory control, or reasons why " +
                    "the operation is qualified as an operation in respect of which suspicions arise that it is carried out for money laundering " +
                    "(of proceeds obtained by criminal means) or terrorism financing purposes;").setFixedLeading(8)).setBold()
                                        .setBackgroundColor(deviceRgb)).addCell(new Cell(1, 2).add(outputOperationData.getQualifyProblems()));
            Cell userCell = createCenterCell(
                    getFullName(userSession.getUser().getLastname(), userSession.getUser().getFirstname(), userSession.getUser().getPatronymic()) +
                    ", " + Optional.ofNullable(userSession.getUser().getPositionText()).orElse(""));
            table.addCell(
                    new Cell().add(new Paragraph("6. Information about the employee who prepared the message: Full Name (with position):").setFixedLeading(8))
                              .setBold().setBackgroundColor(deviceRgb)).addCell(userCell);
            table.addCell(new Cell().add("Signature of employee who prepared the message:").setBold()).addCell(new Cell());
            document.add(table);
            table = new Table(unitValues).setMarginTop(5);
            table.addCell(new Cell().add("7. Date and time of preparation of operation message:").setBold().setBackgroundColor(deviceRgb)).addCell(
                    createCenterCell(outputOperationData.getDate().atTime(outputOperationData.getTime()).plusMinutes(10)
                                                        .format(DateTimes.DATE_TIME_FORMATTER.withLocale(locale))));
            table.addCell(new Cell().add("Department head Full Name with position/signature:").setBold()).addCell(new Cell());
            document.add(table);
            table = new Table(unitValues).setMarginTop(5);
            table.addCell(new Cell(1, 2).add(new Paragraph(
                    "8. Date and time of receipt by Responsible employee, authorized employee of branch in AML/CFT area of operation message " +
                    "and their signature").setFixedLeading(8)).setBold().setBackgroundColor(deviceRgb));
            table.addCell(createCenterCell("Date/time").setPaddingBottom(12).setPaddingTop(0)).addCell(createCenterCell("Signature").setPaddingTop(0));
            table.addCell(new Cell(1, 2).add("9. Decision of Responsible employee, authorized employee of branch in AML/CFT area regarding " +
                                             "operation message").setBold().setBackgroundColor(deviceRgb));
            table.addCell(new Cell().add("Decision content:").setBold());
            Table subTable = new Table(unitValues);
            subTable.addCell(createCenterCell("Date/time").setBorderTop(Border.NO_BORDER).setBorderLeft(Border.NO_BORDER))
                    .addCell(createCenterCell("Signature").setBorderTop(Border.NO_BORDER).setBorderRight(Border.NO_BORDER));
            subTable.addCell(new Cell().setPaddingBottom(20).setBorderBottom(Border.NO_BORDER).setBorderLeft(Border.NO_BORDER))
                    .addCell(new Cell().setBorderBottom(Border.NO_BORDER).setBorderRight(Border.NO_BORDER));
            table.addCell(subTable);
            table.startNewRow().addCell(new Cell().add(new Paragraph().add(new Text("Reasoned justification").setBold())
                                                                      .add(" of the decision made (in case of decision not to send information " +
                                                                           "about the operation to the authorized body):").setFixedLeading(8)));
            table.addCell(subTable);
            table.startNewRow().addCell(new Cell(1, 2).add("10. Decision of Bank Board Chairman, Branch Manager, regarding the operation message ").setBold().setBackgroundColor(deviceRgb));
            table.addCell(new Cell());
            table.addCell(subTable);
            document.add(table);
        });
    }

    private Cell createLeftCell(String text) {
        return new Cell().add(text).setBorder(Border.NO_BORDER).setPaddingRight(50).setPaddingTop(0).setPaddingBottom(0);
    }

    private Cell createRightCell(String text) {
        return new Cell().add(text).setBorder(Border.NO_BORDER).setPaddingTop(0).setPaddingBottom(0);
    }

    private Cell createFixedLeadingLeftCell(String text) {
        return new Cell().add(new Paragraph(text)).setBorder(Border.NO_BORDER).setPaddingRight(20).setPaddingTop(0).setPaddingBottom(0);
    }

    private Cell createFixedLeadingRightCell(String text) {
        return new Cell().add(new Paragraph(text)).setBorder(Border.NO_BORDER).setPaddingTop(0).setPaddingBottom(0);
    }

    private Cell createCenterCell(String text) {
        return createCenterCell(text, 0);
    }

    private Cell createCenterCell(String text, float leading) {
        return leading > 0 ? new Cell().add(new Paragraph(text).setFixedLeading(leading)).setTextAlignment(TextAlignment.CENTER) :
               new Cell().add(text).setTextAlignment(TextAlignment.CENTER);
    }

    private String getFullName(String... strings) {
        return Stream.of(strings).filter(Objects::nonNull).collect(Collectors.joining(" "));
    }
}

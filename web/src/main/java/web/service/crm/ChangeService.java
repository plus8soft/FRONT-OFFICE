/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.crm;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.persistence.Column;
import javax.persistence.Table;
import javax.persistence.metamodel.SingularAttribute;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.dictionary.AddressTypeDictionary;
import web.dictionary.Unit;
import web.dictionary.AttractionSourceDictionary;
import web.dictionary.BusinessTypeDictionary;
import web.dictionary.ContactTypeDictionary;
import web.dictionary.DocumentTypeDictionary;
import web.entity.core.User;
import web.entity.crm.Address;
import web.entity.crm.Address_;
import web.entity.crm.Change;
import web.entity.crm.ChangeLog;
import web.entity.crm.ChangeLogData;
import web.entity.crm.ChangeType;
import web.entity.crm.Contact;
import web.entity.crm.Contact_;
import web.entity.crm.Document;
import web.entity.crm.Document_;
import web.entity.crm.Gender;
import web.entity.crm.Person;
import web.entity.crm.PersonAddress;
import web.entity.dict.Country;
import web.entity.crm.PersonAddress_;
import web.entity.crm.Person_;
import web.entity.crm.Photo;
import web.entity.crm.Photo_;
import web.entity.views.TableColumn;
import web.entity.views.TableColumn_;
import web.entity.views.TableDescription;
import web.entity.views.TableDescription_;
import web.repository.core.TableColumnRepository;
import web.repository.core.TableDescriptionRepository;
import web.repository.crm.ChangeLogDataRepository;
import web.repository.crm.ChangeLogRepository;
import web.repository.crm.ChangeRepository;
import web.repository.dict.CountryRepository;
import web.session.Menu;
import web.utils.DateTimes;
import web.view.ce.clientedit.PersonDiffContainer;

@Service
@Transactional
public class ChangeService {

    @Autowired
    private ChangeRepository changeRepository;

    @Autowired
    private ChangeLogRepository changeLogRepository;

    @Autowired
    private ChangeLogDataRepository changeLogDataRepository;

    @Autowired
    private TableColumnRepository tableColumnRepository;

    @Autowired
    private TableDescriptionRepository tableDescriptionRepository;

    @Autowired
    private AddressTypeDictionary addressTypeDictionary;

    @Autowired
    private ContactTypeDictionary contactTypeDictionary;

    @Autowired
    private DocumentTypeDictionary documentTypeDictionary;


    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private AttractionSourceDictionary attractionSourceDictionary;

    @Autowired
    private BusinessTypeDictionary businessTypeDictionary;

    private User user;

    public void addHistoryLog(User user, PersonDiffContainer diff, List<Document> removedDocuments, List<Contact> removedContacts) {
        this.user = user;
        Change change = new Change();
        change.setType(diff.getOldPerson().getId() == null ? ChangeType.C : ChangeType.U);
        change.setVersion(1);
        change.setUser(user);
        change.setDepartment(user.getDepartment());
        change.setPerson(diff.getPerson());
        change.setConnectionEvent(Menu.getInstance().getUserSession().getConnectionEvent());
        change.setDateTime(Instant.now());
        change.setTask(Menu.getInstance().getTask());
        saveChanges(change, logDocChanges(diff.getOldDocuments(), diff.getDocuments(), removedDocuments));
        saveChanges(change, getPersonDiff(diff.getOldPerson(), diff.getPerson()));
        saveChanges(change, logAddressChanges(diff.getOldPersonAddresses(), diff.getPersonAddresses()));
        saveChanges(change, logContactChanges(diff.getOldContacts(), diff.getContacts(), removedContacts));
        saveChanges(change, logPhotoChanges(diff.getOldPhoto(), diff.getPerson().getPhoto()));
    }

    private void saveChanges(Change change, List<ChangeLog> changeLogs) {
        changeLogs.forEach(changeLog -> {
            changeLog.setChange(change.getId() == null ? changeRepository.save(change) : change);
            changeLogRepository.save(changeLog);
            changeLog.getChangeLogDataList().forEach(logData -> {
                logData.setChangeLog(changeLog);
                changeLogDataRepository.save(logData);
            });
        });
    }

    private List<ChangeLog> logAddressChanges(List<PersonAddress> oldPersonAddresses, List<PersonAddress> personAddresses) {
        List<ChangeLog> changeLogs = new ArrayList<>();
        oldPersonAddresses.forEach(oldPersonAddress -> personAddresses.stream().filter(personAddress -> Objects
                .equals(oldPersonAddress.getType(), personAddress.getType())).forEach(personAddress -> {
            Address address = personAddress.getAddress();
            Address oldAddress = oldPersonAddress.getAddress();
            if (address == null || oldAddress == null) {
                return;
            }
            if (oldPersonAddress.getId() == null) {
                changeLogs.add(getLogs(defineChangeLogDataList(getDiffs(
                        defineDiff(null, addressTypeValue(personAddress.getType()), PersonAddress_.type),
                        defineDiff(null, addressTypeValue(personAddress.getMatchType()), PersonAddress_.matchType)), PersonAddress.class), ChangeType.C,
                                       personAddress.getId(), personAddress, addressTypeValue(personAddress.getType())));
            } else {
                changeLogs.add(getLogs(defineChangeLogDataList(getDiffs(
                        defineDiff(addressTypeValue(oldPersonAddress.getType()), addressTypeValue(personAddress.getType()),
                                   PersonAddress_.type),
                        defineDiff(addressTypeValue(oldPersonAddress.getMatchType()), addressTypeValue(personAddress.getMatchType()),
                                   PersonAddress_.matchType)), PersonAddress.class), ChangeType.U,
                                       personAddress.getId(), personAddress, addressTypeValue(personAddress.getType())));
            }
            if (personAddress.getMatchType() == null && oldPersonAddress.getMatchType() == null) {
                changeLogs.add(getLogs(defineChangeLogDataList(
                        getDiffs(defineDiff(oldAddress.getPostalCode(), address.getPostalCode(), Address_.postalCode),
                                 defineDiff(countryName(oldAddress.getCountry()), countryName(address.getCountry()), Address_.country),
                                 defineDiff(oldAddress.getRegion(), address.getRegion(), Address_.region),
                                 defineDiff(oldAddress.getRegionType(), address.getRegionType(), Address_.regionType),
                                 defineDiff(oldAddress.getDistrict(), address.getDistrict(), Address_.district),
                                 defineDiff(oldAddress.getDistrictType(), address.getDistrictType(), Address_.districtType),
                                 defineDiff(oldAddress.getCity(), address.getCity(), Address_.city),
                                 defineDiff(oldAddress.getCityType(), address.getCityType(), Address_.cityType),
                                 defineDiff(oldAddress.getStreet(), address.getStreet(), Address_.street),
                                 defineDiff(oldAddress.getStreetType(), address.getStreetType(), Address_.streetType),
                                 defineDiff(oldAddress.getHouse(), address.getHouse(), Address_.house),
                                 defineDiff(oldAddress.getHousing(), address.getHousing(), Address_.housing),
                                 defineDiff(oldAddress.getFlat(), address.getFlat(), Address_.flat),
                                 defineDiff(oldAddress.getStructure(), address.getStructure(), Address_.structure),
                                 defineDiff(oldAddress.getCode(), address.getCode(), Address_.code)), Address.class), ChangeType.U, address.getId(),
                                       address,
                                       addressTypeValue(personAddress.getType())));
            } else if (personAddress.getMatchType() == null && oldPersonAddress.getMatchType() != null) {
                changeLogs.add(getLogs(defineChangeLogDataList(getDiffs(
                        defineDiff(null, countryName(address.getCountry()), Address_.country), defineDiff(null, address.getRegion(), Address_.region),
                        defineDiff(null, address.getRegionType(), Address_.regionType), defineDiff(null, address.getDistrict(), Address_.district),
                        defineDiff(null, address.getDistrictType(), Address_.districtType), defineDiff(null, address.getCity(), Address_.city),
                        defineDiff(null, address.getCityType(), Address_.cityType),
                        defineDiff(null, address.getStreet(), Address_.street),
                        defineDiff(null, address.getStreetType(), Address_.streetType), defineDiff(null, address.getHouse(), Address_.house),
                        defineDiff(null, address.getHousing(), Address_.housing), defineDiff(null, address.getFlat(), Address_.flat),
                        defineDiff(null, address.getStructure(), Address_.structure), defineDiff(null, address.getCode(), Address_.code),
                        defineDiff(null, address.getPostalCode(), Address_.postalCode)), Address.class), ChangeType.C, address.getId(), address,
                                       addressTypeValue(personAddress.getType())));
            }
        }));
        return changeLogs.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    private String addressTypeValue(String code) {
        if (code == null) {
            return null;
        }
        Unit<String> unit = addressTypeDictionary.findOne(code);
        return unit != null ? unit.getValue() : code;
    }

    private String countryName(String countryId) {
        if (countryId == null) {
            return null;
        }
        Country country = countryRepository.findOne(countryId);
        return country != null ? country.getName() : countryId;
    }

    private List<ChangeLog> logContactChanges(List<Contact> oldContacts, List<Contact> contacts, List<Contact> removedContacts) {
        List<ChangeLog> changeLogs = new ArrayList<>();
        contacts.stream().filter(contact -> !oldContacts.contains(contact)).forEach(contact -> {
            changeLogs.add(getLogs(defineChangeLogDataList(getDiffs(
                    defineDiff(null, contact.getType() == null ? null : contactTypeDictionary.findOne(contact.getType()).getValue(), Contact_.type),
                    defineDiff(null, contact.getData(), Contact_.data), defineDiff(null, contact.getDescription(), Contact_.description),
                    defineDiff(null, contact.isMain(), Contact_.main), defineDiff(null, contact.isNotification(), Contact_.notification)),
                                                           Contact.class), ChangeType.C, contact.getId(), contact,
                                   contact.getType() == null ? null : contactTypeDictionary.findOne(contact.getType()).getValue()));
        });
        contacts.forEach(contact -> oldContacts.stream().filter(oldContact -> Objects.equals(contact, oldContact)).forEach(oldContact -> {
            changeLogs.add(getLogs(defineChangeLogDataList(
                    getDiffs(defineDiff(oldContact.getDescription(), contact.getDescription(), Contact_.description),
                             defineDiff(oldContact.getType() == null ? null : contactTypeDictionary.findOne(oldContact.getType()).getValue(),
                                        contact.getType() == null ? null : contactTypeDictionary.findOne(contact.getType()).getValue(),
                                        Contact_.type), defineDiff(oldContact.getData(), contact.getData(), Contact_.data),
                             defineDiff(oldContact.isMain(), contact.isMain(), Contact_.main),
                             defineDiff(oldContact.isNotification(), contact.isNotification(), Contact_.notification)), Contact.class), ChangeType.U,
                                   contact.getId(), contact,
                                   contact.getType() == null ? null : contactTypeDictionary.findOne(contact.getType()).getValue()));
        }));
        removedContacts.forEach(contact -> {
            changeLogs.add(getLogs(defineChangeLogDataList(getDiffs(
                    defineDiff(contact.getType() == null ? null : contactTypeDictionary.findOne(contact.getType()).getValue(), null, Contact_.type),
                    defineDiff(contact.getData(), null, Contact_.data), defineDiff(contact.getDescription(), null, Contact_.description),
                    defineDiff(contact.isMain(), null, Contact_.main), defineDiff(contact.isNotification(), null, Contact_.notification),
                    defineDiff(contact.isNotification(), null, Contact_.notification)), Contact.class), ChangeType.D, contact.getId(), contact,
                                   contact.getType() == null ? null : contactTypeDictionary.findOne(contact.getType()).getValue()));
        });
        return changeLogs.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    private List<ChangeLog> logDocChanges(List<Document> oldDocs, List<Document> docs, List<Document> removedDocuments) {
        List<ChangeLog> changeLogs = new ArrayList<>();
        docs.stream().filter(doc -> !oldDocs.contains(doc)).forEach(document -> {
            changeLogs.add(getLogs(defineChangeLogDataList(getDiffs(
                    defineDiff(null, document.getType() == null ? null : documentTypeDictionary.findOne(document.getType()).getValue(),
                               Document_.type), defineDiff(null, document.getIssuanceUnit(), Document_.issuanceUnit),
                    defineDiff(null, document.getIssuanceUnitCode(), Document_.issuanceUnitCode),
                    defineDiff(null, document.getNumber(), Document_.number), defineDiff(null, document.getSeries(), Document_.series),
                    defineDiff(null, document.getIssuanceDate() == null ? null : DateTimes.DATE_FORMATTER.format(document.getIssuanceDate()),
                               Document_.issuanceDate),
                    defineDiff(null, document.getValidUntilDate() == null ? null : DateTimes.DATE_FORMATTER.format(document.getValidUntilDate()),
                               Document_.validUntilDate)), Document.class), ChangeType.C, document.getId(), document,
                                   document.getType() == null ? null : documentTypeDictionary.findOne(document.getType()).getValue()));
        });
        docs.forEach(document -> oldDocs.stream().filter(oldDocument -> Objects.equals(document, oldDocument)).forEach(oldDocument -> {
            changeLogs.add(getLogs(defineChangeLogDataList(
                    getDiffs(defineDiff(oldDocument.getIssuanceUnit(), document.getIssuanceUnit(), Document_.issuanceUnit),
                             defineDiff(oldDocument.getIssuanceUnitCode(), document.getIssuanceUnitCode(), Document_.issuanceUnitCode),
                             defineDiff(oldDocument.getNumber(), document.getNumber(), Document_.number),
                             defineDiff(oldDocument.getSeries(), document.getSeries(), Document_.series),
                             defineDiff(oldDocument.getType() == null ? null : documentTypeDictionary.findOne(oldDocument.getType()).getValue(),
                                        document.getType() == null ? null : documentTypeDictionary.findOne(document.getType()).getValue(),
                                        Document_.type),
                             defineDiff(oldDocument.getIssuanceDate() == null ? null : DateTimes.DATE_FORMATTER.format(oldDocument.getIssuanceDate()),
                                        document.getIssuanceDate() == null ? null : DateTimes.DATE_FORMATTER.format(document.getIssuanceDate()),
                                        Document_.issuanceDate), defineDiff(
                                    oldDocument.getValidUntilDate() == null ? null : DateTimes.DATE_FORMATTER.format(oldDocument.getValidUntilDate()),
                                    document.getValidUntilDate() == null ? null : DateTimes.DATE_FORMATTER.format(document.getValidUntilDate()),
                                    Document_.validUntilDate)), Document.class), ChangeType.U, document.getId(), document,
                                   document.getType() == null ? null : documentTypeDictionary.findOne(document.getType()).getValue()));
        }));
        removedDocuments.forEach(document -> changeLogs.add(getLogs(defineChangeLogDataList(getDiffs(
                defineDiff(document.getIssuanceDate() == null ? null : DateTimes.DATE_FORMATTER.format(document.getIssuanceDate()), null,
                           Document_.issuanceDate), defineDiff(document.getIssuanceUnit(), null, Document_.issuanceUnit),
                defineDiff(document.getIssuanceUnitCode(), null, Document_.issuanceUnitCode),
                defineDiff(document.getNumber(), null, Document_.number), defineDiff(document.getSeries(), null, Document_.series),
                defineDiff(document.getType() == null ? null : documentTypeDictionary.findOne(document.getType()).getValue(), null, Document_.type),
                defineDiff(document.getValidUntilDate() == null ? null : DateTimes.DATE_FORMATTER.format(document.getValidUntilDate()), null,
                           Document_.validUntilDate)), Document.class), ChangeType.D, document.getId(), document, document.getType() == null ? null :
                                                                                                                  documentTypeDictionary
                                                                                                                          .findOne(document.getType())
                                                                                                                          .getValue())));
        return changeLogs.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    private List<ChangeLog> logPhotoChanges(Photo oldPhoto, Photo photo) {
        List<ChangeLog> changeLogs = new ArrayList<>();
        String oldDateTime = null;
        String newDateTime = null;
        if (photo == null && oldPhoto != null) {
            if (oldPhoto.getDateTime() != null) {
                oldDateTime = String.format("%s, %s",
                                            DateTimes.ZONED_DATE_TIME_FORMAT.format(oldPhoto.getDateTime().atZone(user.getDepartment().getZoneId())),
                                            DateTimes.ZONED_DATE_TIME_FORMAT.format(oldPhoto.getDateTime().atZone(ZoneId.systemDefault())));
            }
            changeLogs.add(getLogs(defineChangeLogDataList(getDiffs(
                    defineDiff(oldPhoto.getAdditionMethod() == null ? null : oldPhoto.getAdditionMethod().getValue(), null, Photo_.additionMethod),
                    defineDiff(oldDateTime, null, Photo_.dateTime)), Photo.class), ChangeType.D, oldPhoto.getId(), oldPhoto, null));
        } else if (oldPhoto == null && photo != null) {
            if (photo.getDateTime() != null) {
                newDateTime =
                        String.format("%s, %s", DateTimes.ZONED_DATE_TIME_FORMAT.format(photo.getDateTime().atZone(user.getDepartment().getZoneId())),
                                      DateTimes.ZONED_DATE_TIME_FORMAT.format(photo.getDateTime().atZone(ZoneId.systemDefault())));
            }
            changeLogs.add(getLogs(defineChangeLogDataList(
                    getDiffs(defineDiff(null, photo.getAdditionMethod() == null ? null : photo.getAdditionMethod().getValue(), Photo_.additionMethod),
                             defineDiff(null, newDateTime, Photo_.dateTime)), Photo.class), ChangeType.C, photo.getId(), photo, null));
        } else if (oldPhoto != null) {
            if (photo.getDateTime() != null) {
                newDateTime =
                        String.format("%s, %s", DateTimes.ZONED_DATE_TIME_FORMAT.format(photo.getDateTime().atZone(user.getDepartment().getZoneId())),
                                      DateTimes.ZONED_DATE_TIME_FORMAT.format(photo.getDateTime().atZone(ZoneId.systemDefault())));
            }
            if (oldPhoto.getDateTime() != null) {
                oldDateTime = String.format("%s, %s",
                                            DateTimes.ZONED_DATE_TIME_FORMAT.format(oldPhoto.getDateTime().atZone(user.getDepartment().getZoneId())),
                                            DateTimes.ZONED_DATE_TIME_FORMAT.format(oldPhoto.getDateTime().atZone(ZoneId.systemDefault())));
            }
            changeLogs.add(getLogs(defineChangeLogDataList(getDiffs(
                    defineDiff(oldPhoto.getAdditionMethod() == null ? null : oldPhoto.getAdditionMethod().getValue(),
                               photo.getAdditionMethod() == null ? null : photo.getAdditionMethod().getValue(), Photo_.additionMethod),
                    defineDiff(oldDateTime, newDateTime, Photo_.dateTime)), Photo.class), ChangeType.U, photo.getId(), photo, null));
        }
        return changeLogs.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    private List<ChangeLog> getPersonDiff(Person oldPerson, Person person) {
        List<ChangeLog> changeLogs = new ArrayList<>();
        if (oldPerson.getId() != null) {
            changeLogs.add(getLogs(defineChangeLogDataList(getDiffs(
                    defineDiff(oldPerson.getGender() == null ? null : Gender.MALE.equals(oldPerson.getGender()) ? "Male" : "Female",
                               person.getGender() == null ? null : Gender.MALE.equals(person.getGender()) ? "Male" : "Female", Person_.gender),
                    defineDiff(oldPerson.getLastname(), person.getLastname(), Person_.lastname),
                    defineDiff(oldPerson.getFirstname(), person.getFirstname(), Person_.firstname),
                    defineDiff(oldPerson.getPatronymic(), person.getPatronymic(), Person_.patronymic),
                    defineDiff(oldPerson.getLastnameDative(), person.getLastnameDative(), Person_.lastnameDative),
                    defineDiff(oldPerson.getFirstnameDative(), person.getFirstnameDative(), Person_.firstnameDative),
                    defineDiff(oldPerson.getPatronymicDative(), person.getPatronymicDative(), Person_.patronymicDative),
                    defineDiff(oldPerson.getLastnameGenitive(), person.getLastnameGenitive(), Person_.lastnameGenitive),
                    defineDiff(oldPerson.getFirstnameGenitive(), person.getFirstnameGenitive(), Person_.firstnameGenitive),
                    defineDiff(oldPerson.getPatronymicGenitive(), person.getPatronymicGenitive(), Person_.patronymicGenitive),
                    defineDiff(oldPerson.getLastnameInstrumental(), person.getLastnameInstrumental(), Person_.lastnameInstrumental),
                    defineDiff(oldPerson.getFirstnameInstrumental(), person.getFirstnameInstrumental(), Person_.firstnameInstrumental),
                    defineDiff(oldPerson.getPatronymicInstrumental(), person.getPatronymicInstrumental(), Person_.patronymicInstrumental),
                    defineDiff(
                            oldPerson.getAttractionSource() == null ? null :
                            attractionSourceDictionary.findOne(oldPerson.getAttractionSource()).getValue(),
                            person.getAttractionSource() == null ? null : attractionSourceDictionary.findOne(person.getAttractionSource()).getValue(),
                            Person_.attractionSource),
                    defineDiff(oldPerson.getBirthCountry() == null ? null : countryRepository.findOne(oldPerson.getBirthCountry()).getName(),
                               person.getBirthCountry() == null ? null : countryRepository.findOne(person.getBirthCountry()).getName(),
                               Person_.birthCountry),
                    defineDiff(oldPerson.getCitizenship() == null ? null : countryRepository.findOne(oldPerson.getCitizenship()).getName(),
                               person.getCitizenship() == null ? null : countryRepository.findOne(person.getCitizenship()).getName(),
                               Person_.citizenship), defineDiff(oldPerson.getEin(), person.getEin(), Person_.ein),
                    defineDiff(oldPerson.getResidentCountry() == null ? null : countryRepository.findOne(oldPerson.getResidentCountry()).getName(),
                               person.getResidentCountry() == null ? null : countryRepository.findOne(person.getResidentCountry()).getName(),
                               Person_.residentCountry),
                    defineDiff(oldPerson.getBirthDate() == null ? null : DateTimes.DATE_FORMATTER.format(oldPerson.getBirthDate()),
                               person.getBirthDate() == null ? null : DateTimes.DATE_FORMATTER.format(person.getBirthDate()), Person_.birthDate),
                    defineDiff(oldPerson.getBirthPlace(), person.getBirthPlace(), Person_.birthPlace),
                    defineDiff(oldPerson.getBusinessType() == null ? null : businessTypeDictionary.findOne(oldPerson.getBusinessType()).getValue(),
                               person.getBusinessType() == null ? null : businessTypeDictionary.findOne(person.getBusinessType()).getValue(),
                               Person_.businessType)), Person.class), ChangeType.U, person.getId(), person, null));
        } else {
            String attractionSource =
                    oldPerson.getAttractionSource() != null ? attractionSourceDictionary.findOne(person.getAttractionSource()).getValue() : null;
            changeLogs.add(getLogs(defineChangeLogDataList(getDiffs(
                    defineDiff(null, person.getBusinessType() == null ? null : businessTypeDictionary.findOne(person.getBusinessType()).getValue(),
                               Person_.businessType),
                    defineDiff(null, person.getGender() == null ? null : Gender.MALE.equals(person.getGender()) ? "Male" : "Female",
                               Person_.gender), defineDiff(null, person.getLastname(), Person_.lastname),
                    defineDiff(null, person.getFirstname(), Person_.firstname), defineDiff(null, person.getPatronymic(), Person_.patronymic),
                    defineDiff(null, person.getLastnameDative(), Person_.lastnameDative),
                    defineDiff(null, person.getFirstnameDative(), Person_.firstnameDative),
                    defineDiff(null, person.getPatronymicDative(), Person_.patronymicDative),
                    defineDiff(null, person.getLastnameGenitive(), Person_.lastnameGenitive),
                    defineDiff(null, person.getFirstnameGenitive(), Person_.firstnameGenitive),
                    defineDiff(null, person.getPatronymicGenitive(), Person_.patronymicGenitive),
                    defineDiff(null, person.getLastnameInstrumental(), Person_.lastnameInstrumental),
                    defineDiff(null, person.getFirstnameInstrumental(), Person_.firstnameInstrumental),
                    defineDiff(null, person.getPatronymicInstrumental(), Person_.patronymicInstrumental),
                    defineDiff(null, attractionSource, Person_.attractionSource),
                    defineDiff(null, person.getBirthCountry() == null ? null : countryRepository.findOne(person.getBirthCountry()).getName(),
                               Person_.birthCountry),
                    defineDiff(null, person.getBirthDate() == null ? null : DateTimes.DATE_FORMATTER.format(person.getBirthDate()),
                               Person_.birthDate), defineDiff(null, person.getBirthPlace(), Person_.birthPlace),
                    defineDiff(null, person.getCitizenship() == null ? null : countryRepository.findOne(person.getCitizenship()).getName(),
                               Person_.citizenship), defineDiff(null, person.getEin(), Person_.ein),
                    defineDiff(null, person.getResidentCountry() == null ? null : countryRepository.findOne(person.getResidentCountry()).getName(),
                               Person_.residentCountry)), Person.class), ChangeType.C,
                                   person.getId(), person, null));
        }
        return changeLogs.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    private List<DiffObject> getDiffs(DiffObject... diffs) {
        return Arrays.stream(diffs).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private <X, T> DiffObject defineDiff(Object oldValue, Object newValue, SingularAttribute<X, T> property) {
        return Objects.equals(oldValue, newValue) ? null : new DiffObject(Optional.ofNullable(oldValue).map(String::valueOf).orElse(null),
                                                                          Optional.ofNullable(newValue).map(String::valueOf).orElse(null), property);
    }

    private List<ChangeLogData> defineChangeLogDataList(List<DiffObject> diffList, Class<?> tableClass) {
        return diffList.stream().map(diffObject -> {
            TableColumn column = getColumn(diffObject, tableClass);
            if (column == null) {
                return null;
            }
            ChangeLogData changeLogData = new ChangeLogData();
            changeLogData.setOldValue(diffObject.oldValue);
            changeLogData.setNewValue(diffObject.newValue);
            changeLogData.setFieldName(column.getColumn());
            changeLogData.setFieldDescription(column.getDescription());
            return changeLogData;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private TableColumn getColumn(DiffObject diff, Class<?> tableClass) {
        return tableColumnRepository.findOne((root, query, cb) -> cb
                .and(cb.equal(root.get(TableColumn_.schema), tableClass.getAnnotation(Table.class).schema()),
                     cb.equal(root.get(TableColumn_.table), tableClass.getAnnotation(Table.class).name()),
                     cb.equal(root.get(TableColumn_.column), ((Field) diff.getProperty().getJavaMember()).getAnnotation(Column.class).name())));
    }

    private ChangeLog getLogs(List<ChangeLogData> changeLogDataList, ChangeType type, Long pkValue, Object entity, String entityType) {
        if (!changeLogDataList.isEmpty()) {
            TableDescription table = tableDescriptionRepository.findOne((root, query, cb) -> cb
                    .and(cb.equal(root.get(TableDescription_.table), entity.getClass().getAnnotation(Table.class).name()),
                         cb.equal(root.get(TableDescription_.schema), entity.getClass().getAnnotation(Table.class).schema())));
            ChangeLog changeLog = new ChangeLog();
            changeLog.setEntityPrimaryKey(pkValue);
            changeLog.setEntityDescription(table.getDescription());
            changeLog.setEntityName(table.getTable());
            changeLog.setType(type);
            changeLog.setEntityType(entityType);
            changeLog.getChangeLogDataList().addAll(changeLogDataList);
            return changeLog;
        }
        return null;
    }

    @Getter
    @AllArgsConstructor
    private class DiffObject<X, T> {

        private String oldValue;

        private String newValue;

        private SingularAttribute<X, T> property;
    }
}

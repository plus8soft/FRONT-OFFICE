/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.crm;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import web.entity.crm.Address;
import web.entity.crm.Document;
import web.entity.crm.Document_;
import web.entity.crm.Person;
import web.entity.crm.PersonAddress;
import web.entity.crm.PersonAddress_;
import web.entity.crm.Person_;
import web.projection.PersonAutoComplete;
import web.utils.Addresses;
import web.utils.Documents;

public class PersonRepositoryImpl implements PersonRepositoryCustom {

    @PersistenceContext(unitName = "front-office")
    private EntityManager manager;

    @Override
    public List<PersonAutoComplete> findByLikeDocument(Document document) {
        CriteriaBuilder cb = manager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<Document> root = query.from(Document.class);
        Path<Long> idPath = root.get(Document_.id);
        Path<String> typePath = root.get(Document_.type);
        Path<String> seriesPath = root.get(Document_.series);
        Path<String> numberPath = root.get(Document_.number);
        Path<Person> personPath = root.get(Document_.person);
        Path<Long> personId = personPath.get(Person_.id);
        Path<String> lastnamePath = personPath.get(Person_.lastname);
        Path<String> firstnamePath = personPath.get(Person_.firstname);
        Path<String> patronymicPath = personPath.get(Person_.patronymic);
        Path<LocalDate> birthDatePath = personPath.get(Person_.birthDate);
        ListJoin<Person, PersonAddress> join = root.join(Document_.person, JoinType.LEFT).join(Person_.personAddresses, JoinType.LEFT);
        Path<Long> personAddressIdPath = join.get(PersonAddress_.id);
        Path<Address> addressPath = join.get(PersonAddress_.address);
        Predicate clause = cb.and(cb.or(cb.equal(typePath, Documents.NATIONAL_PASSPORT_CODE),
                                        cb.equal(typePath, Documents.FOREIGN_CITIZEN_PASSPORT_CODE)),
                                  cb.like(numberPath, String.format("%s%%", document.getNumber())),
                                  cb.or(cb.equal(root.get(Document_.type), Documents.NATIONAL_PASSPORT_CODE),
                                        cb.equal(root.get(Document_.type), Documents.FOREIGN_CITIZEN_PASSPORT_CODE)),
                                  cb.or(cb.equal(join.get(PersonAddress_.type), Addresses.STAYING_TYPE)));
        query.multiselect(idPath, typePath, seriesPath, numberPath, personId, lastnamePath, firstnamePath, patronymicPath, birthDatePath,
                          personAddressIdPath, addressPath)
             .where(document.getSeries() == null ? clause : cb.and(clause, cb.equal(seriesPath, document.getSeries())));
        return manager.createQuery(query).setMaxResults(5).getResultList().stream().map(tuple -> {
            PersonAutoComplete autoComplete = new PersonAutoComplete();
            autoComplete.setDocumentId(tuple.get(idPath));
            autoComplete.setDocumentType(tuple.get(typePath));
            autoComplete.setDocumentSeries(tuple.get(seriesPath));
            autoComplete.setDocumentNumber(tuple.get(numberPath));
            autoComplete.setPersonId(tuple.get(personId));
            autoComplete.setLastName(tuple.get(lastnamePath));
            autoComplete.setFirstName(tuple.get(firstnamePath));
            autoComplete.setPatronymic(tuple.get(patronymicPath));
            autoComplete.setBirthDate(tuple.get(birthDatePath));
            autoComplete.setPersonAddressId(tuple.get(personAddressIdPath));
            autoComplete.setAddress(tuple.get(addressPath));
            return autoComplete;
        }).collect(Collectors.toList());
    }
}

/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.department.management.edit;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.RowEditEvent;
import org.primefaces.model.CheckboxTreeNode;
import org.primefaces.model.TreeNode;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import web.entity.core.Department;
import web.entity.core.DepartmentPaymentSystem;
import web.entity.core.Group;
import web.entity.dict.Account;
import web.entity.dict.AccountLink;
import web.entity.dict.AccountLinkType;
import web.entity.dict.AccountLink_;
import web.entity.dict.Account_;
import web.entity.dict.Currency;
import web.entity.dict.Currency_;
import web.entity.dict.PaymentSystem;
import web.repository.core.DepartmentPaymentSystemRepository;
import web.repository.core.DepartmentRepository;
import web.repository.core.GroupRepository;
import web.repository.core.UserRepository;
import web.repository.dict.AccountLinkRepository;
import web.repository.dict.CurrencyRepository;
import web.repository.dict.PaymentSystemRepository;
import web.view.CheckboxTree;
import web.view.Message;
import web.view.administration.model.UserFilter;
import web.view.administration.model.UserModel;
import web.view.component.AddressAutoComplete;

@Getter
@Setter
@Log4j2
public class EditView implements Serializable, CheckboxTree, Message {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private AccountLinkRepository accountLinkRepository;

    @Autowired
    private DepartmentPaymentSystemRepository departmentPaymentSystemRepository;

    @Autowired
    private PaymentSystemRepository paymentSystemRepository;

    private Department department;

    private AddressAutoComplete addressAutoComplete;

    private TreeNode groupTree;

    private TreeNode[] selectedGroups;

    private List<Currency> currencies;

    private List<Account> accounts;

    private List<Account> nightAccounts;

    private UserModel model;

    private UserFilter filter;

    private List<DepartmentPaymentSystem> departmentPaymentSystems;

    private List<PaymentSystem> paymentSystems;

    private DepartmentPaymentSystem departmentPaymentSystem;

    private DepartmentPaymentSystem oldDepartmentPaymentSystem;

    private List<DepartmentPaymentSystem> removedDepartmentPaymentSystems;

    public void init(UserModel userModel) {
        filter = new UserFilter();
        // Initialize lists to avoid NullPointerException
        if (departmentPaymentSystems == null) {
            departmentPaymentSystems = new ArrayList<>();
        }
        if (removedDepartmentPaymentSystems == null) {
            removedDepartmentPaymentSystems = new ArrayList<>();
        }
        if (department.getId() != null) {
            model = userModel;
            filter.setDepartment(department);
            model.setFilter(filter.clone());
            LocalDateTime now = LocalDateTime.now(department.getZoneId());
            accounts = accountLinkRepository.findAll((root, query, cb) -> {
                root.fetch(AccountLink_.account).fetch(Account_.currency);
                return cb
                        .and(cb.isTrue(root.get(AccountLink_.account).get(Account_.enabled)), cb.equal(root.get(AccountLink_.department), department),
                             cb.isFalse(root.get(AccountLink_.nightly)),
                             cb.equal(root.get(AccountLink_.type), AccountLinkType.CURRENCY_EXCHANGE.name()),
                             cb.lessThanOrEqualTo(root.get(AccountLink_.openDate), now),
                             cb.or(cb.isNull(root.get(AccountLink_.closeDate)), cb.greaterThan(root.get(AccountLink_.closeDate), now)));
            }).stream().map(AccountLink::getAccount).collect(Collectors.toList());
            nightAccounts = accountLinkRepository.findAll((root, query, cb) -> {
                root.fetch(AccountLink_.account).fetch(Account_.currency);
                return cb
                        .and(cb.isTrue(root.get(AccountLink_.account).get(Account_.enabled)), cb.equal(root.get(AccountLink_.department), department),
                             cb.isTrue(root.get(AccountLink_.nightly)),
                             cb.equal(root.get(AccountLink_.type), AccountLinkType.CURRENCY_EXCHANGE.name()),
                             cb.lessThanOrEqualTo(root.get(AccountLink_.openDate), now),
                             cb.or(cb.isNull(root.get(AccountLink_.closeDate)), cb.greaterThan(root.get(AccountLink_.closeDate), now)));
            }).stream().map(AccountLink::getAccount).collect(Collectors.toList());
            paymentSystems = paymentSystemRepository.findAll();
            if (department.getDepartmentPaymentSystems() != null) {
                departmentPaymentSystems = new ArrayList<>(department.getDepartmentPaymentSystems());
            } else {
                departmentPaymentSystems = new ArrayList<>();
            }
            for (DepartmentPaymentSystem departmentPaymentSystem : departmentPaymentSystems) {
                if (departmentPaymentSystem.getPaymentSystem() != null) {
                    paymentSystems.remove(departmentPaymentSystem.getPaymentSystem());
                }
            }
        } else {
            // For new department, initialize paymentSystems
            paymentSystems = paymentSystemRepository.findAll();
            if (departmentPaymentSystems == null) {
                departmentPaymentSystems = new ArrayList<>();
            }
        }
        buildTreeWithEmptyMessage(groupTree = new CheckboxTreeNode(), Group::getParent, Group::getPosition, groupRepository.findByAreUserIs(false),
                                  department.getGroups(), true, false);
        currencies = currencyRepository.findAll(new Sort(Currency_.iso.getName()));
        addressAutoComplete = new AddressAutoComplete(department);
    }

    public void addDepartmentPaymentSystem() {
        DepartmentPaymentSystem departmentPaymentSystem = new DepartmentPaymentSystem();
        departmentPaymentSystem.setDepartment(department);
        departmentPaymentSystems.add(departmentPaymentSystem);
    }

    public void onInitDepartmentPaymentSystemEdit(RowEditEvent event) {
        departmentPaymentSystem = (DepartmentPaymentSystem) event.getObject();
        if (departmentPaymentSystem.getPaymentSystem() != null && !paymentSystems.contains(departmentPaymentSystem.getPaymentSystem())) {
            paymentSystems.add(departmentPaymentSystem.getPaymentSystem());
        }
        BeanUtils.copyProperties(departmentPaymentSystem, oldDepartmentPaymentSystem = new DepartmentPaymentSystem());
    }

    public void onConfirmDepartmentPaymentSystemEdit() {
        paymentSystems.remove(departmentPaymentSystem.getPaymentSystem());
    }

    public void onCancelDepartmentPaymentSystemEdit() {
        BeanUtils.copyProperties(oldDepartmentPaymentSystem, departmentPaymentSystem);
        paymentSystems.remove(departmentPaymentSystem.getPaymentSystem());
    }

    public void onDepartmentPaymentSystemRowDelete(int index) {
        DepartmentPaymentSystem departmentPaymentSystem = departmentPaymentSystems.get(index);
        departmentPaymentSystems.remove(index);
        if (departmentPaymentSystem.getPaymentSystem() != null) {
            paymentSystems.add(departmentPaymentSystem.getPaymentSystem());
        }
        if (departmentPaymentSystem.getId() != null) {
            removedDepartmentPaymentSystems.add(departmentPaymentSystem);
        }
    }

    @Transactional
    public String save() {
        if (departmentPaymentSystems.stream().anyMatch(system -> system.getPaymentSystem() == null || system.getCode() == null)) {
            addErrorMessage("Payment system data is not filled");
            return null;
        } else {
            department.setGroups(mapTree(selectedGroups));
            department.setDepartmentPaymentSystems(departmentPaymentSystems);
            departmentRepository.save(department);
            departmentPaymentSystemRepository.save(departmentPaymentSystems);
            if (!removedDepartmentPaymentSystems.isEmpty()) {
                departmentPaymentSystemRepository.deleteInBatch(removedDepartmentPaymentSystems);
            }
            return "save";
        }
    }

    public void fastSearch() {
        filter.setExtendedSearch(false);
        updateFilter();
    }

    public void extendedSearch() {
        filter.setExtendedSearch(true);
        updateFilter();
    }

    private void updateFilter() {
        if (model != null) {
            model.setSelected(null);
            model.setFilter(filter.clone());
            model.reset();
            ((DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(":content:users")).reset();
        }
    }
}

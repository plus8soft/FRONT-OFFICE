/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.user.management.edit;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.CheckboxTreeNode;
import org.primefaces.model.TreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import web.entity.core.Certificate;
import web.entity.core.Certificate_;
import web.entity.core.Department;
import web.entity.core.Group;
import web.entity.core.Password;
import web.entity.core.Right;
import web.entity.core.Role;
import web.entity.core.SecurityProfile;
import web.entity.core.Task;
import web.entity.core.TrustedHost;
import web.entity.core.TrustedHost_;
import web.entity.core.User;
import web.repository.core.CertificateRepository;
import web.repository.core.GroupRepository;
import web.repository.core.PasswordRepository;
import web.repository.core.RightRepository;
import web.repository.core.RoleRepository;
import web.repository.core.SecurityProfileRepository;
import web.repository.core.TaskRepository;
import web.repository.core.TrustedHostRepository;
import web.repository.core.UserRepository;
import web.service.administration.department.DepartmentService;
import web.service.encryption.CryptoService;
import web.view.CheckboxTree;
import web.view.Message;

@Getter
@Setter
@Log4j2
public class EditView implements Serializable, CheckboxTree, Message {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RightRepository rightRepository;

    @Autowired
    private TrustedHostRepository trustedHostRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private SecurityProfileRepository securityProfileRepository;

    @Autowired
    private PasswordRepository passwordRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private CryptoService cryptoService;

    private User user;

    private String password;

    private List<Department> departments;

    private TreeNode groupTree;

    private TreeNode[] selectedGroups;

    private TreeNode departmentGroupTree;

    private TreeNode[] selectedDepartmentGroups;

    private TreeNode roleTree;

    private TreeNode[] selectedRoles;

    private TreeNode rightTree;

    private TreeNode[] selectedRights;

    private List<TrustedHost> trustedHosts;

    private List<TrustedHost> removedHosts;

    private TreeNode taskTree;

    private TreeNode[] selectedTasks;

    private List<SecurityProfile> securityProfiles;

    private List<Certificate> certificates;

    private List<Certificate> removedCerificates;

    private Certificate selectedCertificate;

    public void init() {
        if (user.getId() == null) {
            user.setStatus("ACTIVE");
        }
        departments = departmentService.getDepartmentFlatTree();
        buildTreeWithEmptyMessage(groupTree = new CheckboxTreeNode(), Group::getParent, Group::getPosition, groupRepository.findByAreUserIs(true),
                                  user.getGroups(), true, false);
        buildTreeWithEmptyMessage(departmentGroupTree = new CheckboxTreeNode(), Group::getParent, Group::getPosition,
                                  groupRepository.findByAreUserIs(false), user.getGroups(), true, false);
        buildGroupingTree(roleTree = new CheckboxTreeNode(), Role::getGroupName, roleRepository.findAll(), user.getRoles(), true);
        buildGroupingTree(rightTree = new CheckboxTreeNode(), Right::getGroupName, rightRepository.findAll(), user.getRights(), true);
        trustedHosts = user.getId() == null ? new ArrayList<>() :
                       trustedHostRepository.findAll((root, query, cb) -> cb.equal(root.get(TrustedHost_.user), user));
        removedHosts = new ArrayList<>();
        buildGroupingTree(taskTree = new CheckboxTreeNode(), Task::getProject, Task::getParent, taskRepository.findFetchProject(), user.getTasks(),
                          true, true);
        securityProfiles = securityProfileRepository.findAll();
        certificates = user.getId() == null ? new ArrayList<>() :
                       certificateRepository.findAll((root, query, cb) -> cb.equal(root.get(Certificate_.user), user));
        removedCerificates = new ArrayList<>();
    }

    public void addTrustedHost() {
        TrustedHost host = new TrustedHost();
        host.setUser(user);
        trustedHosts.add(host);
    }

    public void removeTrustedHost(int index) {
        TrustedHost removedHost;
        if ((removedHost = trustedHosts.remove(index)).getId() != null) {
            removedHosts.add(removedHost);
        }
    }

    public void removeCertificate() {
        certificates = certificates.stream().filter(certificate -> !certificate.getSerialNumber().equals(selectedCertificate.getSerialNumber()))
                                   .collect(Collectors.toList());
        removedCerificates.add(selectedCertificate);
        selectedCertificate = null;
    }

    public void uploadCertificate(FileUploadEvent event) {
        Certificate certificate = cryptoService.parseCertificate(new String(Base64.getEncoder().encode(event.getFile().getContents())));
        if (certificateRepository.exists((root, query, cb) -> cb.equal(root.get(Certificate_.serialNumber), certificate.getSerialNumber()))) {
            addErrorMessage("Certificate with this serial number already exists in the system");
        } else {
            certificate.setUser(user);
            certificate.setLocked(false);
            certificates.add(certificate);
        }
    }

    @Transactional
    public String save() {
        if (password != null) {
            user.setPassword(passwordEncoder.encode(password));
        }
        Predicate<TreeNode> predicate = treeNode -> CheckboxTreeNode.DEFAULT_TYPE.equals(treeNode.getType());
        user.setRoles(filterTreeArray(selectedRoles, predicate));
        user.setRights(filterTreeArray(selectedRights, predicate));
        user.setTasks(filterTreeArray(selectedTasks, predicate.and((other) -> ((Task) other.getData()).getSystemName() != null)));
        user.setGroups(mapTree(Stream.concat(Stream.of(this.selectedGroups), Stream.of(this.selectedDepartmentGroups))));
        userRepository.save(user);
        if (password != null) {
            passwordRepository.save(new Password(null, user, Instant.now(), user.getPassword()));
        }
        trustedHostRepository.delete(removedHosts);
        trustedHostRepository.save(trustedHosts.stream().filter(host -> host.getIp() != null).collect(Collectors.toList()));
        certificateRepository.delete(removedCerificates);
        certificateRepository.save(certificates);
        return "save";
    }
}

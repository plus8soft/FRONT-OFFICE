/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import javax.persistence.criteria.Subquery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.entity.AuthorizationResult;
import web.entity.core.Certificate;
import web.entity.core.Certificate_;
import web.entity.core.Department;
import web.entity.core.Department_;
import web.entity.core.Group_;
import web.entity.core.Password;
import web.entity.core.Right;
import web.entity.core.Right_;
import web.entity.core.Role;
import web.entity.core.Role_;
import web.entity.core.Task;
import web.entity.core.Task_;
import web.entity.core.User;
import web.entity.core.User_;
import web.repository.core.CertificateRepository;
import web.repository.core.DepartmentRepository;
import web.repository.core.PasswordRepository;
import web.repository.core.RightRepository;
import web.repository.core.TaskRepository;
import web.repository.core.UserRepository;
import web.service.administration.department.DepartmentService;
import web.service.encryption.CryptoService;

@Service
@Transactional
public class AuthorizationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private RightRepository rightRepository;

    @Autowired
    private PasswordRepository passwordRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    @Lazy
    private CryptoService cryptoService;

    public AuthorizationInfo login(String login, String password) throws AuthorizationException {
        Instant now = Instant.now();
        User user = userRepository.findOne((root, query, cb) -> {
            root.fetch(User_.department).fetch(Department_.parent, JoinType.LEFT);
            root.fetch(User_.securityProfile);
            return cb.equal(root.get(User_.login), login);
        });
        if (user == null) {
            throw new AuthorizationException(AuthorizationResult.WRONG_LOGIN, now);
        }
        if (!user.getDepartment().isEnabled()) {
            throw new AuthorizationException(AuthorizationResult.BLOCKED_DEPARTMENT, user, now);
        }
        if (user.getStatus().startsWith("LOCKED")) {
            throw new AuthorizationException(AuthorizationResult.BLOCKED, user, now);
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AuthorizationException(AuthorizationResult.WRONG_PASSWORD, user, now);
        }
        if (user.isRequireChangePassword()) {
            throw new AuthorizationException(AuthorizationResult.REQUIRE_CHANGE_PASSWORD, user, now);
        }
        if (now.isAfter(passwordRepository.findTopByUserOrderByIdDesc(user).getDate().plus(user.getSecurityProfile().getPasswordExpirationTerm(),
                                                                                           user.getSecurityProfile()
                                                                                               .getPasswordExpirationTermUnit()))) {
            throw new AuthorizationException(AuthorizationResult.EXPIRED_PASSWORD, user, now);
        }
        return collectAuthorizationInfo(user, now);
    }

    public AuthorizationInfo login(String certificateSerialNumber, String signedContent, String signature) throws AuthorizationException {
        Instant now = Instant.now();
        Certificate certificate = certificateRepository.findOne((root, query, cb) -> {
            root.fetch(Certificate_.user).fetch(User_.department).fetch(Department_.parent, JoinType.LEFT);
            return cb.and(cb.equal(root.get(Certificate_.serialNumber), certificateSerialNumber), cb.isFalse(root.get(Certificate_.locked)));
        });
        if (certificate == null) {
            throw new AuthorizationException(AuthorizationResult.WRONG_CERTIFICATE, now);
        }
        User user = certificate.getUser();
        if (!user.getDepartment().isEnabled()) {
            throw new AuthorizationException(AuthorizationResult.BLOCKED_DEPARTMENT, user, now);
        }
        if (user.getStatus().startsWith("LOCKED")) {
            throw new AuthorizationException(AuthorizationResult.BLOCKED, user, now);
        }
        if (now.isBefore(certificate.getStartDate()) || now.isAfter(certificate.getEndDate())) {
            throw new AuthorizationException(AuthorizationResult.WRONG_CERTIFICATE_PERIOD, user, now);
        }
        if (!cryptoService.verifySignature(certificate.getData(), signedContent, signature)) {
            throw new AuthorizationException(AuthorizationResult.WRONG_CERTIFICATE_SIGNATURE, user, now);
        }
        return collectAuthorizationInfo(user, now);
    }

    private AuthorizationInfo collectAuthorizationInfo(User user, Instant instant) throws AuthorizationException {
        AuthorizationInfo authorizationInfo = new AuthorizationInfo();
        authorizationInfo.setDate(instant);
        authorizationInfo.setUser(user);
        authorizationInfo.setTasks(taskRepository.findAll((root, query, cb) -> {
            root.fetch(Task_.project);
            query.distinct(true);
            Subquery<Task> taskUserSubQuery = query.subquery(Task.class);
            Root<Task> taskUserRoot = taskUserSubQuery.from(Task.class);
            taskUserSubQuery.select(taskUserRoot);
            taskUserSubQuery.where(cb.equal(taskUserRoot.join(Task_.users), user));
            Subquery<Task> taskRoleSubQuery = query.subquery(Task.class);
            Root<Task> taskRoleRoot = taskRoleSubQuery.from(Task.class);
            taskRoleSubQuery.select(taskRoleRoot);
            SetJoin<Task, Role> taskRoleJoin = taskRoleRoot.join(Task_.roles);
            taskRoleSubQuery.where(cb.or(cb.equal(taskRoleJoin.join(Role_.users, JoinType.LEFT), user),
                                         cb.equal(taskRoleJoin.join(Role_.groups, JoinType.LEFT).join(Group_.users, JoinType.LEFT), user)));
            return cb.or(cb.in(root).value(taskUserSubQuery), cb.in(root).value(taskRoleSubQuery));
        }));
        if (authorizationInfo.getTasks().isEmpty()) {
            throw new AuthorizationException(AuthorizationResult.EMPTY_AVAILABLE_TASKS, user, instant);
        }
        Set<Department> departments = departmentRepository.findAll((root, query, cb) -> {
            root.fetch(Department_.parent, JoinType.LEFT);
            return cb.equal(root.join(Department_.groups).join(Group_.users), user);
        }).stream().collect(Collectors.toSet());
        departments.add(user.getDepartment());
        authorizationInfo.setDepartmentsGraph(departmentService.getDepartmentFlatTree(departmentService.getDepartmentsGraphFromChilds(departments)));
        Set<Department> departmentsGraph = new LinkedHashSet<>(authorizationInfo.getDepartmentsGraph());
        departmentsGraph.retainAll(departments);
        authorizationInfo.setDepartments(departmentsGraph);
        authorizationInfo.setRights(rightRepository.findAll((root, query, cb) -> {
            query.distinct(true);
            Subquery<Right> rightUserSubQuery = query.subquery(Right.class);
            Root<Right> taskUserRoot = rightUserSubQuery.from(Right.class);
            rightUserSubQuery.select(taskUserRoot);
            rightUserSubQuery.where(cb.equal(taskUserRoot.join(Right_.users), user));
            Subquery<Right> rightRoleSubQuery = query.subquery(Right.class);
            Root<Right> rightRoleRoot = rightRoleSubQuery.from(Right.class);
            rightRoleSubQuery.select(rightRoleRoot);
            SetJoin<Right, Role> taskRoleJoin = rightRoleRoot.join(Right_.roles);
            rightRoleSubQuery.where(cb.or(cb.equal(taskRoleJoin.join(Role_.users, JoinType.LEFT), user),
                                          cb.equal(taskRoleJoin.join(Role_.groups, JoinType.LEFT).join(Group_.users, JoinType.LEFT), user)));
            return cb.or(cb.in(root).value(rightUserSubQuery), cb.in(root).value(rightRoleSubQuery));
        }).stream().map(Right::getSystemName).collect(Collectors.toSet()));
        user.setLastLoginEventDate(instant);
        userRepository.save(user);
        return authorizationInfo;
    }

    public void changePassword(User user, String password) {
        if (user.isRequireChangePassword()) {
            user.setRequireChangePassword(false);
        }
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        passwordRepository.save(new Password(null, user, Instant.now(), user.getPassword()));
    }
}

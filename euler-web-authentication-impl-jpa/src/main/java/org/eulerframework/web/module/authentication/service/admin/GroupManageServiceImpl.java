package org.eulerframework.web.module.authentication.service.admin;

import org.eulerframework.common.util.Assert;
import org.eulerframework.web.core.base.request.PageQueryRequest;
import org.eulerframework.web.core.base.response.PageResponse;
import org.eulerframework.web.module.authentication.entity.EulerGroupEntity;
import org.eulerframework.web.module.authentication.entity.Group;
import org.eulerframework.web.module.authentication.entity.User;
import org.eulerframework.web.module.authentication.repository.GroupRepository;
import org.eulerframework.web.module.authentication.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GroupManageServiceImpl implements GroupManageService {

    @Resource
    private GroupRepository groupRepository;
    @Resource
    private UserRepository userRepository;

    @Override
    public PageResponse<Group> findGroupByPage(PageQueryRequest pageQueryRequest) {
        Pageable pageable = PageRequest.of(pageQueryRequest.getPageIndex(), pageQueryRequest.getPageSize());

        Specification<Group> spec = (Specification<Group>) (root, query, criteriaBuilder) -> {
            // TODO Auto-generated method stub
            List<Predicate> p = new ArrayList<>();
            if(p.isEmpty()) {
                return null;
            }
            return criteriaBuilder.or(p.toArray(new Predicate[] {}));
        };

        Page<Group> page = this.groupRepository.findAll(spec , pageable);
        //Page<User> page = this.userRepository.findAll(pageable);
        PageResponse<Group> ret = new PageResponse<>(
                page.getContent(),
                page.getTotalElements(),
                page.getNumber(),
                page.getSize());

        return ret;
    }

    @Override
    public PageResponse<Group> findUserGroupByPage(PageQueryRequest pageQueryRequest) {
        String userId = pageQueryRequest.getQueryValue("userId");
        Assert.hasText(userId, "userId is required");

        Pageable pageable = PageRequest.of(pageQueryRequest.getPageIndex(), pageQueryRequest.getPageSize());

        User currentUserEntity = this.userRepository.findUserById(userId);

        if(currentUserEntity == null || CollectionUtils.isEmpty(currentUserEntity.getGroups())) {
            return new PageResponse<>(new ArrayList<>(), 0, pageQueryRequest.getPageIndex(), pageQueryRequest.getPageSize());
        }

        Set<String> currentUserGroupCodes = currentUserEntity.getGroups()
                .stream()
                .map(Group::getCode)
                .collect(Collectors.toSet());

        Specification<Group> spec = (Specification<Group>) (root, query, criteriaBuilder) -> {
            List<Predicate> p = new ArrayList<>();
            Predicate a = root.get("code").in(currentUserGroupCodes);
            p.add(a);
            return criteriaBuilder.or(p.toArray(new Predicate[] {}));
        };

        Page<Group> page = this.groupRepository.findAll(spec , pageable);
        //Page<User> page = this.userRepository.findAll(pageable);
        PageResponse<Group> ret = new PageResponse<>(
                page.getContent(),
                page.getTotalElements(),
                page.getNumber(),
                page.getSize());

        return ret;
    }
}

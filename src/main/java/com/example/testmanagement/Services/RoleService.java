package com.example.testmanagement.Services;


import com.example.testmanagement.Entities.Role;
import com.example.testmanagement.Repository.RoleRepository;
import com.example.testmanagement.Requests.RoleRequest;
import com.example.testmanagement.Responses.RoleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService
{
    private final RoleRepository roleRepository;

    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public RoleResponse createRole(RoleRequest request) {
        Role role = new Role();
        role.setName(request.getName());
        role.setPermissions(request.getPermissions() != null ? request.getPermissions() : new HashSet<>());
        return mapToResponse(roleRepository.save(role));
    }

    public RoleResponse updateRole(Long id, RoleRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        role.setName(request.getName());
        role.setPermissions(request.getPermissions() != null ? request.getPermissions() : new HashSet<>());
        return mapToResponse(roleRepository.save(role));
    }

    public void deleteRole(Long id) {
        roleRepository.deleteById(id);
    }

    private RoleResponse mapToResponse(Role role) {
        RoleResponse res = new RoleResponse();
        res.setId(role.getId());
        res.setName(role.getName());
        res.setPermissions(role.getPermissions());
        return res;
    }
}

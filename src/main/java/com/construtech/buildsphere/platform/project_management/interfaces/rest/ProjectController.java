package com.construtech.buildsphere.platform.project_management.interfaces.rest;

import com.construtech.buildsphere.platform.project_management.domain.model.aggregates.Dashboard;
import com.construtech.buildsphere.platform.project_management.domain.model.commands.CreateProjectCommand;
import com.construtech.buildsphere.platform.project_management.domain.model.commands.UpdateProjectCommand;
import com.construtech.buildsphere.platform.project_management.domain.model.commands.DeleteProjectCommand;
import com.construtech.buildsphere.platform.project_management.domain.model.queries.GetAllProjectsQuery;
import com.construtech.buildsphere.platform.project_management.domain.model.queries.GetProjectByIdQuery;
import com.construtech.buildsphere.platform.project_management.domain.services.ProjectCommandService;
import com.construtech.buildsphere.platform.project_management.domain.services.ProjectQueryService;
import com.construtech.buildsphere.platform.project_management.interfaces.rest.resources.CreateProjectResource;
import com.construtech.buildsphere.platform.project_management.interfaces.rest.resources.ProjectResource;
import com.construtech.buildsphere.platform.project_management.interfaces.rest.transform.CreateProjectCommandFromResourceAssembler;
import com.construtech.buildsphere.platform.project_management.interfaces.rest.transform.ProjectResourceFromEntityAssembler;
import com.construtech.buildsphere.platform.resourceManagement.domain.model.queries.GetAllMachinesByProjectIdQuery;
import com.construtech.buildsphere.platform.resourceManagement.domain.model.queries.GetAllMaterialsByProjectIdQuery;
import com.construtech.buildsphere.platform.resourceManagement.domain.model.valueobjects.Project;
import com.construtech.buildsphere.platform.resourceManagement.domain.services.MachineQueryService;
import com.construtech.buildsphere.platform.resourceManagement.domain.services.MaterialQueryService;
import com.construtech.buildsphere.platform.resourceManagement.interfaces.rest.resources.MachineResource;
import com.construtech.buildsphere.platform.resourceManagement.interfaces.rest.resources.MaterialResource;
import com.construtech.buildsphere.platform.resourceManagement.interfaces.rest.transform.MachineResourceFromEntityAssembler;
import com.construtech.buildsphere.platform.resourceManagement.interfaces.rest.transform.MaterialResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/projects", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Projects", description = "Projects Management Endpoints")
public class ProjectController {
    private final ProjectCommandService projectCommandService;
    private final ProjectQueryService projectQueryService;
    private final MaterialQueryService materialQueryService;
    private final MachineQueryService machineQueryService;

    public ProjectController(ProjectCommandService projectCommandService, ProjectQueryService projectQueryService, MaterialQueryService materialQueryService, MachineQueryService machineQueryService) {
        this.projectCommandService = projectCommandService;
        this.projectQueryService = projectQueryService;

        this.materialQueryService = materialQueryService;
        this.machineQueryService = machineQueryService;
    }

    @PostMapping
    public ResponseEntity<ProjectResource> createProject(@RequestBody CreateProjectResource resource) {
        var createProjectCommand = CreateProjectCommandFromResourceAssembler.toCommandFromResource(resource);
        var dashboard = projectCommandService.handle(createProjectCommand);
        if (dashboard.isEmpty()) return ResponseEntity.badRequest().build();
        var projectResource = ProjectResourceFromEntityAssembler.toResourceFromEntity(dashboard.get().getProject());
        return new ResponseEntity<>(projectResource, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResource> updateProject(@PathVariable Long id, @RequestBody CreateProjectResource resource) {
        var updateProjectCommand = new UpdateProjectCommand(
                id,
                resource.name(),
                resource.description(),
                resource.location(),
                resource.startDate(),
                resource.expectedEndDate(),
                resource.budget(),
                resource.urlImage(),
                resource.userId()
        );
        var dashboard = projectCommandService.handle(updateProjectCommand);
        if (dashboard.isEmpty()) return ResponseEntity.badRequest().build();
        var projectResource = ProjectResourceFromEntityAssembler.toResourceFromEntity(dashboard.get().getProject());
        return ResponseEntity.ok(projectResource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectCommandService.handle(new DeleteProjectCommand(id));
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ProjectResource>> getAllProjects() {
        var dashboards = projectQueryService.handle(new GetAllProjectsQuery());
        var projectResources = dashboards.stream()
                .map(dashboard -> ProjectResourceFromEntityAssembler.toResourceFromEntity(dashboard.getProject()))
                .toList();
        return ResponseEntity.ok(projectResources);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResource> getProjectById(@PathVariable Long id) {
        var dashboard = projectQueryService.handle(new GetProjectByIdQuery(id));
        if (dashboard.isEmpty()) return ResponseEntity.notFound().build();
        var projectResource = ProjectResourceFromEntityAssembler.toResourceFromEntity(dashboard.get().getProject());
        return ResponseEntity.ok(projectResource);
    }

    @GetMapping("/{projectId}/materials")
    public ResponseEntity<List<MaterialResource>> getAllMaterialsByProjectId(@PathVariable Long projectId) {
        var project = new Project(projectId);
        var getAllMaterialsByProjectIdQuery = new GetAllMaterialsByProjectIdQuery(project);
        var materials = materialQueryService.handle(getAllMaterialsByProjectIdQuery);
        var materialResources = materials.stream()
                .map(MaterialResourceFromEntityAssembler::toResourceFromEntity).toList();
        return ResponseEntity.ok(materialResources);
    }

    @GetMapping("/{projectId}/machines")
    public ResponseEntity<List<MachineResource>> getAllMachinesByProjectId(@PathVariable Long projectId) {
        var project = new Project(projectId);
        var getAllMachinesByProjectIdQuery = new GetAllMachinesByProjectIdQuery(project);
        var machines = machineQueryService.handle(getAllMachinesByProjectIdQuery);
        var machineResources = machines.stream()
                .map(MachineResourceFromEntityAssembler::toResourceFromEntity).toList();
        return ResponseEntity.ok(machineResources);
    }

}

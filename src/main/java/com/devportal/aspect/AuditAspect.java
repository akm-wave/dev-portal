package com.devportal.aspect;

import com.devportal.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.UUID;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final ActivityLogService activityLogService;

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void controllerMethods() {}

    @Pointcut("execution(* com.devportal.controller..*(..))")
    public void controllerPackage() {}

    @Pointcut("!execution(* com.devportal.controller.AuthController.login(..))")
    public void excludeLogin() {}

    @Pointcut("!execution(* com.devportal.controller.AuditController.*(..))")
    public void excludeAudit() {}

    @Pointcut("!execution(* com.devportal.controller.DashboardController.*(..))")
    public void excludeDashboard() {}

    @AfterReturning(pointcut = "controllerMethods() && controllerPackage() && excludeLogin() && excludeAudit() && excludeDashboard()", returning = "result")
    public void logAfterController(JoinPoint joinPoint, Object result) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            
            String action = determineAction(method);
            String entityType = determineEntityType(joinPoint.getTarget().getClass().getSimpleName());
            UUID entityId = extractEntityId(joinPoint.getArgs());
            String description = buildDescription(method, joinPoint.getArgs(), entityType);
            
            // Skip GET requests to reduce noise (optional - can be removed if all GETs should be logged)
            if (action.equals("VIEW") || action.equals("LIST")) {
                return; // Don't log read operations to reduce noise
            }
            
            activityLogService.logActivity(action, entityType, entityId, description);
            
        } catch (Exception e) {
            log.debug("Failed to log audit: {}", e.getMessage());
        }
    }

    private String determineAction(Method method) {
        if (method.isAnnotationPresent(PostMapping.class)) {
            return "CREATE";
        } else if (method.isAnnotationPresent(PutMapping.class)) {
            return "UPDATE";
        } else if (method.isAnnotationPresent(PatchMapping.class)) {
            return "UPDATE";
        } else if (method.isAnnotationPresent(DeleteMapping.class)) {
            return "DELETE";
        } else if (method.isAnnotationPresent(GetMapping.class)) {
            String methodName = method.getName().toLowerCase();
            if (methodName.contains("all") || methodName.contains("list")) {
                return "LIST";
            }
            return "VIEW";
        }
        return "UNKNOWN";
    }

    private String determineEntityType(String controllerName) {
        return controllerName
                .replace("Controller", "")
                .toUpperCase();
    }

    private UUID extractEntityId(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof UUID) {
                return (UUID) arg;
            }
        }
        return null;
    }

    private String buildDescription(Method method, Object[] args, String entityType) {
        String methodName = method.getName();
        StringBuilder desc = new StringBuilder();
        
        if (methodName.startsWith("create")) {
            desc.append("Created ").append(entityType.toLowerCase());
        } else if (methodName.startsWith("update")) {
            desc.append("Updated ").append(entityType.toLowerCase());
        } else if (methodName.startsWith("delete")) {
            desc.append("Deleted ").append(entityType.toLowerCase());
        } else if (methodName.startsWith("upload")) {
            desc.append("Uploaded attachment for ").append(entityType.toLowerCase());
        } else if (methodName.startsWith("download")) {
            desc.append("Downloaded attachment from ").append(entityType.toLowerCase());
        } else {
            desc.append(methodName).append(" on ").append(entityType.toLowerCase());
        }
        
        // Add entity ID if present
        UUID entityId = extractEntityId(args);
        if (entityId != null) {
            desc.append(" (ID: ").append(entityId.toString().substring(0, 8)).append("...)");
        }
        
        return desc.toString();
    }
}

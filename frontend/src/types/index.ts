export interface User {
  id: string;
  username: string;
  email: string;
  role: 'ADMIN' | 'USER';
}

export interface AuthResponse {
  token: string;
  type: string;
  username: string;
  email: string;
  role: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface PagedResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
  first: boolean;
}

export type ChecklistStatus = 'PENDING' | 'IN_PROGRESS' | 'DONE' | 'BLOCKED';
export type ChecklistPriority = 'LOW' | 'MEDIUM' | 'HIGH';
export type MicroserviceStatus = 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED';
export type FeatureStatus = 'PLANNED' | 'IN_PROGRESS' | 'RELEASED';

export interface Checklist {
  id: string;
  name: string;
  description: string;
  status: ChecklistStatus;
  priority: ChecklistPriority;
  createdBy: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ChecklistRequest {
  name: string;
  description?: string;
  status?: ChecklistStatus;
  priority?: ChecklistPriority;
}

export interface Microservice {
  id: string;
  name: string;
  description: string;
  version: string;
  owner: string | UserSummary;
  status: MicroserviceStatus;
  checklists: Checklist[];
  checklistCount: number;
  completedChecklistCount: number;
  progressPercentage: number;
  highRisk?: boolean;
  technicalDebtScore?: number;
  featureCount?: number;
  gitlabUrl?: string;
  createdAt: string;
  updatedAt: string;
}

export interface MicroserviceRequest {
  name: string;
  description?: string;
  version?: string;
  owner?: string;
  status?: MicroserviceStatus;
  checklistIds: string[];
  gitlabUrl?: string;
}

export interface Feature {
  id: string;
  name: string;
  description: string;
  domain: string;
  releaseVersion: string;
  targetDate: string;
  status: FeatureStatus;
  microservices: Microservice[];
  microserviceCount: number;
  totalChecklistCount: number;
  completedChecklistCount: number;
  progressPercentage: number;
  createdAt: string;
  updatedAt: string;
}

export interface FeatureRequest {
  name: string;
  description?: string;
  domain: string;
  releaseVersion?: string;
  targetDate?: string;
  status?: FeatureStatus;
  microserviceIds: string[];
}

export interface MicroserviceAnalysis {
  id: string;
  name: string;
  description: string;
  status: string;
  owner: string;
  version: string;
  progressPercentage: number;
  totalCheckpoints: number;
  completedCheckpoints: number;
  highRisk: boolean;
  featureCount: number;
  checkpoints: CheckpointSummary[];
}

export interface CheckpointSummary {
  id: string;
  name: string;
  status: ChecklistStatus;
  priority: string;
}

export interface CheckpointAnalysis {
  id: string;
  name: string;
  description: string;
  originalStatus: ChecklistStatus;
  featureStatus: ChecklistStatus;
  priority: string;
  remark: string | null;
  attachmentUrl: string | null;
  mongoFileId: string | null;
  attachmentFilename: string | null;
  updatedBy: string | null;
  updatedAt: string;
  connectedMicroservices: string[];
  connectedMicroserviceIds: string[];
}

export interface FeatureDetails {
  id: string;
  name: string;
  description: string;
  domain: string;
  status: FeatureStatus;
  releaseVersion: string;
  targetDate: string;
  createdAt: string;
  updatedAt: string;
  totalMicroservices: number;
  totalUniqueCheckpoints: number;
  overallProgress: number;
  microservices: MicroserviceAnalysis[];
  checkpoints: CheckpointAnalysis[];
}

export interface CheckpointProgressRequest {
  status?: ChecklistStatus;
  remark?: string;
  attachmentUrl?: string;
}

export interface HighImpactService {
  id: string;
  name: string;
  featureCount: number;
  progressPercentage: number;
}

export interface TechnicalDebtService {
  id: string;
  name: string;
  debtScore: number;
  blockedCount: number;
  stalePendingCount: number;
}

export interface DashboardStats {
  totalFeatures: number;
  totalMicroservices: number;
  totalChecklists: number;
  featuresByStatus: Record<string, number>;
  microservicesByStatus: Record<string, number>;
  checklistsByStatus: Record<string, number>;
  overallProgress: number;
  recentActivities: ActivityLog[];
  highImpactServices?: HighImpactService[];
  technicalDebtServices?: TechnicalDebtService[];
  issuesByCategory?: Record<string, number>;
  totalTechDebtIssues?: number;
  openTechDebtIssues?: number;
}

export interface ActivityLog {
  id: string;
  username: string;
  action: string;
  entityType: string;
  entityId: string;
  description: string;
  createdAt: string;
}

// Incident, Hotfix, Issue types
export type Severity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
export type IncidentStatus = 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED';
export type HotfixStatus = 'PLANNED' | 'IN_PROGRESS' | 'DEPLOYED';
export type IssueStatus = 'OPEN' | 'ASSIGNED' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED';
export type IssuePriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
export type IssueCategory = 'TECH_DEBT' | 'TECHNICAL_ISSUE' | 'PROD_ISSUE' | 'BUG' | 'ENHANCEMENT' | 'SECURITY' | 'PERFORMANCE' | 'OTHER';

export interface FeatureSummary {
  id: string;
  name: string;
  domain: string;
}

export interface UserSummary {
  id: string;
  username: string;
  email: string;
  fullName: string;
}

export interface MicroserviceSummary {
  id: string;
  name: string;
  status: string;
}

export interface Incident {
  id: string;
  title: string;
  description: string;
  severity: Severity;
  status: IncidentStatus;
  mainFeature: FeatureSummary;
  owner: UserSummary | null;
  createdBy: string;
  resolvedAt: string | null;
  createdAt: string;
  updatedAt: string;
  microservices: MicroserviceSummary[];
  microserviceCount: number;
}

export interface IncidentRequest {
  title: string;
  description?: string;
  severity: Severity;
  status?: IncidentStatus;
  mainFeatureId: string;
  ownerId?: string;
  microserviceIds?: string[];
}

export interface Hotfix {
  id: string;
  title: string;
  description: string;
  releaseVersion: string;
  status: HotfixStatus;
  mainFeature: FeatureSummary;
  owner: UserSummary | null;
  createdBy: string;
  deployedAt: string | null;
  createdAt: string;
  updatedAt: string;
  microservices: MicroserviceSummary[];
  microserviceCount: number;
}

export interface HotfixRequest {
  title: string;
  description?: string;
  releaseVersion?: string;
  status?: HotfixStatus;
  mainFeatureId: string;
  ownerId?: string;
  microserviceIds?: string[];
}

export interface Issue {
  id: string;
  title: string;
  description: string;
  priority: IssuePriority;
  status: IssueStatus;
  category: IssueCategory;
  mainFeature: FeatureSummary;
  assignedTo: UserSummary | null;
  owner: UserSummary | null;
  createdBy: string;
  resultComment: string | null;
  attachmentUrl: string | null;
  resolvedAt: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface IssueRequest {
  title: string;
  description: string;
  priority?: IssuePriority;
  status?: IssueStatus;
  category?: IssueCategory;
  mainFeatureId: string;
  assignedToId?: string;
  ownerId?: string;
  resultComment?: string;
  attachmentUrl?: string;
}

// Feature Checkpoint (execution-level tracking)
export interface FeatureCheckpoint {
  id: string;
  featureId: string;
  checklistId: string;
  checklistName: string;
  checklistDescription: string;
  checklistPriority: string;
  status: ChecklistStatus;
  remark: string | null;
  attachmentUrl: string | null;
  updatedBy: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface FeatureCheckpointUpdateRequest {
  status?: ChecklistStatus;
  remark?: string;
  attachmentUrl?: string;
}

// Issue Resolution types
export interface IssueAttachment {
  id: string;
  fileName: string;
  fileType: string;
  fileSize: number;
  fileUrl: string;
  uploadedBy: UserSummary | null;
  createdAt: string;
}

export interface IssueComment {
  id: string;
  content: string;
  user: UserSummary;
  isResolutionComment: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface IssueCommentRequest {
  content: string;
  isResolutionComment?: boolean;
}

// Global Search types
export interface GlobalSearchResult {
  domains: SearchItem[];
  features: SearchItem[];
  microservices: SearchItem[];
  checklists: SearchItem[];
  incidents: SearchItem[];
  hotfixes: SearchItem[];
  issues: SearchItem[];
  utilities: SearchItem[];
  releases: SearchItem[];
  attachments: SearchItem[];
  questions: SearchItem[];
  totalCount: number;
}

// Utility types
export type UtilityType = 'MOP' | 'CR_REQUIREMENT' | 'DEVELOPMENT_GUIDELINE' | 'SOP' | 'OTHERS';

export interface Utility {
  id: string;
  title: string;
  type: UtilityType;
  description: string;
  version: string | null;
  createdBy: UserSummary | null;
  createdAt: string;
  updatedAt: string;
  attachments?: UtilityAttachment[];
  attachmentCount: number;
}

export interface UtilityRequest {
  title: string;
  type?: UtilityType;
  description?: string;
  version?: string;
}

export interface UtilityAttachment {
  id: string;
  fileName: string;
  fileUrl: string;
  mongoFileId: string | null;
  fileType: string;
  fileSize: number;
  uploadedBy: UserSummary | null;
  uploadedAt: string;
}

// Issue Resolution types
export interface IssueResolution {
  id: string;
  issueId: string;
  comment: string;
  isResolutionComment: boolean;
  createdBy: UserSummary | null;
  createdAt: string;
  updatedAt: string;
  attachments: IssueResolutionAttachment[];
}

export interface IssueResolutionAttachment {
  id: string;
  mongoFileId: string;
  fileName: string;
  fileType: string;
  fileSize: number;
  uploadedBy: UserSummary | null;
  uploadedAt: string;
}

export interface IssueResolutionRequest {
  comment?: string;
  isResolutionComment?: boolean;
}

export interface SearchItem {
  id: string;
  name: string;
  description: string;
  type: string;
  status: string;
  url: string;
  contentSnippet?: string;
  fileName?: string;
  moduleType?: string;
  moduleId?: string;
}

export interface Domain {
  id: string;
  name: string;
  description: string;
  colorCode: string;
  isActive: boolean;
  createdBy: string;
  createdAt: string;
  updatedAt: string;
}

export interface DomainRequest {
  name: string;
  description?: string;
  colorCode?: string;
  isActive?: boolean;
}

export interface ChecklistProgress {
  id: string;
  checklistId: string;
  checklistName: string;
  checklistDescription: string;
  status: ChecklistStatus;
  remark: string;
  mongoFileId: string;
  attachmentFilename: string;
  updatedBy: string;
  createdAt: string;
  updatedAt: string;
}

export interface ChecklistProgressUpdateRequest {
  status?: ChecklistStatus;
  remark?: string;
}

// Release Management Types
export type ReleaseStatus = 'DRAFT' | 'SCHEDULED' | 'DEPLOYED' | 'ROLLED_BACK';
export type ReleaseLinkType = 'FEATURE' | 'INCIDENT' | 'HOTFIX' | 'ISSUE';

export interface Release {
  id: string;
  name: string;
  version: string;
  releaseDate: string | null;
  description: string | null;
  status: ReleaseStatus;
  oldBuildNumber: string | null;
  featureBranch: string | null;
  createdBy: UserSummary | null;
  microservices: ReleaseMicroservice[];
  links: ReleaseLink[];
  createdAt: string;
  updatedAt: string;
}

export interface ReleaseMicroservice {
  id: string;
  microserviceId: string;
  microserviceName: string;
  branchName: string | null;
  buildNumber: string | null;
  releaseDate: string | null;
  notes: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface ReleaseLink {
  id: string;
  entityType: ReleaseLinkType;
  entityId: string;
  entityName: string;
  createdAt: string;
}

export interface ReleaseRequest {
  name: string;
  version: string;
  releaseDate?: string;
  description?: string;
  status?: ReleaseStatus;
  oldBuildNumber?: string;
  featureBranch?: string;
  microservices?: ReleaseMicroserviceRequest[];
  links?: ReleaseLinkRequest[];
}

export interface ReleaseMicroserviceRequest {
  microserviceId: string;
  branchName?: string;
  buildNumber?: string;
  releaseDate?: string;
  notes?: string;
}

export interface ReleaseLinkRequest {
  entityType: ReleaseLinkType;
  entityId: string;
}

// ==================== UTILITIES ENHANCEMENTS ====================

export type TemplateEntityType = 'UTILITY' | 'ISSUE' | 'RELEASE';
export type SummaryType = 'THREAD_SUMMARY' | 'RELEASE_NOTES' | 'RESOLUTION_SUMMARY';

export interface Template {
  id: string;
  name: string;
  description?: string;
  entityType: TemplateEntityType;
  templateData: Record<string, unknown>;
  isDefault: boolean;
  isActive: boolean;
  createdBy?: UserSummary;
  createdAt: string;
  updatedAt: string;
}

export interface TemplateRequest {
  name: string;
  description?: string;
  entityType: TemplateEntityType;
  templateData: Record<string, unknown>;
  isDefault?: boolean;
}

export interface Tag {
  id: string;
  name: string;
  color: string;
  description?: string;
  createdAt: string;
  utilityCount?: number;
}

export interface TagRequest {
  name: string;
  color?: string;
  description?: string;
}

export interface UtilityCategory {
  id: string;
  name: string;
  description?: string;
  parentId?: string;
  parentName?: string;
  sortOrder: number;
  createdAt: string;
  utilityCount?: number;
  children?: UtilityCategory[];
}

export interface UtilityCategoryRequest {
  name: string;
  description?: string;
  parentId?: string;
  sortOrder?: number;
}

export interface UtilityVersion {
  id: string;
  utilityId: string;
  versionNumber: number;
  title: string;
  description?: string;
  content?: string;
  changeSummary?: string;
  createdBy?: UserSummary;
  createdAt: string;
  isCurrent: boolean;
}

// ==================== AI FEATURES ====================

export interface AiSummary {
  id: string;
  entityType: string;
  entityId: string;
  summaryType: SummaryType;
  summaryText: string;
  generatedAt: string;
  generatedBy: string;
  isApproved: boolean;
  approvedBy?: UserSummary;
  approvedAt?: string;
}

export interface SimilaritySuggestion {
  id: string;
  sourceEntityType: string;
  sourceEntityId: string;
  similarEntityType: string;
  similarEntityId: string;
  similarEntityName: string;
  similarEntityDescription?: string;
  similarityScore: number;
  suggestionReason: string;
  createdAt: string;
}

export interface ReleaseRecommendation {
  id: string;
  releaseId: string;
  recommendedEntityType: string;
  recommendedEntityId: string;
  recommendedEntityName: string;
  recommendedEntityDescription?: string;
  recommendationScore: number;
  recommendationReason: string;
  isAccepted: boolean;
  createdAt: string;
}

// My Workspace Types
export type ReminderPriority = 'LOW' | 'MEDIUM' | 'HIGH';
export type ReminderStatus = 'PENDING' | 'COMPLETED' | 'OVERDUE' | 'SNOOZED';
export type ModuleType = 'ISSUE' | 'MICROSERVICE' | 'RELEASE' | 'UTILITY' | 'INCIDENT' | 'HOTFIX' | 'FEATURE';

export interface UserNote {
  id: string;
  title: string;
  description?: string;
  tags?: string[];
  isPinned: boolean;
  isArchived: boolean;
  moduleType?: ModuleType;
  moduleId?: string;
  moduleName?: string;
  createdAt: string;
  updatedAt: string;
}

export interface UserNoteRequest {
  title: string;
  description?: string;
  tags?: string[];
  isPinned?: boolean;
  isArchived?: boolean;
  moduleType?: ModuleType;
  moduleId?: string;
}

export interface UserReminder {
  id: string;
  title: string;
  description?: string;
  reminderDatetime: string;
  priority: ReminderPriority;
  status: ReminderStatus;
  moduleType?: ModuleType;
  moduleId?: string;
  moduleName?: string;
  isSystemGenerated: boolean;
  snoozedUntil?: string;
  createdAt: string;
  updatedAt: string;
}

export interface UserReminderRequest {
  title: string;
  description?: string;
  reminderDatetime: string;
  priority?: ReminderPriority;
  moduleType?: ModuleType;
  moduleId?: string;
}

export interface ReminderCounts {
  overdue: number;
  pending: number;
}

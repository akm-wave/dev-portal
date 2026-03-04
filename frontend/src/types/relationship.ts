export interface MicroserviceNode {
  id: string;
  name: string;
  description: string;
  status: string;
  owner: string;
  version: string;
  featureCount: number;
  checklistCount: number;
  completedChecklistCount: number;
  progressPercentage: number;
  highRisk: boolean;
  technicalDebtScore: number;
}

export interface FeatureNode {
  id: string;
  name: string;
  description: string;
  domain: string;
  status: string;
  releaseVersion: string;
  targetDate: string | null;
  microserviceCount: number;
  isShared: boolean;
}

export interface RelationshipEdge {
  microserviceId: string;
  featureId: string;
}

export interface RelationshipData {
  microservices: MicroserviceNode[];
  features: FeatureNode[];
  relationships: RelationshipEdge[];
  microserviceToFeatures: Record<string, string[]>;
  featureToMicroservices: Record<string, string[]>;
}

export type ViewMode = 'list' | 'graph' | 'cards';
export type SelectionType = 'microservice' | 'feature' | null;

export interface SelectionState {
  type: SelectionType;
  id: string | null;
  connectedIds: Set<string>;
}

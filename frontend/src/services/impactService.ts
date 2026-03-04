import api from './api';

export interface ChangeType {
  value: string;
  label: string;
  riskWeight: number;
}

export interface MicroserviceChangeRequest {
  microserviceId: string;
  changeTypes: string[];
  notes?: string;
}

export interface ImpactCalculationRequest {
  featureId?: string;
  incidentId?: string;
  hotfixId?: string;
  issueId?: string;
  microserviceChanges: MicroserviceChangeRequest[];
}

export interface ImpactedArea {
  name: string;
  domain: string;
  impactLevel: number;
}

export interface ImpactedMicroservice {
  id: string;
  name: string;
  changeTypes: string[];
  riskScore: number;
  riskColor: string;
}

export interface CriticalChecklist {
  id: string;
  name: string;
  priority: string;
  status: string;
}

export interface RecommendedTest {
  testType: string;
  description: string;
  microserviceName: string;
  priority: string;
}

export interface ImpactAnalysisResponse {
  id: string;
  featureId?: string;
  incidentId?: string;
  hotfixId?: string;
  issueId?: string;
  riskScore: number;
  riskLevel: string;
  impactedAreas: ImpactedArea[];
  impactedMicroservices: ImpactedMicroservice[];
  criticalChecklists: CriticalChecklist[];
  recommendedTests: RecommendedTest[];
  analysisSummary: string;
  createdBy: string;
  createdAt: string;
}

export const impactService = {
  getChangeTypes: async (): Promise<ChangeType[]> => {
    const response = await api.get<ChangeType[]>('/impact/change-types');
    return response.data;
  },

  calculateImpact: async (request: ImpactCalculationRequest): Promise<ImpactAnalysisResponse> => {
    const response = await api.post<ImpactAnalysisResponse>('/impact/calculate', request);
    return response.data;
  },

  previewImpact: async (request: ImpactCalculationRequest): Promise<ImpactAnalysisResponse> => {
    const response = await api.post<ImpactAnalysisResponse>('/impact/preview', request);
    return response.data;
  },

  getFeatureImpact: async (featureId: string): Promise<ImpactAnalysisResponse | null> => {
    try {
      const response = await api.get<ImpactAnalysisResponse>(`/impact/feature/${featureId}`);
      return response.data;
    } catch {
      return null;
    }
  },

  getIncidentImpact: async (incidentId: string): Promise<ImpactAnalysisResponse | null> => {
    try {
      const response = await api.get<ImpactAnalysisResponse>(`/impact/incident/${incidentId}`);
      return response.data;
    } catch {
      return null;
    }
  },

  getHotfixImpact: async (hotfixId: string): Promise<ImpactAnalysisResponse | null> => {
    try {
      const response = await api.get<ImpactAnalysisResponse>(`/impact/hotfix/${hotfixId}`);
      return response.data;
    } catch {
      return null;
    }
  },

  getIssueImpact: async (issueId: string): Promise<ImpactAnalysisResponse | null> => {
    try {
      const response = await api.get<ImpactAnalysisResponse>(`/impact/issue/${issueId}`);
      return response.data;
    } catch {
      return null;
    }
  },

  getFeatureImpactHistory: async (featureId: string): Promise<ImpactAnalysisResponse[]> => {
    const response = await api.get<ImpactAnalysisResponse[]>(`/impact/feature/${featureId}/history`);
    return response.data;
  },
};

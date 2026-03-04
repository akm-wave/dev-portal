import { create } from 'zustand';
import { RelationshipData, SelectionState, ViewMode } from '../types/relationship';

interface RelationshipStore {
  data: RelationshipData | null;
  loading: boolean;
  error: string | null;
  viewMode: ViewMode;
  highlightMode: boolean;
  selection: SelectionState;
  microserviceSearch: string;
  featureSearch: string;
  microserviceStatusFilter: string | null;
  featureStatusFilter: string | null;

  setData: (data: RelationshipData) => void;
  setLoading: (loading: boolean) => void;
  setError: (error: string | null) => void;
  setViewMode: (mode: ViewMode) => void;
  toggleHighlightMode: () => void;
  selectMicroservice: (id: string | null) => void;
  selectFeature: (id: string | null) => void;
  clearSelection: () => void;
  setMicroserviceSearch: (search: string) => void;
  setFeatureSearch: (search: string) => void;
  setMicroserviceStatusFilter: (status: string | null) => void;
  setFeatureStatusFilter: (status: string | null) => void;
}

export const useRelationshipStore = create<RelationshipStore>((set, get) => ({
  data: null,
  loading: false,
  error: null,
  viewMode: 'list',
  highlightMode: true,
  selection: { type: null, id: null, connectedIds: new Set() },
  microserviceSearch: '',
  featureSearch: '',
  microserviceStatusFilter: null,
  featureStatusFilter: null,

  setData: (data) => set({ data }),
  setLoading: (loading) => set({ loading }),
  setError: (error) => set({ error }),
  setViewMode: (mode) => set({ viewMode: mode }),
  toggleHighlightMode: () => set((state) => ({ highlightMode: !state.highlightMode })),

  selectMicroservice: (id) => {
    const { data } = get();
    if (!id || !data) {
      set({ selection: { type: null, id: null, connectedIds: new Set() } });
      return;
    }
    const connectedFeatureIds = data.microserviceToFeatures[id] || [];
    set({
      selection: {
        type: 'microservice',
        id,
        connectedIds: new Set(connectedFeatureIds),
      },
    });
  },

  selectFeature: (id) => {
    const { data } = get();
    if (!id || !data) {
      set({ selection: { type: null, id: null, connectedIds: new Set() } });
      return;
    }
    const connectedMicroserviceIds = data.featureToMicroservices[id] || [];
    set({
      selection: {
        type: 'feature',
        id,
        connectedIds: new Set(connectedMicroserviceIds),
      },
    });
  },

  clearSelection: () => set({ selection: { type: null, id: null, connectedIds: new Set() } }),

  setMicroserviceSearch: (search) => set({ microserviceSearch: search }),
  setFeatureSearch: (search) => set({ featureSearch: search }),
  setMicroserviceStatusFilter: (status) => set({ microserviceStatusFilter: status }),
  setFeatureStatusFilter: (status) => set({ featureStatusFilter: status }),
}));

import { GeoPoint } from "firebase-admin/firestore";

export interface TaskDocument {
  status: string;
  location: GeoPoint;
  locationRadiusKm: number;
  bountyUsd: number;
  clientUid: string;
  clientFcmToken?: string;
  pinnedInspectorIds?: string[];
  expiresAtMs?: number;
  h3Res7?: string;
  h3Res9?: string;
  h3NeighborRes9?: string[];
}

export interface InspectorDocument {
  available: boolean;
  location: GeoPoint;
  fcmToken?: string;
  geohash?: string;
  h3Res7?: string;
  h3Res9?: string;
  h3NeighborRes9?: string[];
  rating?: number;
  tierLevel?: number;
}

export interface InspectorCandidate {
  uid: string;
  fcmToken?: string;
  distanceKm: number;
  rating?: number;
  tierLevel?: number;
}

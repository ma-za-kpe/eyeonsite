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
}

export interface InspectorDocument {
  available: boolean;
  location: GeoPoint;
  fcmToken?: string;
  geohash?: string;
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

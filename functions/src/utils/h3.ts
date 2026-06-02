import { cellToParent, gridDisk, latLngToCell } from "h3-js";

export const H3_PARENT_RESOLUTION = 7;
export const H3_TASK_RESOLUTION = 9;

export interface H3Coverage {
  h3Res7: string;
  h3Res9: string;
  h3NeighborRes9: string[];
}

export interface H3CellFields {
  h3Res9?: string;
  h3NeighborRes9?: string[];
}

function uniqueOrdered(values: string[]): string[] {
  const seen = new Set<string>();
  const result: string[] = [];
  for (const value of values) {
    if (value && !seen.has(value)) {
      seen.add(value);
      result.push(value);
    }
  }
  return result;
}

export function h3CoverageForPoint(lat: number, lng: number): H3Coverage {
  const h3Res9 = latLngToCell(lat, lng, H3_TASK_RESOLUTION);
  const h3Res7 = cellToParent(h3Res9, H3_PARENT_RESOLUTION);
  const h3NeighborRes9 = gridDisk(h3Res9, 1).filter((cell) => cell !== h3Res9);
  return { h3Res7, h3Res9, h3NeighborRes9 };
}

export function h3CandidateCellsForTask(task: H3CellFields, lat?: number, lng?: number): string[] {
  const persistedCells = uniqueOrdered([task.h3Res9 ?? "", ...(task.h3NeighborRes9 ?? [])]);
  if (persistedCells.length > 0) {
    return persistedCells;
  }
  if (lat == null || lng == null) {
    return [];
  }
  const coverage = h3CoverageForPoint(lat, lng);
  return [coverage.h3Res9, ...coverage.h3NeighborRes9];
}

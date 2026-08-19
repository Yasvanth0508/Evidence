import { type ClassValue, clsx } from "clsx";
import { twMerge } from "tailwind-merge";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export function formatScore(score: number | null | undefined): string {
  if (score === null || score === undefined) return "N/A";
  return `${score.toFixed(1)}/100`;
}

export function formatTime(minutes: number | null | undefined): string {
  if (!minutes) return "0 mins";
  return `${minutes} mins`;
}

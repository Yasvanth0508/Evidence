import { useState, useEffect, useMemo, useCallback } from "react";

export interface CountdownState {
  days: string;
  hours: string;
  minutes: string;
  seconds: string;
  totalRemainingSeconds: number;
  isUnlocked: boolean;
  formattedString: string;
  forceUnlock: () => void;
}

/**
 * Parses diverse human date + time inputs into a concrete target timestamp.
 * Example formats supported:
 * - "25 August 2026", "10:30 AM"
 * - "2026-08-25", "14:00"
 * - ISO string in fallbackIso
 */
export function parseScheduledTarget(
  dateStr?: string,
  timeStr?: string,
  fallbackIso?: string
): number {
  if (fallbackIso) {
    const d = new Date(fallbackIso);
    if (!isNaN(d.getTime())) return d.getTime();
  }

  if (!dateStr) {
    return Date.now();
  }

  if (dateStr.toLowerCase().includes("live") || dateStr.toLowerCase().includes("today")) {
    return Date.now() - 5000; // already live
  }

  // Handle ISO format in dateStr (e.g. 2026-08-28T10:00:00.000Z)
  if (dateStr.includes("T")) {
    const isoDate = new Date(dateStr);
    if (!isNaN(isoDate.getTime())) return isoDate.getTime();
  }

  const cleanDate = dateStr.replace(/,/g, "").trim();
  const cleanTime = (timeStr || "").trim();
  const combined = cleanTime ? `${cleanDate} ${cleanTime}` : cleanDate;

  const parsed = new Date(combined);
  if (!isNaN(parsed.getTime())) {
    return parsed.getTime();
  }

  // Attempt YYYY-MM-DD + HH:mm or HH:mm:ss
  if (cleanTime) {
    const isoAttempt = new Date(`${cleanDate}T${cleanTime}`);
    if (!isNaN(isoAttempt.getTime())) {
      return isoAttempt.getTime();
    }
  }

  const dateOnly = new Date(cleanDate);
  if (!isNaN(dateOnly.getTime())) {
    return dateOnly.getTime();
  }

  // If unparseable, default to now (unlocked) rather than an arbitrary 5 days lock
  return Date.now();
}

export function useAssessmentCountdown(
  scheduledDate: string,
  scheduledTime: string,
  scheduledStartAt?: string
): CountdownState {
  const [manualUnlocked, setManualUnlocked] = useState(false);
  const [now, setNow] = useState<number>(Date.now());

  const targetTimestamp = useMemo(() => {
    return parseScheduledTarget(scheduledDate, scheduledTime, scheduledStartAt);
  }, [scheduledDate, scheduledTime, scheduledStartAt]);

  useEffect(() => {
    const timer = setInterval(() => {
      setNow(Date.now());
    }, 1000);

    return () => clearInterval(timer);
  }, []);

  const forceUnlock = useCallback(() => {
    setManualUnlocked(true);
  }, []);

  const remainingMs = Math.max(0, targetTimestamp - now);
  const isUnlocked = manualUnlocked || remainingMs <= 0;

  const totalRemainingSeconds = Math.floor(remainingMs / 1000);

  const daysNum = Math.floor(totalRemainingSeconds / (3600 * 24));
  const hoursNum = Math.floor((totalRemainingSeconds % (3600 * 24)) / 3600);
  const minutesNum = Math.floor((totalRemainingSeconds % 3600) / 60);
  const secondsNum = totalRemainingSeconds % 60;

  const days = isUnlocked ? "00" : String(daysNum).padStart(2, "0");
  const hours = isUnlocked ? "00" : String(hoursNum).padStart(2, "0");
  const minutes = isUnlocked ? "00" : String(minutesNum).padStart(2, "0");
  const seconds = isUnlocked ? "00" : String(secondsNum).padStart(2, "0");

  const formattedString = `${days} Days : ${hours} Hours : ${minutes} Minutes : ${seconds} Seconds`;

  return {
    days,
    hours,
    minutes,
    seconds,
    totalRemainingSeconds,
    isUnlocked,
    formattedString,
    forceUnlock,
  };
}

import { create } from "zustand";

export interface IntegrityViolation {
  timestamp: string;
  type: "TAB_SWITCH" | "WINDOW_BLUR" | "COPY_PASTE";
  details: string;
  count: number;
}

interface IntegrityState {
  tabSwitchCount: number;
  maxAllowedSwitches: number;
  violations: IntegrityViolation[];
  showWarningModal: boolean;
  warningTitle: string;
  warningMessage: string;
  isAutoSubmitted: boolean;

  recordTabSwitch: (assessmentId: string) => { count: number; shouldAutoSubmit: boolean };
  acknowledgeWarning: () => void;
  resetIntegrity: () => void;
}

export const useIntegrityStore = create<IntegrityState>((set, get) => ({
  tabSwitchCount: 0,
  maxAllowedSwitches: 2,
  violations: [],
  showWarningModal: false,
  warningTitle: "",
  warningMessage: "",
  isAutoSubmitted: false,

  recordTabSwitch: (_assessmentId: string) => {
    const currentCount = get().tabSwitchCount + 1;
    const timestamp = new Date().toLocaleTimeString();
    const newViolation: IntegrityViolation = {
      timestamp,
      type: "TAB_SWITCH",
      details: `Candidate switched browser tab/window during assessment (Occurrence #${currentCount})`,
      count: currentCount,
    };

    const shouldAutoSubmit = currentCount > get().maxAllowedSwitches;

    if (shouldAutoSubmit) {
      set((state) => ({
        tabSwitchCount: currentCount,
        violations: [...state.violations, newViolation],
        isAutoSubmitted: true,
        showWarningModal: true,
        warningTitle: "Assessment Auto-Submitted",
        warningMessage:
          "You have switched tabs more than 2 times. As per anti-cheating policy, your assessment has been automatically submitted for evaluation.",
      }));
    } else {
      const remaining = get().maxAllowedSwitches - currentCount;
      set((state) => ({
        tabSwitchCount: currentCount,
        violations: [...state.violations, newViolation],
        showWarningModal: true,
        warningTitle: `Tab Switch Warning (${currentCount}/${get().maxAllowedSwitches})`,
        warningMessage: `Tab switching is strictly monitored. You have ${remaining === 1 ? "1 remaining warning" : "no warnings left"}. If you switch tabs again, your assessment will be submitted automatically.`,
      }));
    }

    return { count: currentCount, shouldAutoSubmit };
  },

  acknowledgeWarning: () => {
    set({ showWarningModal: false });
  },

  resetIntegrity: () => {
    set({
      tabSwitchCount: 0,
      violations: [],
      showWarningModal: false,
      warningTitle: "",
      warningMessage: "",
      isAutoSubmitted: false,
    });
  },
}));

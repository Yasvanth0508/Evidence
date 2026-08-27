import { useState } from "react";
import { HRAssessment } from "@/store/hrStore";
import { useAssessmentCountdown } from "@/hooks/useAssessmentCountdown";
import { Button } from "@/components/ui/button";
import {
  Calendar,
  Clock,
  Lock,
  Play,
  Building2,
  AlertCircle,
  Unlock,
  CheckCircle2,
  Sparkles,
} from "lucide-react";
import { PreAssessmentModal } from "@/components/candidate/PreAssessmentModal";

interface ScheduledAssessmentCardProps {
  assessment: HRAssessment & { workspaceName?: string };
}

export const ScheduledAssessmentCard = ({
  assessment,
}: ScheduledAssessmentCardProps) => {
  const [isHoveringDisabled, setIsHoveringDisabled] = useState(false);
  const [isPreAssessmentModalOpen, setIsPreAssessmentModalOpen] = useState(false);

  const {
    days,
    hours,
    minutes,
    seconds,
    isUnlocked,
    formattedString,
    forceUnlock,
  } = useAssessmentCountdown(
    assessment.scheduledDate,
    assessment.scheduledTime,
    assessment.scheduledStartAt
  );

  const handleTakeAssessment = () => {
    if (!isUnlocked) return;
    setIsPreAssessmentModalOpen(true);
  };

  return (
    <div
      className={`rounded-3xl border transition-all p-6 sm:p-7 flex flex-col justify-between space-y-6 shadow-2xs ${
        isUnlocked
          ? "bg-white border-emerald-300 ring-2 ring-emerald-400/20 shadow-md"
          : "bg-white border-gray-200/90"
      }`}
    >
      {/* Top Header */}
      <div className="space-y-3">
        <div className="flex flex-wrap items-center justify-between gap-2">
          <span className="inline-flex items-center gap-1.5 text-[11px] font-bold px-2.5 py-0.5 rounded-full bg-orange-50 text-[#F05323] border border-orange-200/60">
            <Sparkles className="w-3 h-3 text-[#F05323]" />
            {assessment.category || "Java Spring Boot Assessment"}
          </span>

          <div className="flex items-center gap-2">
            {isUnlocked ? (
              <span className="inline-flex items-center gap-1 text-xs font-extrabold px-3 py-1 rounded-full bg-emerald-50 text-emerald-700 border border-emerald-300 animate-pulse">
                <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600" />
                Available Now
              </span>
            ) : (
              <span className="inline-flex items-center gap-1 text-xs font-bold px-3 py-1 rounded-full bg-amber-50 text-amber-700 border border-amber-200">
                <Lock className="w-3.5 h-3.5 text-amber-600" />
                Scheduled
              </span>
            )}
          </div>
        </div>

        <div>
          <h3 className="text-xl font-extrabold text-gray-900 tracking-tight leading-tight">
            {assessment.title}
          </h3>
          <div className="flex flex-wrap items-center gap-3 mt-1.5 text-xs text-gray-500 font-medium">
            <span className="inline-flex items-center gap-1 text-gray-700 font-semibold">
              <Building2 className="w-3.5 h-3.5 text-gray-400" />
              {assessment.workspaceName || "Placement Drive"}
            </span>
            <span>•</span>
            <span className="text-gray-600 font-bold uppercase text-[11px]">
              {assessment.difficulty} Difficulty
            </span>
            <span>•</span>
            <span className="text-gray-600">{assessment.durationMinutes} Minutes</span>
          </div>
        </div>
      </div>

      {/* Scheduled Time Banner */}
      <div className="grid grid-cols-2 gap-3 p-3.5 rounded-2xl bg-gray-50/80 border border-gray-200 text-xs">
        <div className="space-y-0.5">
          <span className="text-[10px] uppercase font-bold text-gray-400 block tracking-wider">
            Scheduled Date
          </span>
          <span className="font-extrabold text-gray-900 flex items-center gap-1.5">
            <Calendar className="w-3.5 h-3.5 text-gray-500" />
            {assessment.scheduledDate}
          </span>
        </div>

        <div className="space-y-0.5">
          <span className="text-[10px] uppercase font-bold text-gray-400 block tracking-wider">
            Scheduled Time
          </span>
          <span className="font-extrabold text-gray-900 flex items-center gap-1.5">
            <Clock className="w-3.5 h-3.5 text-gray-500" />
            {assessment.scheduledTime}
          </span>
        </div>
      </div>

      {/* 4. Countdown Timer Section (Requirement 4) */}
      <div className="space-y-2 p-5 rounded-2xl bg-[#0F172A] text-white border border-gray-800 shadow-inner">
        <div className="flex items-center justify-between text-[11px] font-semibold tracking-wider text-gray-400 uppercase">
          <span>{isUnlocked ? "Status" : "Assessment starts in"}</span>
          <span className="text-[10px] font-mono text-orange-400">
            {isUnlocked ? "UNLOCKED" : "LIVE COUNTDOWN"}
          </span>
        </div>

        {/* Formatted Countdown Display (e.g. 10 Days : 02 Hours : 50 Minutes : 03 Seconds) */}
        <div className="pt-1">
          <div className="text-center font-mono font-black text-lg sm:text-xl text-[#F05323] tracking-wider py-1 bg-black/40 rounded-xl border border-white/5 shadow-inner">
            {formattedString}
          </div>
        </div>

        {/* Digits Breakdown Cards */}
        <div className="grid grid-cols-4 gap-2 pt-2 text-center font-mono">
          <div className="bg-slate-800/80 p-2 rounded-xl border border-slate-700/60">
            <span className="text-xl sm:text-2xl font-black text-white block leading-none">{days}</span>
            <span className="text-[9px] uppercase font-bold text-slate-400 tracking-wider mt-1 block">Days</span>
          </div>
          <div className="bg-slate-800/80 p-2 rounded-xl border border-slate-700/60">
            <span className="text-xl sm:text-2xl font-black text-white block leading-none">{hours}</span>
            <span className="text-[9px] uppercase font-bold text-slate-400 tracking-wider mt-1 block">Hours</span>
          </div>
          <div className="bg-slate-800/80 p-2 rounded-xl border border-slate-700/60">
            <span className="text-xl sm:text-2xl font-black text-white block leading-none">{minutes}</span>
            <span className="text-[9px] uppercase font-bold text-slate-400 tracking-wider mt-1 block">Mins</span>
          </div>
          <div className="bg-slate-800/80 p-2 rounded-xl border border-slate-700/60">
            <span className="text-xl sm:text-2xl font-black text-white block leading-none">{seconds}</span>
            <span className="text-[9px] uppercase font-bold text-slate-400 tracking-wider mt-1 block">Secs</span>
          </div>
        </div>

        {/* Demo Fast-Forward / Force Unlock button */}
        {!isUnlocked && (
          <div className="pt-2 text-center">
            <button
              type="button"
              onClick={forceUnlock}
              className="text-[10px] text-orange-300/80 hover:text-orange-300 font-semibold hover:underline inline-flex items-center gap-1"
            >
              <Unlock className="w-3 h-3" /> Simulate Countdown Finish (Test Mode)
            </button>
          </div>
        )}
      </div>

      {/* 5. Take Assessment Button (Disabled before start time, Enabled when zero) */}
      <div className="pt-1 space-y-2">
        <div
          className="relative"
          onMouseEnter={() => !isUnlocked && setIsHoveringDisabled(true)}
          onMouseLeave={() => setIsHoveringDisabled(false)}
        >
          <Button
            type="button"
            disabled={!isUnlocked}
            onClick={handleTakeAssessment}
            size="lg"
            className={`w-full font-bold gap-2 text-sm transition-all duration-300 ${
              isUnlocked
                ? "bg-[#F05323] hover:bg-[#d94417] text-white shadow-md hover:scale-[1.01] cursor-pointer"
                : "bg-gray-200 text-gray-400 cursor-not-allowed opacity-60 filter blur-[0.4px] hover:opacity-40 select-none shadow-none"
            }`}
          >
            {isUnlocked ? (
              <>
                <Play className="w-4 h-4 fill-white" /> Take Assessment Now
              </>
            ) : (
              <>
                <Lock className="w-4 h-4 text-gray-400" /> Take Assessment
              </>
            )}
          </Button>

          {/* Hover Notice for Disabled State */}
          {!isUnlocked && isHoveringDisabled && (
            <div className="absolute -top-10 left-1/2 -translate-x-1/2 bg-gray-900 text-white text-[11px] font-semibold px-3 py-1 rounded-lg shadow-xl whitespace-nowrap flex items-center gap-1.5 pointer-events-none animate-in fade-in zoom-in-95 duration-150 z-20">
              <AlertCircle className="w-3.5 h-3.5 text-amber-400" />
              Assessment is locked until scheduled time
            </div>
          )}
        </div>

        <p className="text-[11px] text-center text-gray-400 font-medium">
          {isUnlocked
            ? "Assessment is live. You can enter the isolated browser IDE now."
            : "The test environment will automatically unlock when the countdown reaches 00:00:00."}
        </p>
      </div>

      {/* Pre-Assessment Instructions & Start Confirmation Modal */}
      <PreAssessmentModal
        isOpen={isPreAssessmentModalOpen}
        onClose={() => setIsPreAssessmentModalOpen(false)}
        assessment={{
          id: assessment.id,
          title: assessment.title,
          workspaceName: assessment.workspaceName || "Placement Drive",
          difficulty: assessment.difficulty,
          durationMinutes: assessment.durationMinutes,
          scheduledStartAt: assessment.scheduledStartAt || "",
          scheduledEndAt: assessment.scheduledEndAt || "",
          techStack: assessment.category,
        }}
      />
    </div>
  );
};

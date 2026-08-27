import { FeatureSpecification } from "@/types";
import { Sparkles, CheckCircle2, AlertCircle, X, Code2 } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";
import { FormattedMarkdown, cleanRawString } from "@/components/common/FormattedMarkdown";

interface FeatureSpecDrawerProps {
  spec?: FeatureSpecification | any;
  isOpen: boolean;
  theme?: "dark" | "light";
  onClose: () => void;
}

const normalizeList = (val: any): string[] => {
  if (!val) return [];
  if (Array.isArray(val)) {
    const list: string[] = [];
    val.forEach((item) => {
      if (typeof item === "string") {
        const cleaned = cleanRawString(item);
        if (cleaned.includes("\n")) {
          cleaned.split("\n").forEach((s) => {
            const t = s.trim().replace(/^[-*•\d.]+\s*/, "");
            if (t) list.push(t);
          });
        } else {
          list.push(cleaned);
        }
      } else if (item && typeof item === "object") {
        const itemStr = item.requirement || item.item || item.description || item.text || item.constraint || JSON.stringify(item);
        list.push(cleanRawString(itemStr));
      } else {
        list.push(String(item));
      }
    });
    return list.filter(Boolean);
  }
  if (typeof val === "object") {
    if (Array.isArray(val.items)) return normalizeList(val.items);
    if (Array.isArray(val.requirements)) return normalizeList(val.requirements);
    if (Array.isArray(val.constraints)) return normalizeList(val.constraints);
    return Object.values(val).map((v) => cleanRawString(typeof v === "string" ? v : JSON.stringify(v))).filter(Boolean);
  }
  if (typeof val === "string") {
    const cleaned = cleanRawString(val);
    try {
      const parsed = JSON.parse(cleaned);
      return normalizeList(parsed);
    } catch {
      return cleaned
        .split("\n")
        .map((s) => s.trim().replace(/^[-*•\d.]+\s*/, ""))
        .filter(Boolean);
    }
  }
  return [cleanRawString(val)].filter(Boolean);
};

const formatSpecValue = (val: any): string => {
  if (!val) return "";
  if (typeof val === "string") {
    const cleaned = cleanRawString(val);
    try {
      const parsed = JSON.parse(cleaned);
      return JSON.stringify(parsed, null, 2);
    } catch {
      return cleaned;
    }
  }
  if (typeof val === "object") {
    return JSON.stringify(val, null, 2);
  }
  return String(val);
};

export const FeatureSpecDrawer = ({
  spec,
  isOpen,
  theme = "dark",
  onClose,
}: FeatureSpecDrawerProps) => {
  if (!isOpen) return null;

  const isDark = theme === "dark";
  const title = spec?.title || spec?.featureName || "Task Feature Specification";
  const description = spec?.description || "Implement the required feature according to the specification.";
  const httpMethod = spec?.httpMethod || "POST";
  const endpoint = spec?.endpoint || "/api/v1/resource";
  const requirements = normalizeList(spec?.requirements);
  const constraints = normalizeList(spec?.constraints);
  const requestSpec = formatSpecValue(spec?.requestSpecification);
  const responseSpec = formatSpecValue(spec?.responseSpecification);

  return (
    <div
      className={cn(
        "fixed inset-y-0 right-0 w-full sm:w-[480px] border-l shadow-2xl z-50 flex flex-col font-sans animate-in slide-in-from-right duration-300 transition-colors",
        isDark
          ? "bg-slate-900 border-slate-800 text-slate-200"
          : "bg-white border-gray-200 text-gray-700"
      )}
    >
      {/* Header */}
      <div
        className={cn(
          "p-5 border-b flex items-center justify-between flex-shrink-0 transition-colors",
          isDark
            ? "bg-slate-950/80 border-slate-800"
            : "bg-purple-50/50 border-gray-100"
        )}
      >
        <div className="flex items-center gap-2.5">
          <div
            className={cn(
              "w-8 h-8 rounded-lg flex items-center justify-center shadow-2xs",
              isDark
                ? "bg-purple-950 text-purple-300 border border-purple-800"
                : "bg-purple-100 text-purple-700"
            )}
          >
            <Sparkles className="w-4 h-4" />
          </div>
          <div>
            <h3 className={cn("font-bold text-sm", isDark ? "text-slate-100" : "text-gray-900")}>
              Task Feature Specification
            </h3>
            <span
              className={cn(
                "text-[11px] font-semibold",
                isDark ? "text-purple-400" : "text-purple-700"
              )}
            >
              AI-Generated Requirement Contract
            </span>
          </div>
        </div>

        <button
          onClick={onClose}
          className={cn(
            "w-8 h-8 rounded-lg flex items-center justify-center transition-colors shadow-2xs",
            isDark
              ? "text-slate-400 hover:text-white hover:bg-slate-800"
              : "text-gray-400 hover:text-gray-700 hover:bg-white"
          )}
          title="Close Drawer"
        >
          <X className="w-4 h-4" />
        </button>
      </div>

      {/* Drawer Body */}
      <div className="flex-1 p-6 overflow-y-auto space-y-6 text-xs">
        {/* Endpoint & Title Banner */}
        <div
          className={cn(
            "space-y-2.5 p-4 rounded-xl border transition-colors",
            isDark
              ? "bg-slate-800/80 border-slate-700"
              : "bg-gray-50/80 border-gray-200/70"
          )}
        >
          <div className="flex items-center gap-2">
            <Badge
              variant="default"
              className="font-mono font-extrabold text-[11px] px-2.5 py-0.5 bg-purple-700 text-white shadow-2xs"
            >
              {httpMethod}
            </Badge>
            <span
              className={cn(
                "font-mono font-bold text-xs",
                isDark ? "text-slate-200" : "text-gray-900"
              )}
            >
              {endpoint}
            </span>
          </div>
          <h4
            className={cn(
              "text-sm font-extrabold leading-snug",
              isDark ? "text-slate-100" : "text-gray-900"
            )}
          >
            {title}
          </h4>
          <FormattedMarkdown
            content={description}
            theme={theme}
            className={isDark ? "text-slate-300" : "text-gray-600"}
          />
        </div>

        {/* Requirements */}
        <div className="space-y-3">
          <h5
            className={cn(
              "font-bold uppercase text-[10px] tracking-wider flex items-center gap-1.5",
              isDark ? "text-slate-200" : "text-gray-900"
            )}
          >
            <CheckCircle2 className="w-3.5 h-3.5 text-emerald-500" />
            <span>Functional Requirements</span>
          </h5>
          {requirements.length === 0 ? (
            <p className="text-gray-400 italic">No explicit requirement items provided.</p>
          ) : (
            <ul className="space-y-2">
              {requirements.map((req, i) => (
                <li
                  key={i}
                  className={cn(
                    "flex items-start gap-2.5 p-3 rounded-xl border leading-relaxed font-medium transition-colors",
                    isDark
                      ? "bg-slate-800/90 border-slate-700 text-slate-200"
                      : "bg-gray-50/90 border-gray-200/60 text-gray-800"
                  )}
                >
                  <span className="w-5 h-5 rounded-full bg-emerald-500/20 text-emerald-400 font-bold text-[10px] flex items-center justify-center flex-shrink-0 mt-0.5">
                    {i + 1}
                  </span>
                  <div className="flex-1">
                    <FormattedMarkdown content={req} theme={theme} />
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>

        {/* Specifications Contract */}
        {(requestSpec || responseSpec) && (
          <div className="space-y-3">
            <h5
              className={cn(
                "font-bold uppercase text-[10px] tracking-wider flex items-center gap-1.5",
                isDark ? "text-slate-200" : "text-gray-900"
              )}
            >
              <Code2 className="w-3.5 h-3.5 text-indigo-400" />
              <span>Request & Response Contract</span>
            </h5>
            {requestSpec && (
              <div className="p-3.5 bg-slate-950 text-slate-100 rounded-xl border border-slate-800 font-mono text-[11px] overflow-x-auto space-y-1.5">
                <span className="text-indigo-400 block text-[9px] uppercase font-bold tracking-wider">
                  Request Specification
                </span>
                <FormattedMarkdown content={requestSpec} theme="dark" />
              </div>
            )}
            {responseSpec && (
              <div className="p-3.5 bg-slate-950 text-slate-100 rounded-xl border border-slate-800 font-mono text-[11px] overflow-x-auto space-y-1.5">
                <span className="text-emerald-400 block text-[9px] uppercase font-bold tracking-wider">
                  Expected Response Specification
                </span>
                <FormattedMarkdown content={responseSpec} theme="dark" />
              </div>
            )}
          </div>
        )}

        {/* Constraints */}
        <div className="space-y-2.5">
          <h5
            className={cn(
              "font-bold uppercase text-[10px] tracking-wider flex items-center gap-1.5",
              isDark ? "text-slate-200" : "text-gray-900"
            )}
          >
            <AlertCircle className="w-3.5 h-3.5 text-amber-400" />
            <span>Constraints & Edge Cases</span>
          </h5>
          {constraints.length === 0 ? (
            <div
              className={cn(
                "p-3 rounded-xl border text-[11px]",
                isDark
                  ? "bg-amber-950/30 border-amber-900/50 text-amber-200"
                  : "bg-amber-50/50 border-amber-200/60 text-amber-900"
              )}
            >
              Follow standard Spring Boot architectural conventions and pass all unit/integration tests.
            </div>
          ) : (
            <ul className="space-y-2">
              {constraints.map((c, i) => (
                <li
                  key={i}
                  className={cn(
                    "flex items-start gap-2 p-2.5 rounded-xl border font-medium text-xs transition-colors",
                    isDark
                      ? "bg-amber-950/40 border-amber-900/60 text-amber-200"
                      : "bg-amber-50/40 border-amber-200/50 text-amber-950"
                  )}
                >
                  <AlertCircle className="w-4 h-4 text-amber-500 flex-shrink-0 mt-0.5" />
                  <div className="flex-1">
                    <FormattedMarkdown content={c} theme={theme} />
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>
    </div>
  );
};

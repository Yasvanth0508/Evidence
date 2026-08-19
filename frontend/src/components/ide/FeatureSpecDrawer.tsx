import { FeatureSpecification } from "@/types";
import { Sparkles, CheckCircle2, AlertCircle, X } from "lucide-react";
import { Badge } from "@/components/ui/badge";

interface FeatureSpecDrawerProps {
  spec: FeatureSpecification;
  isOpen: boolean;
  onClose: () => void;
}

export const FeatureSpecDrawer = ({
  spec,
  isOpen,
  onClose,
}: FeatureSpecDrawerProps) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-y-0 right-0 w-full sm:w-[460px] bg-white border-l border-gray-200 shadow-2xl z-50 flex flex-col font-sans animate-in slide-in-from-right duration-300">
      {/* Header */}
      <div className="p-5 border-b border-gray-100 flex items-center justify-between bg-purple-50/40">
        <div className="flex items-center gap-2 text-purple-900">
          <div className="w-7 h-7 rounded-lg bg-purple-100 flex items-center justify-center text-purple-700">
            <Sparkles className="w-4 h-4" />
          </div>
          <div>
            <h3 className="font-bold text-sm">Feature Task Specification</h3>
            <span className="text-[10px] text-purple-600 font-medium">
              AI-Generated Assessment Feature
            </span>
          </div>
        </div>

        <button
          onClick={onClose}
          className="w-8 h-8 rounded-lg text-gray-400 hover:text-gray-700 hover:bg-white flex items-center justify-center transition-colors"
        >
          <X className="w-4 h-4" />
        </button>
      </div>

      {/* Drawer Body */}
      <div className="flex-1 p-6 overflow-y-auto space-y-6 text-xs text-gray-700">
        {/* Title & Method */}
        <div className="space-y-2">
          <div className="flex items-center gap-2">
            <Badge variant="default" className="font-mono font-bold text-[11px] px-2 py-0.5">
              {spec.httpMethod}
            </Badge>
            <span className="font-mono font-semibold text-gray-900 text-xs">
              {spec.endpoint}
            </span>
          </div>
          <h4 className="text-base font-extrabold text-gray-900">
            {spec.title}
          </h4>
          <p className="text-xs text-gray-600 leading-relaxed">
            {spec.description}
          </p>
        </div>

        {/* Requirements */}
        <div className="space-y-3">
          <h5 className="font-bold text-gray-900 uppercase text-[10px] tracking-wider">
            Requirements
          </h5>
          <ul className="space-y-2.5">
            {spec.requirements.map((req, i) => (
              <li key={i} className="flex items-start gap-2.5 bg-gray-50/80 p-2.5 rounded-xl border border-gray-100">
                <CheckCircle2 className="w-4 h-4 text-emerald-600 flex-shrink-0 mt-0.5" />
                <span className="leading-tight text-gray-700 font-medium">{req}</span>
              </li>
            ))}
          </ul>
        </div>

        {/* Specifications */}
        {(spec.requestSpecification || spec.responseSpecification) && (
          <div className="space-y-3">
            <h5 className="font-bold text-gray-900 uppercase text-[10px] tracking-wider">
              Request & Response Contract
            </h5>
            {spec.requestSpecification && (
              <div className="p-3 bg-gray-50 rounded-xl border border-gray-100 font-mono text-[11px]">
                <span className="text-gray-400 block text-[9px] uppercase font-bold mb-1">Request Spec</span>
                <span className="text-gray-800">{spec.requestSpecification}</span>
              </div>
            )}
            {spec.responseSpecification && (
              <div className="p-3 bg-gray-50 rounded-xl border border-gray-100 font-mono text-[11px]">
                <span className="text-gray-400 block text-[9px] uppercase font-bold mb-1">Response Spec</span>
                <span className="text-gray-800">{spec.responseSpecification}</span>
              </div>
            )}
          </div>
        )}

        {/* Constraints */}
        <div className="space-y-2">
          <h5 className="font-bold text-gray-900 uppercase text-[10px] tracking-wider">
            Constraints
          </h5>
          <ul className="space-y-1.5">
            {spec.constraints.map((c, i) => (
              <li key={i} className="flex items-start gap-2 text-gray-600">
                <AlertCircle className="w-3.5 h-3.5 text-amber-500 flex-shrink-0 mt-0.5" />
                <span>{c}</span>
              </li>
            ))}
          </ul>
        </div>
      </div>
    </div>
  );
};
